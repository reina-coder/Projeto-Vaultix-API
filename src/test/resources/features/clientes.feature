# language: pt
Funcionalidade: Gestão de Clientes
  Como administrador do Vaultix
  Eu quero cadastrar e consultar clientes
  Para garantir a rastreabilidade e conformidade LGPD (pilar ESG de Governança)

  Contexto:
    Dado que estou autenticado com o usuário "admin" e senha "admin123"

  Cenário: Listar clientes com sucesso
    Quando eu envio uma requisição GET autenticada para "/clientes"
    Então o status code da resposta deve ser 200
    E o corpo da resposta deve conter o campo "content"
    E o corpo da resposta deve validar o schema "page-cliente-response.json"

  Cenário: Criar cliente com dados válidos (caminho feliz)
    Quando eu crio um cliente com dados válidos e únicos
    Então o status code da resposta deve ser 201
    E o corpo da resposta deve conter o campo "id"
    E o corpo da resposta deve conter o campo "nome" com valor "Cliente Teste BDD"
    E o corpo da resposta deve validar o schema "cliente-response.json"


  Cenário: Criar cliente com CPF inválido (caminho negativo)
    Quando eu envio uma requisição POST autenticada para "/clientes" com o corpo:
      """
      {
        "nome": "Cliente Inválido",
        "email": "invalido@email.com",
        "cpf": "123",
        "telefone": "(11) 99999-0000"
      }
      """
    Então o status code da resposta deve ser 400
    E o corpo da resposta não deve conter o campo "id"
