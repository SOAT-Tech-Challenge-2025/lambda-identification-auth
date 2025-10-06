resource "aws_iam_role" "lambda_exec_role" {
  name = "tc-infra-id-lambda-exec-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_logs" {
  role       = aws_iam_role.lambda_exec_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_lambda_function" "id_lambda" {
  function_name = "lambda-identification-auth"
  depends_on    = [aws_iam_role_policy_attachment.lambda_logs]
  role          = aws_iam_role.lambda_exec_role.arn
  handler       = "tech.buildrun.lambda.Handler::handleRequest"
  runtime       = "java17"

  filename         = "lambda-identification-auth.zip"
  source_code_hash = filebase64sha256("lambda-identification-auth.zip")

  environment {
    variables = {
      DB_URL      = var.db_url
      DB_USER     = var.db_user
      DB_PASSWORD = var.db_password
    }
  }
}
