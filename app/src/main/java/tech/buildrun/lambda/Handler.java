package tech.buildrun.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class Handler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String DB_URL = System.getenv("DB_URL");
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        String method = input.getHttpMethod();

        try {
            // Garantir que o driver PostgreSQL esteja carregado
            Class.forName("org.postgresql.Driver");

            // Conexão com o banco usando variáveis de ambiente
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

                if ("GET".equalsIgnoreCase(method)) {
                    return consultarCliente(input, conn);
                } else if ("POST".equalsIgnoreCase(method)) {
                    return criarCliente(conn, input.getBody());
                } else {
                    return buildResponse(405, Map.of("message", "Método não permitido"));
                }

            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return buildResponse(500, Map.of("message", "Driver JDBC não encontrado: " + e.getMessage()));
        } catch (SQLException e) {
            e.printStackTrace();
            return buildResponse(500, Map.of("message", "Erro de conexão com o banco: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return buildResponse(500, Map.of("message", "Erro interno: " + e.getMessage()));
        }
    }

    private APIGatewayProxyResponseEvent consultarCliente(APIGatewayProxyRequestEvent input, Connection conn) throws Exception {
        String body = input.getBody();
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

    private APIGatewayProxyResponseEvent criarCliente(Connection conn, String body) throws Exception {
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
            return buildResponse(500, Map.of("message", "Erro ao criar cliente: " + e.getMessage()));
        }
    }

    private APIGatewayProxyResponseEvent buildResponse(int statusCode, Map<String, String> body) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
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
