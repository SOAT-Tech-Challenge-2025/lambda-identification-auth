data "aws_iam_role" "lambda_exec_role" {
  name = "tc-infra-id-lambda-exec-role"
}


resource "aws_lambda_function" "id_lambda" {
  function_name = "lambda-identification-auth"
  role          = data.aws_iam_role.lambda_exec_role.arn
  handler       = "tech.buildrun.lambda.Handler::handleRequest"
  runtime       = "java17"

  # Usa o caminho passado via variável
  filename         = var.lambda_jar_path
  source_code_hash = filebase64sha256(var.lambda_jar_path)

  environment {
    variables = {
      DB_URL      = var.db_url
      DB_USER     = var.db_user
      DB_PASSWORD = var.db_password
    }
  }

  tags = var.tags
}
