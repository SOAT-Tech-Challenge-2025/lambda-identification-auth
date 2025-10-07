package tech.buildrun.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import java.util.Map;

public class CriarClienteLambda implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse>, CriarClienteLambdaInterface {

    private final ClienteRepository repository = new ClienteRepository();
    private final Gson gson = new Gson();

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        try {
            Map<String, Object> body = gson.fromJson(event.getBody(), Map.class);

            String name = (String) body.get("nameClient");
            String document = (String) body.get("numberDocument");
            String email = (String) body.get("email");

            if (name == null || document == null || email == null) {
                return APIGatewayV2HTTPResponse.builder()
                        .withStatusCode(400)
                        .withBody("Erro: parâmetros 'nameClient', 'numberDocument' e 'email' são obrigatórios.")
                        .build();
            }

            if (repository.existeCliente(document)) {
                return APIGatewayV2HTTPResponse.builder()
                        .withStatusCode(409)
                        .withBody("Usuário já existe: " + document)
                        .build();
            }

            repository.criarCliente(name, email, document);

            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(201)
                    .withHeaders(Map.of("Content-Type", "application/json"))
                    .withBody("Usuário criado com sucesso")
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(500)
                    .withBody("Erro na criação: " + e.getMessage())
                    .build();
        }
    }
}
