package tech.buildrun.lambda;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;

public class Handler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String JWT_SECRET = System.getenv("JWT_SECRET");
    private static final long EXPIRATION_TIME = 3600_000; // 1 hora

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent request,
            Context context) {
        try {
            if (request.getBody() == null || request.getBody().isBlank()) {
                return response(400, Map.of("message", "Body obrigatório"));
            }

            Map<String, String> body =
                    mapper.readValue(request.getBody(), Map.class);

            String username = body.get("username");

            if (username == null) {
                return response(400, Map.of("message", "username obrigatórios"));
            }

            // 🔐 AQUI você valida usuário/senha no banco
            // boolean valido = authService.validar(username, password);
            boolean valido = true; // EXEMPLO

            if (!valido) {
                return response(401, Map.of("message", "Credenciais inválidas"));
            }

            String token = gerarToken(username);

            return response(200, Map.of("token", token));

        } catch (Exception e) {
            try {
                return response(500, Map.of("message", "Erro ao gerar token"));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
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

    private APIGatewayProxyResponseEvent response(int status, Object body) throws Exception {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(status)
                .withHeaders(Map.of("Content-Type", "application/json"))
                .withBody(mapper.writeValueAsString(body));
    }
}