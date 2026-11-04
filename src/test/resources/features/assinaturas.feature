# language: pt
Funcionalidade: Gestão de Assinaturas
  Como usuário autenticado
  Eu quero vincular clientes a planos
  Para gerenciar a cobertura de identidade digital com rastreabilidade

  Contexto:
    Dado que estou autenticado com o usuário "admin" e senha "admin123"

  Cenário: Listar assinaturas
    Quando eu envio uma requisição GET autenticada para "/assinaturas"
    Então o status code da resposta deve ser 200
    E o corpo da resposta deve conter o campo "content"
    E o corpo da resposta deve validar o schema "page-assinatura-response.json"

  Cenário: Buscar assinaturas de um cliente
    Quando eu envio uma requisição GET autenticada para "/assinaturas/cliente/1"
    Então o status code da resposta deve ser 200
    E o corpo da resposta deve ser um array
