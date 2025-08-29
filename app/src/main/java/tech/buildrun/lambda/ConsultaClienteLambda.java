package tech.buildrun.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.Map;

public abstract class ConsultaClienteLambda implements RequestHandler<Map<String, Object>, String>, ConsultaClienteLambdaInterface {

    private final ClienteRepository repository = new ClienteRepository();

    @Override
    public String handleRequest(Map<String, Object> input, Context context) {
        String document = (String) input.get("document");

        if (document == null) {
            return "Erro: parâmetro 'document' é obrigatório.";
        }

        try {
            boolean existe = repository.existeCliente(document);
            return existe ? "Cliente existe:" : "Cliente não encontrado";
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro na consulta: " + e.getMessage();
        }
    }
}
