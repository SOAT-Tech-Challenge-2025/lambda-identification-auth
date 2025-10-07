//package tech.buildrun.lambda;
//
//import com.amazonaws.services.lambda.runtime.Context;
//import com.amazonaws.services.lambda.runtime.RequestHandler;
//import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
//import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
//import com.google.gson.Gson;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class ConsultaClienteLambda implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
//
//    private final ClienteRepository repository = new ClienteRepository();
//    private final Gson gson = new Gson();
//
//    @Override
//    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
//        try {
//            Map<String, Object> body = gson.fromJson(event.getBody(), Map.class);
//            String document = (String) body.get("document");
//
//            if (document == null || document.isEmpty()) {
//                return APIGatewayV2HTTPResponse.builder()
//                        .withStatusCode(400)
//                        .withBody("Erro: parâmetro 'document' é obrigatório.")
//                        .build();
//            }
//
//            boolean existe = repository.existeCliente(document);
//
//            Map<String, Object> responseBody = new HashMap<>();
//            responseBody.put("mensagem", existe ? "Cliente encontrado" : "Cliente não encontrado");
//            responseBody.put("document", document);
//
//            return APIGatewayV2HTTPResponse.builder()
//                    .withStatusCode(200)
//                    .withBody(gson.toJson(responseBody))
//                    .withHeaders(Map.of("Content-Type", "application/json"))
//                    .build();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            Map<String, String> errorResponse = Map.of("erro", e.getMessage());
//
//            return APIGatewayV2HTTPResponse.builder()
//                    .withStatusCode(500)
//                    .withBody(gson.toJson(errorResponse))
//                    .withHeaders(Map.of("Content-Type", "application/json"))
//                    .build();
//        }
//    }
//}
