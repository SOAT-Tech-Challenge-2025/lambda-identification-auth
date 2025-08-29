package tech.buildrun.lambda;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.Map;

public abstract class CriarClienteLambda implements RequestHandler<Map<String, Object>, String>, CriarClienteLambdaInterface {

    private final ClienteRepository repository = new ClienteRepository();

    @Override
    public String handleRequest(Map<String, Object> input, Context context) {
        String name = (String) input.get("nameClient");
        String document = (String) input.get("numberDocument");
        String email = (String) input.get("email");

        if (name == null || document == null || email == null) {
            return "Erro: parâmetros 'name', 'document' e 'email' são obrigatórios.";
        }

        try {
            if (repository.existeCliente(document)) {
                return "Usuário já existe: " + document;
            }

            repository.criarCliente(name, email, document);
            return "Usuário criado com sucesso";
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro na criação: " + e.getMessage();
        }
    }
}
