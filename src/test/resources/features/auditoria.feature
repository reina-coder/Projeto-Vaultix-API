# language: pt
Funcionalidade: Trilha de Auditoria de Compliance
  Como auditor ou administrador
  Eu quero consultar os registros de auditoria gerados automaticamente
  Para garantir integridade, rastreabilidade e conformidade LGPD (pilar ESG de Governança)

  Contexto:
    Dado que estou autenticado com o usuário "admin" e senha "admin123"

  Cenário: Listar registros de auditoria
    Quando eu envio uma requisição GET autenticada para "/auditoria"
    Então o status code da resposta deve ser 200
    E o corpo da resposta deve conter o campo "content"
    E o corpo da resposta deve validar o schema "page-auditoria-response.json"

  Cenário: Consultar auditoria de um cliente específico
    Quando eu envio uma requisição GET autenticada para "/auditoria/cliente/1"
    Então o status code da resposta deve ser 200
    E o corpo da resposta deve ser um array
