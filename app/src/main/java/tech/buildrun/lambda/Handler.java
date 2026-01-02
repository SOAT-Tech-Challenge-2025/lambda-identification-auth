package tech.buildrun.lambda;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;

public class Handler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String DB_URL = System.getenv("DB_URL");
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");
    private static final String JWT_SECRET = System.getenv("JWT_SECRET");
    private static final long TOKEN_EXPIRATION_TIME = 3600000; // 1 hora em milissegundos

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        String method = input.getHttpMethod();
        String path = input.getPath();

        try {
            // Verificar a rota de autenticação
            if ("/auth".equalsIgnoreCase(path) && "POST".equalsIgnoreCase(method)) {
                return autenticarUsuario(input);
            }

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

    private APIGatewayProxyResponseEvent autenticarUsuario(APIGatewayProxyRequestEvent input) throws Exception {
        String body = input.getBody();

        if (body == null || body.isEmpty()) {
            return buildResponse(400, Map.of("message", "Body da requisição não pode estar vazio"));
        }

        Map<String, String> request = mapper.readValue(body, HashMap.class);
        String username = request.get("username");

        if (username == null || username.isEmpty()) {
            return buildResponse(400, Map.of("message", "Username é obrigatório"));
        }

        try {
            // Gerar o token JWT
            String token = gerarToken(username);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("message", "Token gerado com sucesso");

            return buildResponseObject(200, response);
        } catch (IllegalStateException e) {
            return buildResponse(500, Map.of("message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return buildResponse(500, Map.of("message", "Erro ao gerar token: " + e.getMessage()));
        }
    }

    private String gerarToken(String username) {
        if (JWT_SECRET == null || JWT_SECRET.isEmpty()) {
            throw new IllegalStateException("JWT_SECRET não configurada nas variáveis de ambiente");
        }

        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date expiryDate = new Date(nowMillis + TOKEN_EXPIRATION_TIME);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
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

    private APIGatewayProxyResponseEvent buildResponseObject(int statusCode, Object body) {
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
