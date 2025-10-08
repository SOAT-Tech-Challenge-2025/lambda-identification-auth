# Região da AWS
variable "region" {
  default = "us-east-1"
}

# Tags para os recursos
variable "tags" {
  default = {
    Environment = "PRD"
    Project     = "tc-infra"
  }
}

# Banco de dados
variable "db_url" {
  description = "URL do banco de dados PostgreSQL"
  default     = "aws_db_instance.tc_psql_db.endpoint"
}

variable "db_user" {
  description = "Usuário do banco de dados"
  default     = "tcadmin"
}

variable "db_password" {
  description = "Senha do banco de dados"
  default     = "TcTech2025"
  sensitive   = true
}

# Caminho do arquivo ZIP da Lambda
variable "lambda_zip_path" {
  description = "Caminho do arquivo ZIP da AWS Lambda"
  type        = string
}
