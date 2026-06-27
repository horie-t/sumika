# デプロイ手順（AWS / PoC）

PoC 構成: backend=ECS **Fargate Spot**、frontend=**S3 + CloudFront**、DB=**Aurora Serverless v2**。
**ALB なし**（ECS タスクが起動時に Route53 を更新）、**夜間停止**でコスト最小。IaC=Terraform、CD=GitHub Actions(OIDC)。

```
User ─HTTPS→ CloudFront ┬─ "/"     → S3(静的SPA)
                        └─ "/api/*" → Route53(sumika-api…) → ECS:8080 → Aurora
```

## 前提
- AWS 認証。本リポジトリの環境では `aws login`（独自方式）のため、Terraform/CLI 実行時は標準の環境変数へ変換する:
  ```bash
  eval "$(aws configure export-credentials --format env)"
  unset AWS_PROFILE
  ```
  （SSO を使う場合は `aws sso login --sso-session <name>` でも可）
- 既存の **Route53 ホステッドゾーン**（例 `t-horie.com`）と API ホスト名（例 `sumika-api.t-horie.com`）。
- アカウントに既存の GitHub OIDC プロバイダがある場合、Terraform はそれを参照する（新規作成しない）。

## 1. インフラ構築（Terraform）
```bash
cd infra
cp terraform.tfvars.example terraform.tfvars   # route53_zone_id / api_hostname を設定
terraform init
terraform apply                                # 課金リソース作成（Aurora/CloudFront 等）
terraform output                               # 後段で使う値を取得
```
state はローカル（PoC）。`.terraform/`・`*.tfstate`・`terraform.tfvars` は git 管理外。

## 2. GitHub Actions 変数（`terraform output` から設定）
リポジトリ Variables（Secrets ではなく Variables）に設定:

| 変数 | 値（output） |
|------|------|
| `AWS_REGION` | `us-west-2` |
| `AWS_DEPLOY_ROLE_ARN` | `github_deploy_role_arn` |
| `ECR_REPOSITORY` | `ecr_repository_url` |
| `ECS_CLUSTER` | `ecs_cluster` |
| `ECS_SERVICE` | `ecs_service` |
| `FRONTEND_BUCKET` | `frontend_bucket` |
| `CLOUDFRONT_DISTRIBUTION_ID` | `cloudfront_distribution_id` |
| `DEPLOY_ENABLED` | `true`（準備完了後に有効化） |

```bash
gh variable set AWS_REGION --body us-west-2
# ... 各変数を設定 ...
gh variable set DEPLOY_ENABLED --body true
```

## 3. デプロイ（CD: `.github/workflows/deploy.yml`）
- `main` への push、または手動 `gh workflow run deploy.yml` で起動（`DEPLOY_ENABLED=true` が条件）。
- backend: OIDC assume → ECR へ `docker build/push`（`:latest`/`:sha`）→ `ecs update-service --force-new-deployment`。
- frontend: `npm ci && npm run build` → `aws s3 sync` → CloudFront invalidation。
- 起動時、タスクが public IP を `api_hostname` の A レコードへ UPSERT。Flyway がマイグレーションを適用。

## 4. 動作確認
`terraform output cloudfront_domain` の URL を開く。`/` で SPA、`/api/...` で API。
（初回・夜間停止明け・Spot 中断直後は **コールドスタート/DNS 反映で一時的に 5xx** → 数十秒で復帰）

## コスト運用（夜間停止）
- EventBridge Scheduler が ECS `desiredCount` を **夜=0 / 朝=1**（既定 JST 22:00 / 09:00）。
- Aurora Serverless v2 は **min ACU=0** でアイドル時に自動一時停止。
- 変更は `infra/variables.tf` の `stop_cron` / `start_cron` / `aurora_min_acu` 等。

## 既知のトレードオフ（PoC）
- **ALB なし**: ヘルスチェックベースのルーティングがなく、タスク再作成直後は DNS 反映まで `/api` が一時不通。
- Fargate **Spot**: 中断され得る（desired 維持で再起動）。

## 破棄
```bash
cd infra
terraform destroy
```
