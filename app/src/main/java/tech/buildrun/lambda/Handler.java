package tech.buildrun.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.Map;

public class Handler implements RequestHandler<Map<String, Object>, String> {
    private final CriarClienteLambda criarCliente = new CriarClienteLambda() {};
    private final ConsultaClienteLambda consultaCliente = new ConsultaClienteLambda() {};

    @Override
    public String handleRequest(Map<String, Object> input, Context context) {
        String operacao = (String) input.get("operacao");

        if ("criar".equals(operacao)) {
            return criarCliente.handleRequest(input, context);
        } else if ("consultar".equals(operacao)) {
            return consultaCliente.handleRequest(input, context);
        }

        return "Operação inválida. Use 'criar' ou 'consultar'";
    }
}