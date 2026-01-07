package tech.buildrun.lambda;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HandlerTest {

    private final Handler handler = new Handler();
    private final Context context = new TestContext();

    @BeforeAll
    static void setupEnv() {
        // JWT_SECRET precisa ter no mínimo 32 caracteres
        setEnv("JWT_SECRET", "12345678901234567890123456789012");
    }

    /* ================= LOGIN ================= */


    @Test
    void deveFalharLoginSemUser() {
        APIGatewayV2HTTPEvent request = criarRequest("/login", "POST");
        request.setBody("{}");

        APIGatewayProxyResponseEvent response =
                handler.handleRequest(request, context);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("user é obrigatório"));
    }

    /* ================= ME ================= */

    @Test
    void deveFalharSemToken() {
        APIGatewayV2HTTPEvent request = criarRequest("/me", "GET");
        request.setHeaders(Map.of());

        APIGatewayProxyResponseEvent response =
                handler.handleRequest(request, context);

        assertEquals(401, response.getStatusCode());
    }

    /* ================= ROTA INVÁLIDA ================= */

    @Test
    void deveRetornar404ParaRotaInexistente() {
        APIGatewayV2HTTPEvent request = criarRequest("/invalid", "GET");

        APIGatewayProxyResponseEvent response =
                handler.handleRequest(request, context);

        assertEquals(404, response.getStatusCode());
    }

    /* ================= HELPERS ================= */

    private APIGatewayV2HTTPEvent criarRequest(String path, String method) {
        APIGatewayV2HTTPEvent request = new APIGatewayV2HTTPEvent();
        request.setRawPath(path);

        APIGatewayV2HTTPEvent.RequestContext.Http http =
                new APIGatewayV2HTTPEvent.RequestContext.Http();
        http.setMethod(method);

        APIGatewayV2HTTPEvent.RequestContext requestContext =
                new APIGatewayV2HTTPEvent.RequestContext();
        requestContext.setHttp(http);

        request.setRequestContext(requestContext);
        return request;
    }

    static class TestContext implements Context {

        @Override
        public LambdaLogger getLogger() {
            return new LambdaLogger() {
                @Override
                public void log(String message) {
                    // noop
                }

                @Override
                public void log(byte[] message) {
                    // noop
                }
            };
        }

        @Override public String getAwsRequestId() { return null; }
        @Override public String getLogGroupName() { return null; }
        @Override public String getLogStreamName() { return null; }
        @Override public String getFunctionName() { return null; }
        @Override public String getFunctionVersion() { return null; }
        @Override public String getInvokedFunctionArn() { return null; }
        @Override public CognitoIdentity getIdentity() { return null; }
        @Override public ClientContext getClientContext() { return null; }
        @Override public int getRemainingTimeInMillis() { return 0; }
        @Override public int getMemoryLimitInMB() { return 0; }
    }

    // Hack para setar variável de ambiente em testes
    private static void setEnv(String key, String value) {
        try {
            Map<String, String> env = System.getenv();
            var field = env.getClass().getDeclaredField("m");
            field.setAccessible(true);
            ((Map<String, String>) field.get(env)).put(key, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
