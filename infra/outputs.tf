output "cloudfront_domain" {
  value       = aws_cloudfront_distribution.this.domain_name
  description = "フロント/アプリの公開 URL（https://<this>）"
}

output "cloudfront_distribution_id" {
  value = aws_cloudfront_distribution.this.id
}

output "frontend_bucket" {
  value = aws_s3_bucket.frontend.bucket
}

output "ecr_repository_url" {
  value = aws_ecr_repository.backend.repository_url
}

output "ecs_cluster" {
  value = aws_ecs_cluster.this.name
}

output "ecs_service" {
  value = aws_ecs_service.backend.name
}

output "api_hostname" {
  value = var.api_hostname
}

output "aurora_endpoint" {
  value = aws_rds_cluster.this.endpoint
}

output "github_deploy_role_arn" {
  value       = aws_iam_role.github_deploy.arn
  description = "GitHub Actions の OIDC で assume するロール ARN（deploy.yml の secrets/vars に設定）"
}
