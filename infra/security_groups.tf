# CloudFront のオリジン向けマネージドプレフィックスリスト（オリジンへの inbound 制限に使う）
data "aws_ec2_managed_prefix_list" "cloudfront" {
  name = "com.amazonaws.global.cloudfront.origin-facing"
}

resource "aws_security_group" "ecs" {
  name        = "${var.project}-ecs"
  description = "ECS task: allow 8080 from CloudFront only"
  vpc_id      = aws_vpc.this.id

  ingress {
    description     = "App port from CloudFront"
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

  tags = { Name = "${var.project}-ecs" }
}

resource "aws_security_group" "aurora" {
  name        = "${var.project}-aurora"
  description = "Aurora: allow 5432 from ECS only"
  vpc_id      = aws_vpc.this.id

  ingress {
    description     = "Postgres from ECS (backend / keycloak)"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs.id, aws_security_group.keycloak.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${var.project}-aurora" }
}
