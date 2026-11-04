package br.com.fiap.vaultix.bdd.steps;

import br.com.fiap.vaultix.bdd.RestAssuredConfig;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.InputStream;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class ApiSteps {

    private Response response;
    private String authToken;
    private RequestSpecification request;

    private RequestSpecification baseRequest() {
        return given().spec(RestAssuredConfig.getRequestSpec());
    }

    // ========== AUTENTICAÇÃO ==========

    @Dado("que o usuário {string} existe no sistema com a senha {string}")
    public void queOUsuarioExisteNoSistemaComASenha(String username, String password) {
        // Pré-condição: usuário seed (admin) já existe via Flyway V4
        // Nada a fazer em runtime
    }

    @Dado("que estou autenticado com o usuário {string} e senha {string}")
    public void queEstouAutenticadoComOUsuarioESenha(String username, String password) {
        Response loginResponse = baseRequest()
                .body("""
                      {
                        "username": "%s",
                        "password": "%s"
                      }
                      """.formatted(username, password))
                .when()
                .post("/auth/login");

        assertEquals(200, loginResponse.getStatusCode(),
                "Falha ao autenticar. Verifique se a API está rodando e o usuário seed existe.");
        this.authToken = loginResponse.jsonPath().getString("token");
        assertNotNull(authToken, "Token não retornado no login");
    }

    // ========== REQUISIÇÕES ==========

    @Quando("eu envio uma requisição POST para {string} com o corpo:")
    public void euEnvioUmaRequisicaoPOSTParaComOCorpo(String path, String body) {
        this.response = baseRequest()
                .body(body)
                .when()
                .post(path);
    }

    @Quando("eu registro um novo usuário com username único e senha {string}")
    public void euRegistroUmNovoUsuarioComUsernameUnicoESenha(String password) {
        String uniqueUsername = "usuario_bdd_" + System.currentTimeMillis();
        String body = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(uniqueUsername, password);
        this.response = baseRequest()
                .body(body)
                .when()
                .post("/auth/register");
    }


    @Quando("eu envio uma requisição GET autenticada para {string}")
    public void euEnvioUmaRequisicaoGETAutenticadaPara(String path) {
        this.response = baseRequest()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get(path);
    }

    @Quando("eu envio uma requisição POST autenticada para {string} com o corpo:")
    public void euEnvioUmaRequisicaoPOSTAutenticadaParaComOCorpo(String path, String body) {
        this.response = baseRequest()
                .header("Authorization", "Bearer " + authToken)
                .body(body)
                .when()
                .post(path);
    }

    @Quando("eu crio um cliente com dados válidos e únicos")
    public void euCrioUmClienteComDadosValidosEUnicos() {
        long ts = System.currentTimeMillis();
        // CPF de 11 dígitos único baseado em timestamp
        String cpf = String.format("%011d", ts % 100000000000L);
        String body = """
                {
                  "nome": "Cliente Teste BDD",
                  "email": "cliente.bdd.%d@email.com",
                  "cpf": "%s",
                  "telefone": "(11) 99999-0000"
                }
                """.formatted(ts, cpf);
        this.response = baseRequest()
                .header("Authorization", "Bearer " + authToken)
                .body(body)
                .when()
                .post("/clientes");
    }


    // ========== ASSERÇÕES ==========

    @Então("o status code da resposta deve ser {int}")
    public void oStatusCodeDaRespostaDeveSer(int statusCode) {
        assertEquals(statusCode, response.getStatusCode(),
                "Status code esperado: " + statusCode + " | Obtido: " + response.getStatusCode()
                        + "\nBody: " + response.asString());
    }

    @Então("o corpo da resposta deve conter o campo {string}")
    public void oCorpoDaRespostaDeveConterOCampo(String field) {
        assertNotNull(response.jsonPath().get(field),
                "Campo '" + field + "' não encontrado no body: " + response.asString());
    }

    @Então("o corpo da resposta não deve conter o campo {string}")
    public void oCorpoDaRespostaNaoDeveConterOCampo(String field) {
        Object value = response.jsonPath().get(field);
        assertTrue(value == null || (value instanceof String && ((String) value).isBlank()),
                "Campo '" + field + "' não deveria existir, mas foi encontrado: " + value);
    }

    @Então("o corpo da resposta deve conter o campo {string} com valor {string}")
    public void oCorpoDaRespostaDeveConterOCampoComValorString(String field, String expected) {
        String actual = response.jsonPath().getString(field);
        assertEquals(expected, actual,
                "Campo '" + field + "' esperado: " + expected + " | Obtido: " + actual);
    }

    @Então("o corpo da resposta deve conter o campo {string} com valor {int}")
    public void oCorpoDaRespostaDeveConterOCampoComValorInt(String field, int expected) {
        Integer actual = response.jsonPath().getInt(field);
        assertEquals(expected, actual,
                "Campo '" + field + "' esperado: " + expected + " | Obtido: " + actual);
    }

    @Então("o corpo da resposta deve validar o schema {string}")
    public void oCorpoDaRespostaDeveValidarOSchema(String schemaFile) {
        response.then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("schemas/" + schemaFile));
    }

    @Então("o corpo da resposta deve ser um array")
    public void oCorpoDaRespostaDeveSerUmArray() {
        assertTrue(response.jsonPath().getList("$") != null || response.asString().trim().startsWith("["),
                "Resposta esperada como array, mas recebido: " + response.asString());
    }
}
