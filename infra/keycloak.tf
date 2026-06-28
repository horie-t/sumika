# 自前ホストの Keycloak（ECS Fargate Spot）。CloudFront の /auth/* から到達し、
# 起動時に sidecar(aws-cli) が自 public IP を Route53(keycloak_hostname) へ UPSERT する（ALB なし）。
# DB は同一 Aurora クラスタ内の専用 database（下の null_resource で作成）。

locals {
  # sidecar が実行する Route53 更新スクリプト（タスクの public IP を keycloak_hostname に UPSERT）。
  keycloak_route53_script = <<-EOT
    set -e
    IP=$(curl -fsS --max-time 5 https://checkip.amazonaws.com | tr -d '[:space:]')
    printf '{"Changes":[{"Action":"UPSERT","ResourceRecordSet":{"Name":"%s","Type":"A","TTL":60,"ResourceRecords":[{"Value":"%s"}]}}]}' "$KEYCLOAK_HOSTNAME" "$IP" > /tmp/cb.json
    aws route53 change-resource-record-sets --hosted-zone-id "$ROUTE53_HOSTED_ZONE_ID" --change-batch file:///tmp/cb.json
  EOT
}

# ---- ECR ----
resource "aws_ecr_repository" "keycloak" {
  name                 = "${var.project}-keycloak"
  image_tag_mutability = "MUTABLE"
  force_delete         = true

  image_scanning_configuration {
    scan_on_push = true
  }
}

resource "aws_ecr_lifecycle_policy" "keycloak" {
  repository = aws_ecr_repository.keycloak.name
  policy = jsonencode({
    rules = [{
      rulePriority = 1
      description  = "keep last 5 images"
      selection    = { tagStatus = "any", countType = "imageCountMoreThan", countNumber = 5 }
      action       = { type = "expire" }
    }]
  })
}

# ---- ログ ----
resource "aws_cloudwatch_log_group" "keycloak" {
  name              = "/ecs/${var.project}-keycloak"
  retention_in_days = var.log_retention_days
}

# ---- 管理者パスワード（Secrets Manager） ----
resource "random_password" "keycloak_admin" {
  length  = 24
  special = false
}

resource "aws_secretsmanager_secret" "keycloak_admin" {
  name = "${var.project}-keycloak-admin"
}

resource "aws_secretsmanager_secret_version" "keycloak_admin" {
  secret_id     = aws_secretsmanager_secret.keycloak_admin.id
  secret_string = random_password.keycloak_admin.result
}

# ---- セキュリティグループ（CloudFront からのみ 8080 を許可） ----
resource "aws_security_group" "keycloak" {
  name        = "${var.project}-keycloak"
  description = "Keycloak task: allow 8080 from CloudFront only"
  vpc_id      = aws_vpc.this.id

  ingress {
    description     = "Keycloak HTTP from CloudFront"
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    prefix_list_ids = [data.aws_ec2_managed_prefix_list.cloudfront.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${var.project}-keycloak" }
}

# ---- Aurora 内に Keycloak 専用 DB を作成（RDS Data API 経由・冪等） ----
resource "null_resource" "keycloak_database" {
  triggers = {
    cluster = aws_rds_cluster.this.id
    db      = var.keycloak_db_name
  }

  provisioner "local-exec" {
    command = <<-EOT
      aws rds-data execute-statement \
        --region "${var.region}" \
        --resource-arn "${aws_rds_cluster.this.arn}" \
        --secret-arn "${aws_rds_cluster.this.master_user_secret[0].secret_arn}" \
        --database "${var.db_name}" \
        --sql "CREATE DATABASE ${var.keycloak_db_name}" || true
    EOT
  }

  depends_on = [aws_rds_cluster_instance.this]
}

# ---- タスク定義 ----
resource "aws_ecs_task_definition" "keycloak" {
  family                   = "${var.project}-keycloak"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = "512"
  memory                   = "1024"
  execution_role_arn       = aws_iam_role.ecs_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    # sidecar: 起動時に public IP を Route53(keycloak_hostname) へ UPSERT して正常終了する。
    {
      name       = "route53-register"
      image      = "public.ecr.aws/aws-cli/aws-cli:latest"
      essential  = false
      entryPoint = ["/usr/bin/sh", "-c"]
      command    = [local.keycloak_route53_script]
      environment = [
        { name = "ROUTE53_HOSTED_ZONE_ID", value = var.route53_zone_id },
        { name = "KEYCLOAK_HOSTNAME", value = var.keycloak_hostname }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.keycloak.name
          "awslogs-region"        = var.region
          "awslogs-stream-prefix" = "route53"
        }
      }
    },
    {
      name      = "keycloak"
      image     = "${aws_ecr_repository.keycloak.repository_url}:${var.keycloak_image_tag}"
      essential = true
      dependsOn = [
        { containerName = "route53-register", condition = "SUCCESS" }
      ]
      portMappings = [
        { containerPort = 8080, protocol = "tcp" }
      ]
      environment = [
        { name = "KC_DB", value = "postgres" },
        { name = "KC_DB_URL", value = "jdbc:postgresql://${aws_rds_cluster.this.endpoint}:5432/${var.keycloak_db_name}" },
        { name = "KC_DB_USERNAME", value = var.db_username },
        # 公開ベース URL（パス /auth を含める）。relative-path(/auth) はイメージにビルド時焼き込み済み。
        # → issuer = https://<cf>/auth/realms/sumika（SPA/backend と一致）。
        { name = "KC_HOSTNAME", value = "https://${aws_cloudfront_distribution.this.domain_name}/auth" },
        { name = "KC_PROXY_HEADERS", value = "xforwarded" },
        { name = "KC_HTTP_ENABLED", value = "true" },
        { name = "KC_BOOTSTRAP_ADMIN_USERNAME", value = var.keycloak_admin_username },
        { name = "SUMIKA_FRONTEND_URL", value = "https://${aws_cloudfront_distribution.this.domain_name}" }
      ]
      secrets = [
        { name = "KC_DB_PASSWORD", valueFrom = "${aws_rds_cluster.this.master_user_secret[0].secret_arn}:password::" },
        { name = "KC_BOOTSTRAP_ADMIN_PASSWORD", valueFrom = aws_secretsmanager_secret.keycloak_admin.arn }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.keycloak.name
          "awslogs-region"        = var.region
          "awslogs-stream-prefix" = "keycloak"
        }
      }
    }
  ])
}

# ---- サービス ----
resource "aws_ecs_service" "keycloak" {
  name            = "${var.project}-keycloak"
  cluster         = aws_ecs_cluster.this.id
  task_definition = aws_ecs_task_definition.keycloak.arn
  desired_count   = 1

  capacity_provider_strategy {
    capacity_provider = "FARGATE_SPOT"
    weight            = 1
  }

  network_configuration {
    subnets          = aws_subnet.public[*].id
    security_groups  = [aws_security_group.keycloak.id]
    assign_public_ip = true
  }

  lifecycle {
    ignore_changes = [task_definition, desired_count]
  }
}
