```markdown
# Lambda Identification Auth

## Descrição
Serviço AWS Lambda desenvolvido em Java para autenticação e identificação de clientes. Gerencia operações de consulta e criação de clientes em banco de dados relacional.

## Funcionalidades
- Consulta de existência de cliente por documento
- Criação de novo cliente
```
## Requisições

### Consultar Cliente
Verifica se um cliente existe pelo número do documento.

**Request:**
```json
{
    "document": "12345678900"
}
```

**Response (cliente existe):**
```json
{
    "message": "Cliente encontrado"
}
```

**Response (cliente não existe):**
```json
{
    "message": "Cliente não encontrado"
}
```

### Criar Cliente
Cadastra um novo cliente no sistema.

**Request:**
```json
{
    "document": "12345678900",
    "name": "João Silva",
    "email": "joao@email.com"
}
```

**Response (sucesso):**
```json
{
    "message": "Cliente criado com sucesso"
}
```

**Response (erro):**
```json
{
  "message": "Erro ao criar cliente"
}
```

## Configuração
A lambda requer as seguintes variáveis de ambiente:
- `DB_URL`: URL de conexão com o banco de dados
- `DB_USER`: Usuário do banco de dados
- `DB_PASSWORD`: Senha do banco de dados
```