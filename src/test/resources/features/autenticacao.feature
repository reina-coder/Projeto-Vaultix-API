# language: pt
Funcionalidade: Autenticação de usuários
  Como usuário do sistema Vaultix
  Eu quero me autenticar e obter um token JWT
  Para acessar os recursos protegidos da API de seguro de identidade digital (ESG - Governança e Compliance)

  Cenário: Login com credenciais válidas (caminho feliz)
    Dado que o usuário "admin" existe no sistema com a senha "admin123"
    Quando eu envio uma requisição POST para "/auth/login" com o corpo:
      """
      {
        "username": "admin",
        "password": "admin123"
      }
      """
    Então o status code da resposta deve ser 200
    E o corpo da resposta deve conter o campo "token"
    E o corpo da resposta deve conter o campo "tokenType" com valor "Bearer"
    E o corpo da resposta deve validar o schema "token-response.json"

  Cenário: Login com credenciais inválidas (caminho negativo)
    Quando eu envio uma requisição POST para "/auth/login" com o corpo:
      """
      {
        "username": "admin",
        "password": "senha_errada"
      }
      """
    Então o status code da resposta deve ser 401
    E o corpo da resposta não deve conter o campo "token"

  Cenário: Registro de novo usuário com dados válidos
    Quando eu registro um novo usuário com username único e senha "senha123"
    Então o status code da resposta deve ser 201
    E o corpo da resposta deve conter o campo "token"
    E o corpo da resposta deve validar o schema "token-response.json"


