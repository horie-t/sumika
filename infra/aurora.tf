resource "aws_db_subnet_group" "aurora" {
  name       = "${var.project}-aurora"
  subnet_ids = aws_subnet.private[*].id
}

resource "aws_rds_cluster" "this" {
  cluster_identifier = "${var.project}-aurora"
  engine             = "aurora-postgresql"
  engine_mode        = "provisioned"

  database_name   = var.db_name
  master_username = var.db_username
  # マスターパスワードは Secrets Manager で自動管理
  manage_master_user_password = true

  db_subnet_group_name   = aws_db_subnet_group.aurora.name
  vpc_security_group_ids = [aws_security_group.aurora.id]

  serverlessv2_scaling_configuration {
    min_capacity = var.aurora_min_acu
    max_capacity = var.aurora_max_acu
  }

  # PoC: バックアップ最小・削除時スナップショットなし
  backup_retention_period = 1
  skip_final_snapshot     = true
  apply_immediately       = true
  deletion_protection     = false
}

resource "aws_rds_cluster_instance" "this" {
  identifier         = "${var.project}-aurora-1"
  cluster_identifier = aws_rds_cluster.this.id
  instance_class     = "db.serverless"
  engine             = aws_rds_cluster.this.engine
  engine_version     = aws_rds_cluster.this.engine_version
}
