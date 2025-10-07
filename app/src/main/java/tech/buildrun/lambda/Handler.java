package tech.buildrun.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class Handler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final String DB_URL = System.getenv("DB_URL");
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        String method = event.getRequestContext().getHttp().getMethod();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            if ("GET".equalsIgnoreCase(method)) {
                return consultarCliente(conn, event.getBody());
            } else if ("POST".equalsIgnoreCase(method)) {
                return criarCliente(conn, event.getBody());
            } else {
                // Retorna JSON mesmo para métodos não permitidos
                return buildResponse(405, Map.of("message", "Método não permitido"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Retorna JSON para erros internos
            return buildResponse(500, Map.of("message", "Erro interno: " + e.getMessage()));
        }
    }

    // Consulta cliente pelo documento
    private APIGatewayV2HTTPResponse consultarCliente(Connection conn, String body) throws Exception {
        Map<String, String> request = mapper.readValue(body, HashMap.class);
        String document = request.get("document");

        String sql = "SELECT id FROM cliente WHERE document = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, document);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return buildResponse(200, Map.of("message", "Cliente encontrado"));
            } else {
                return buildResponse(404, Map.of("message", "Cliente não encontrado"));
            }
        }
    }

    // Cria um novo cliente
    private APIGatewayV2HTTPResponse criarCliente(Connection conn, String body) throws Exception {
        Map<String, String> request = mapper.readValue(body, HashMap.class);
        String document = request.get("document");
        String name = request.get("name");
        String email = request.get("email");

        String sql = "INSERT INTO cliente (document, name, email) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, document);
            ps.setString(2, name);
            ps.setString(3, email);
            ps.executeUpdate();
            return buildResponse(201, Map.of("message", "Cliente criado com sucesso"));
        } catch (SQLException e) {
            e.printStackTrace();
            return buildResponse(500, Map.of("message", "Erro ao criar cliente"));
        }
    }

    // Método helper para criar a resposta JSON
    private APIGatewayV2HTTPResponse buildResponse(int statusCode, Map<String, String> body) {
        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
        try {
            response.setBody(mapper.writeValueAsString(body));
        } catch (Exception e) {
            response.setBody("{\"message\":\"Erro ao processar resposta\"}");
        }
        response.setStatusCode(statusCode);
        response.setHeaders(Map.of("Content-Type", "application/json"));
        return response;
    }
}
