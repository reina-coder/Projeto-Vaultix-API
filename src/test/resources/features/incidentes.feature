# language: pt
Funcionalidade: Gestão de Incidentes de Segurança
  Como operador do Vaultix
  Eu quero registrar e acompanhar incidentes de identidade digital
  Para assegurar a governança, rastreabilidade e conformidade regulatória (ESG)

  Contexto:
    Dado que estou autenticado com o usuário "admin" e senha "admin123"

  Cenário: Listar incidentes
    Quando eu envio uma requisição GET autenticada para "/incidentes"
    Então o status code da resposta deve ser 200
    E o corpo da resposta deve conter o campo "content"
    E o corpo da resposta deve validar o schema "page-incidente-response.json"

  Cenário: Criar incidente de segurança (caminho feliz)
    Quando eu envio uma requisição POST autenticada para "/incidentes" com o corpo:
      """
      {
        "idCliente": 1,
        "tipoIncidente": "PHISHING",
        "descricao": "Tentativa de phishing detectada via e-mail falso de banco",
        "severidade": "MEDIA",
        "status": "ABERTO"
      }
      """
    Então o status code da resposta deve ser 201
    E o corpo da resposta deve conter o campo "id"
    E o corpo da resposta deve conter o campo "tipoIncidente" com valor "PHISHING"
    E o corpo da resposta deve validar o schema "incidente-response.json"

  Cenário: Criar incidente sem cliente (caminho negativo)
    Quando eu envio uma requisição POST autenticada para "/incidentes" com o corpo:
      """
      {
        "tipoIncidente": "HACKING_CONTA",
        "descricao": "Sem cliente associado",
        "severidade": "ALTA"
      }
      """
    Então o status code da resposta deve ser 400
