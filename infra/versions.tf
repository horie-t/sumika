terraform {
  required_version = ">= 1.6"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # PoC: state はローカル。必要になれば S3 backend に移行する。
}

provider "aws" {
  region = var.region

  default_tags {
    tags = {
      Project = var.project
      Managed = "terraform"
    }
  }
}
