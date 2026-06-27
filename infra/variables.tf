variable "region" {
  type    = string
  default = "ap-northeast-1"
}

variable "project" {
  type    = string
  default = "sumika-poc"
}

variable "vpc_cidr" {
  type    = string
  default = "10.0.0.0/16"
}

variable "route53_zone_id" {
  type        = string
  description = "API オリジン用の既存 Route53 ホステッドゾーン ID"
}

variable "api_hostname" {
  type        = string
  description = "API オリジンの FQDN（例: api-poc.example.com）。CloudFront のオリジン兼 ECS が起動時に UPSERT する先"
}

variable "github_repo" {
  type        = string
  default     = "horie-t/sumika"
  description = "OIDC で信頼する owner/repo"
}

variable "image_tag" {
  type        = string
  default     = "latest"
  description = "ECS タスクが参照する backend イメージタグ（CD が git sha で上書き）"
}

variable "aurora_min_acu" {
  type        = number
  default     = 0
  description = "Aurora Serverless v2 の最小 ACU（0 でアイドル時に自動一時停止）"
}

variable "aurora_max_acu" {
  type    = number
  default = 2
}

variable "db_name" {
  type    = string
  default = "sumika"
}

variable "db_username" {
  type    = string
  default = "sumika"
}

variable "schedule_timezone" {
  type    = string
  default = "Asia/Tokyo"
}

variable "stop_cron" {
  type        = string
  default     = "cron(0 22 * * ? *)"
  description = "夜間停止（desired=0）。schedule_timezone のローカル時刻"
}

variable "start_cron" {
  type        = string
  default     = "cron(0 9 * * ? *)"
  description = "朝起動（desired=1）。schedule_timezone のローカル時刻"
}

variable "log_retention_days" {
  type    = number
  default = 7
}
