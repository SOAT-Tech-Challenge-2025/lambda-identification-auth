resource "aws_apigatewayv2_api" "lambda_api" {
  name          = "identification-auth-api"
  protocol_type = "HTTP"
}

resource "aws_apigatewayv2_stage" "lambda_stage" {
  api_id     = aws_apigatewayv2_api.lambda_api.id
  name        = "prod"
  auto_deploy = true
}

resource "aws_apigatewayv2_integration" "lambda_backend" {
  api_id                 = aws_apigatewayv2_api.lambda_api.id
  integration_type       = "AWS_PROXY"
  integration_uri        = aws_lambda_function.id_lambda.invoke_arn
  integration_method     = "POST"
  payload_format_version = "2.0"
}

resource "aws_apigatewayv2_route" "get_cliente" {
  api_id    = aws_apigatewayv2_api.lambda_api.id
  route_key = "GET /clientes/{id}"
  target    = "integrations/${aws_apigatewayv2_integration.lambda_backend.id}"
}

resource "aws_apigatewayv2_route" "post_cliente" {
  api_id    = aws_apigatewayv2_api.lambda_api.id
  route_key = "POST /clientes"
  target    = "integrations/${aws_apigatewayv2_integration.lambda_backend.id}"
}

resource "aws_lambda_permission" "api_gw" {
  statement_id  = "AllowExecutionFromAPIGateway"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.id_lambda.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_apigatewayv2_api.lambda_api.execution_arn}/*/*"
}
