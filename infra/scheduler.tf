# 夜間停止: EventBridge Scheduler から ECS UpdateService で desiredCount を 0/1 に切替

data "aws_iam_policy_document" "scheduler_assume" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["scheduler.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "scheduler" {
  name               = "${var.project}-scheduler"
  assume_role_policy = data.aws_iam_policy_document.scheduler_assume.json
}

data "aws_iam_policy_document" "scheduler" {
  statement {
    actions   = ["ecs:UpdateService"]
    resources = [aws_ecs_service.backend.id, aws_ecs_service.keycloak.id]
  }
}

resource "aws_iam_role_policy" "scheduler" {
  name   = "update-ecs-service"
  role   = aws_iam_role.scheduler.id
  policy = data.aws_iam_policy_document.scheduler.json
}

resource "aws_scheduler_schedule" "stop" {
  name                         = "${var.project}-ecs-stop"
  schedule_expression          = var.stop_cron
  schedule_expression_timezone = var.schedule_timezone
  flexible_time_window {
    mode = "OFF"
  }
  target {
    arn      = "arn:aws:scheduler:::aws-sdk:ecs:updateService"
    role_arn = aws_iam_role.scheduler.arn
    input = jsonencode({
      Cluster      = aws_ecs_cluster.this.name
      Service      = aws_ecs_service.backend.name
      DesiredCount = 0
    })
  }
}

resource "aws_scheduler_schedule" "start" {
  name                         = "${var.project}-ecs-start"
  schedule_expression          = var.start_cron
  schedule_expression_timezone = var.schedule_timezone
  flexible_time_window {
    mode = "OFF"
  }
  target {
    arn      = "arn:aws:scheduler:::aws-sdk:ecs:updateService"
    role_arn = aws_iam_role.scheduler.arn
    input = jsonencode({
      Cluster      = aws_ecs_cluster.this.name
      Service      = aws_ecs_service.backend.name
      DesiredCount = 1
    })
  }
}

# Keycloak も同じ時刻で停止/起動（コスト最小）
resource "aws_scheduler_schedule" "keycloak_stop" {
  name                         = "${var.project}-keycloak-stop"
  schedule_expression          = var.stop_cron
  schedule_expression_timezone = var.schedule_timezone
  flexible_time_window {
    mode = "OFF"
  }
  target {
    arn      = "arn:aws:scheduler:::aws-sdk:ecs:updateService"
    role_arn = aws_iam_role.scheduler.arn
    input = jsonencode({
      Cluster      = aws_ecs_cluster.this.name
      Service      = aws_ecs_service.keycloak.name
      DesiredCount = 0
    })
  }
}

resource "aws_scheduler_schedule" "keycloak_start" {
  name                         = "${var.project}-keycloak-start"
  schedule_expression          = var.start_cron
  schedule_expression_timezone = var.schedule_timezone
  flexible_time_window {
    mode = "OFF"
  }
  target {
    arn      = "arn:aws:scheduler:::aws-sdk:ecs:updateService"
    role_arn = aws_iam_role.scheduler.arn
    input = jsonencode({
      Cluster      = aws_ecs_cluster.this.name
      Service      = aws_ecs_service.keycloak.name
      DesiredCount = 1
    })
  }
}
