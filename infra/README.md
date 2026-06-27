# infra (Terraform / PoC)

PoC 用の AWS インフラ。**ALB なし**（ECS タスクが起動時に Route53 を更新）、**夜間停止**、コスト最小。

## 構成
- VPC（public: ECS / private: Aurora、NAT なし）、SG
- ECR、ECS(Fargate **Spot**, desired=1)、Aurora Serverless v2(min ACU 0 = 自動一時停止)
- S3 + CloudFront（`/` → S3、`/api/*` → Route53 ホスト名 → ECS:8080）
- EventBridge Scheduler（夜間 desired=0 / 朝 =1）
- GitHub OIDC provider + デプロイロール

## 前提
- AWS 認証（`aws login` / SSO 等）。
- **既存 Route53 ホステッドゾーン**（`route53_zone_id`）と API ホスト名（`api_hostname`）。

## 使い方
```bash
cd infra
cp terraform.tfvars.example terraform.tfvars   # 値を編集
terraform init
terraform plan
terraform apply
```
構文チェックのみ（AWS 認証不要）:
```bash
terraform init -backend=false
terraform fmt -check
terraform validate
```

## 補足
- API レコード（`api_hostname` の A）は **ECS タスクが起動時に UPSERT** するため Terraform では管理しない。
- `terraform apply` 後に backend イメージを ECR へ push → タスク起動で Route53 更新 → CloudFront 経由で到達。
- state は PoC のためローカル（必要なら S3 backend に移行）。
