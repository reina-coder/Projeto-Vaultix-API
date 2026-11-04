# language: pt
Funcionalidade: Catálogo de Planos de Assinatura
  Como usuário autenticado do Vaultix
  Eu quero consultar e gerenciar planos
  Para oferecer opções de cobertura de identidade digital alinhadas à sustentabilidade e compliance

  Contexto:
    Dado que estou autenticado com o usuário "admin" e senha "admin123"

  Cenário: Listar planos disponíveis
    Quando eu envio uma requisição GET autenticada para "/planos"
    Então o status code da resposta deve ser 200
    E o corpo da resposta deve conter o campo "content"
    E o corpo da resposta deve validar o schema "page-plano-response.json"

  Cenário: Buscar plano por ID existente
    Quando eu envio uma requisição GET autenticada para "/planos/1"
    Então o status code da resposta deve ser 200
    E o corpo da resposta deve conter o campo "id" com valor 1
    E o corpo da resposta deve validar o schema "plano-response.json"

  Cenário: Buscar plano inexistente (caminho negativo)
    Quando eu envio uma requisição GET autenticada para "/planos/99999"
    Então o status code da resposta deve ser 404
