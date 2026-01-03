package tech.buildrun.lambda;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Date;
import java.util.Map;

public class Handler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String DB_URL = System.getenv("DB_URL");
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");
    private static final String JWT_SECRET = System.getenv("JWT_SECRET");

    private static final long EXPIRATION_TIME = 3600_000; // 1 hora

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent request,
            Context context) {

        try {
            String method = request.getHttpMethod();
            String path   = request.getPath();

            context.getLogger().log("METHOD=" + method);
            context.getLogger().log("PATH=" + path);

            // 🔐 AUTH
            if ("POST".equals(method) && "/auth/token".equals(path)) {
                return gerarAuthToken(request);
            }

            Class.forName("org.postgresql.Driver");

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                // 👤 CRIAR CLIENTE
                if ("POST".equals(method) && "/clientes".equals(path)) {
                    return criarCliente(request, conn);
                }

                // 🔍 CONSULTAR CLIENTE
                if ("GET".equals(method) && path.startsWith("/clientes/")) {
                    return consultarCliente(path, conn);
                }

                return buildResponse(404, Map.of("message", "Endpoint não encontrado"));
            }

        } catch (Exception e) {
            context.getLogger().log("ERRO: " + e.getMessage());
            return buildResponse(500, Map.of("message", "Erro interno"));
        }
    }

    // ===================== CLIENTES =====================

    private APIGatewayProxyResponseEvent criarCliente(
            APIGatewayProxyRequestEvent request,
            Connection conn) throws Exception {

        if (request.getBody() == null || request.getBody().isBlank()) {
            return buildResponse(400, Map.of("message", "Body obrigatório"));
        }

        Map<String, String> body = mapper.readValue(request.getBody(), Map.class);

        String document = body.get("document");
        String name = body.get("name");
        String email = body.get("email");

        if (document == null || name == null || email == null) {
            return buildResponse(400, Map.of(
                    "message", "document, name e email são obrigatórios"
            ));
        }

        String sql = """
                INSERT INTO tb_cliente (nr_documento, nm_cliente, ds_email)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, document);
            ps.setString(2, name);
            ps.setString(3, email);
            ps.executeUpdate();

            return buildResponse(201, Map.of(
                    "message", "Cliente criado com sucesso",
                    "document", document
            ));

        } catch (SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                return buildResponse(409, Map.of("message", "Cliente já existe"));
            }

            throw e;
        }
    }

    private APIGatewayProxyResponseEvent consultarCliente(
            String path,
            Connection conn) throws Exception {

        // /clientes/123456
        String document = path.substring("/clientes/".length());

        String sql = """
                SELECT id, nr_documento, nm_cliente, ds_email
                FROM tb_cliente
                WHERE nr_documento = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, document);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return buildResponseObject(200, Map.of(
                        "id", rs.getLong("id"),
                        "document", rs.getString("nr_documento"),
                        "name", rs.getString("nm_cliente"),
                        "email", rs.getString("ds_email")
                ));
            } else {
                return buildResponse(404, Map.of("message", "Cliente não encontrado"));
            }
        }
    }

    // ===================== AUTH =====================

    private APIGatewayProxyResponseEvent gerarAuthToken(
            APIGatewayProxyRequestEvent request) throws Exception {

        if (request.getBody() == null || request.getBody().isBlank()) {
            return buildResponse(400, Map.of("message", "Body obrigatório"));
        }

        Map<String, String> body = mapper.readValue(request.getBody(), Map.class);

        String username = body.get("username");

        if (username == null) {
            return buildResponse(400, Map.of("message", "username obrigatório"));
        }

        // 🔒 validação real viria aqui
        String token = gerarToken(username);

        return buildResponse(200, Map.of("token", token));
    }

    private String gerarToken(String username) {

        if (JWT_SECRET == null || JWT_SECRET.length() < 32) {
            throw new IllegalStateException("JWT_SECRET deve ter no mínimo 32 caracteres");
        }

        SecretKey key = Keys.hmacShaKeyFor(
                JWT_SECRET.getBytes(StandardCharsets.UTF_8)
        );

        return Jwts.builder()
                .setSubject(username)
                .claim("role", "USER")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ===================== RESPONSE =====================

    private APIGatewayProxyResponseEvent buildResponse(int statusCode, Object body) {
        try {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(statusCode)
                    .withHeaders(Map.of(
                            "Content-Type", "application/json",
                            "Access-Control-Allow-Origin", "*",
                            "Access-Control-Allow-Headers", "Content-Type,Authorization",
                            "Access-Control-Allow-Methods", "GET,POST,OPTIONS"
                    ))
                    .withBody(mapper.writeValueAsString(body));
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("{\"message\":\"Erro ao serializar resposta\"}");
        }
    }

    private APIGatewayProxyResponseEvent buildResponseObject(int statusCode, Object body) {
        return buildResponse(statusCode, body);
    }
}
