data "aws_iam_role" "lambda_exec_role" {
  name = "tc-infra-id-lambda-exec-role"
}

data "aws_security_group" "id_lambda" {
  name  = "tc-id-lambda-sg"
  vpc_id = data.aws_vpc.tc_lambda_vpc.id
}

resource "aws_lambda_function" "id_lambda" {
  function_name = "lambda-identification-auth"
  depends_on    = []
  role          = data.aws_iam_role.lambda_exec_role.arn
  handler       = "tech.buildrun.lambda.Handler::handleRequest"
  runtime       = "java17"

  timeout       = 6

  # Usa o caminho passado via variável
  filename         = var.lambda_jar_path
  source_code_hash = filebase64sha256(var.lambda_jar_path)

  environment {
    variables = {
      DB_URL      = local.jdbc_url
      DB_USER     = var.db_user
      DB_PASSWORD = var.db_password
      JWT_SECRET  = var.jwt_secret
    }
  }
  vpc_config {
    subnet_ids         = data.aws_subnets.tc_lambda_subnets.ids
    security_group_ids = [data.aws_security_group.id_lambda.id]
  }

  tags = var.tags
}

resource "aws_lambda_permission" "apigw_invoke_lambda" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.id_lambda.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${data.aws_apigatewayv2_api.tc_api.execution_arn}/*/*"
}
