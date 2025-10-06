variable "region" {
  default = "us-east-1"
}

variable "tags" {
  default = {
    Environment = "PRD"
    Project     = "tc-infra"
  }
}

variable "db_url" {
  default = "aws_db_instance.tc_psql_db.endpoint"
}

variable "db_user" {
  default = "tcadmin"
}

variable "db_password" {
  default = "TcTech2025"
}