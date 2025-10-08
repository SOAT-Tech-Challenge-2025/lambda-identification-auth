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
  default     = "jdbc:postgresql://tc-psql-db.c0gz6og5payx.us-east-1.rds.amazonaws.com:5432/soat"
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

variable "lambda_jar_path" {
  description = "Caminho do fat JAR da Lambda"
  type        = string
}
