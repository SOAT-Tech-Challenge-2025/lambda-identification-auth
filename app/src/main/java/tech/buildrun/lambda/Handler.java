package tech.buildrun.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

public class Handler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final CriarClienteLambda criarCliente = new CriarClienteLambda();
    private final ConsultaClienteLambda consultaCliente = new ConsultaClienteLambda();

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        String operacao = event.getQueryStringParameters() != null
                ? event.getQueryStringParameters().get("operacao")
                : null;

        if (operacao == null) {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(400)
                    .withBody("Erro: parâmetro 'operacao' é obrigatório na query string.")
                    .build();
        }

        try {
            if ("criar".equalsIgnoreCase(operacao)) {
                return criarCliente.handleRequest(event, context);
            } else if ("consultar".equalsIgnoreCase(operacao)) {
                return consultaCliente.handleRequest(event, context);
            } else {
                return APIGatewayV2HTTPResponse.builder()
                        .withStatusCode(400)
                        .withBody("Operação inválida. Use 'criar' ou 'consultar'.")
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(500)
                    .withBody("Erro interno: " + e.getMessage())
                    .build();
        }
    }
}
