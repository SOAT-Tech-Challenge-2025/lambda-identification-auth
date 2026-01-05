package tech.buildrun.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import io.jsonwebtoken.Claims;
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

public class Handler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayProxyResponseEvent> {

    private static final String JWT_SECRET = System.getenv("JWT_SECRET");
    private static final long EXPIRATION_TIME = 3600_000; // 1 hora

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayV2HTTPEvent request,
            Context context) {

        try {
            String path = request.getRawPath();
            String method = request.getRequestContext()
                    .getHttp()
                    .getMethod();

            context.getLogger().log("PATH=" + path);
            context.getLogger().log("METHOD=" + method);

            if ("/login".equals(path) && "POST".equalsIgnoreCase(method)) {
                return login(request);
            }

            if ("/me".equals(path) && "GET".equalsIgnoreCase(method)) {
                return me(request);
            }

            return response(404, Map.of("message", "Rota não encontrada"));

        } catch (Exception e) {
            context.getLogger().log("ERRO: " + e.getMessage());
            try {
                return response(500, Map.of("message", "Erro interno"));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /* ================= LOGIN ================= */

    private APIGatewayProxyResponseEvent login(APIGatewayV2HTTPEvent request)
            throws Exception {

        Map<String, String> body =
                mapper.readValue(request.getBody(), Map.class);

        String username = body.get("user");

        if (username == null || username.isBlank()) {
            return response(400, Map.of("message", "user é obrigatório"));
        }

        String token = gerarToken(username);

        return response(200, Map.of("token", token));
    }

    /* ================= ME ================= */

    private APIGatewayProxyResponseEvent me(APIGatewayV2HTTPEvent request)
            throws Exception {

        String authHeader = request.getHeaders().get("authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return response(401, Map.of("message", "Token não informado"));
        }

        String token = authHeader.replace("Bearer ", "");

        Claims claims = validarToken(token);

        return response(200, Map.of(
                "user", claims.getSubject(),
                "role", claims.get("role")
        ));
    }

    /* ================= JWT ================= */

    private String gerarToken(String username) {

        validarSecret();

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

    private Claims validarToken(String token) {

        validarSecret();

        SecretKey key = Keys.hmacShaKeyFor(
                JWT_SECRET.getBytes(StandardCharsets.UTF_8)
        );

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private void validarSecret() {
        if (JWT_SECRET == null || JWT_SECRET.length() < 32) {
            throw new IllegalStateException("JWT_SECRET deve ter no mínimo 32 caracteres");
        }
    }

    /* ================= RESPONSE ================= */

    private APIGatewayProxyResponseEvent response(int status, Object body) throws Exception {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(status)
                .withHeaders(Map.of("Content-Type", "application/json"))
                .withBody(mapper.writeValueAsString(body));
    }
}
