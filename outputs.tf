output "api_gateway_url" {
  description = "URL do API Gateway"
  value       = "${aws_apigatewayv2_api.lambda_api.api_endpoint}/prod"
}

output "available_routes" {
  description = "Rotas disponíveis na API"
  value = [
    "GET /clientes/{id}",
    "POST /clientes"
  ]
}
