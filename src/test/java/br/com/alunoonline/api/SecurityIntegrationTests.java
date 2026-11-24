package br.com.alunoonline.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "api.security.token.secret=segredo-exclusivo-dos-testes")
@AutoConfigureMockMvc
class SecurityIntegrationTests {

    private static final String TEST_SECRET = "segredo-exclusivo-dos-testes";
    private static final String PASSWORD = "senha-de-teste-123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void cadastroPersisteSenhaComBCryptSemExporHashNaResposta() throws Exception {
        String login = uniqueLogin();

        MvcResult result = register(login)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.login").value(login))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(cookie().doesNotExist("JSESSIONID"))
                .andReturn();

        String storedPassword = jdbcTemplate.queryForObject(
                "SELECT senha FROM usuarios WHERE login = ?", String.class, login);
        assertThat(storedPassword).isNotEqualTo(PASSWORD);
        assertThat(new BCryptPasswordEncoder().matches(PASSWORD, storedPassword)).isTrue();
        assertThat(result.getRequest().getSession(false)).isNull();
    }

    @Test
    void cadastroRejeitaLoginDuplicadoSemCriarOutraConta() throws Exception {
        String login = uniqueLogin();
        register(login).andExpect(status().isCreated());

        register(login).andExpect(status().isBadRequest());

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM usuarios WHERE login = ?", Long.class, login))
                .isEqualTo(1L);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"login\":\"   \",\"senha\":\"senha-de-teste-123\"}",
            "{\"login\":\"login-valido\",\"senha\":\"   \"}",
            "{\"senha\":\"senha-de-teste-123\"}",
            "{\"login\":\"login-valido\"}"
    })
    void cadastroRejeitaCredenciaisVaziasOuAusentes(String body) throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cadastroComSenhaMaiorQue72BytesRetorna400() throws Exception {
        String password = "\uD83D\uDE00".repeat(19);

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(uniqueLogin(), password)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginRetornaJwtSemCriarSessao() throws Exception {
        String login = uniqueLogin();
        register(login).andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(login, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(cookie().doesNotExist("JSESSIONID"))
                .andReturn();

        String token = JsonPath.read(result.getResponse().getContentAsString(), "$.token");
        assertThat(token.split("\\.")).hasSize(3);
        assertThat(JWT.decode(token).getExpiresAt()).isAfter(new Date());
        assertThat(result.getRequest().getSession(false)).isNull();
    }

    @Test
    void loginRejeitaSenhaIncorretaEUsuarioInexistente() throws Exception {
        String login = uniqueLogin();
        register(login).andExpect(status().isCreated());

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(login, "senha-incorreta")))
                .andExpect(status().isUnauthorized())
                .andExpect(cookie().doesNotExist("JSESSIONID"));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(uniqueLogin(), PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginComSenhaMaiorQue72BytesRetorna401() throws Exception {
        String login = uniqueLogin();
        register(login).andExpect(status().isCreated());

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(login, "a".repeat(73))))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/alunos", "/professores", "/disciplina", "/matriculas",
            "/dashboard/kpis", "/relatorios/metadata"
    })
    void apiAcademicaExigeAutenticacao(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isUnauthorized())
                .andExpect(cookie().doesNotExist("JSESSIONID"));
    }

    @Test
    void bearerValidoPermiteConsultarAlunosSemSessao() throws Exception {
        String token = registerAndLogin();

        MvcResult result = mockMvc.perform(get("/alunos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(cookie().doesNotExist("JSESSIONID"))
                .andReturn();

        assertThat(result.getRequest().getSession(false)).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"nao-e-um-jwt", "abc.def.ghi"})
    void bearerMalformadoRetorna401(String token) throws Exception {
        mockMvc.perform(get("/alunos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void bearerComAssinaturaValidaMasExpiradoRetorna401() throws Exception {
        DecodedJWT original = JWT.decode(registerAndLogin());
        String unexpired = signedToken(original, Instant.now().plusSeconds(300), TEST_SECRET);
        String expired = signedToken(original, Instant.now().minusSeconds(300), TEST_SECRET);

        // A mesma identidade e assinatura sao aceitas antes de o token expirar.
        mockMvc.perform(get("/alunos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + unexpired))
                .andExpect(status().isOk());

        mockMvc.perform(get("/alunos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void bearerForjadoComOutraChaveRetorna401() throws Exception {
        DecodedJWT original = JWT.decode(registerAndLogin());
        String forged = signedToken(original, Instant.now().plusSeconds(300), "outra-chave");

        mockMvc.perform(get("/alunos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + forged))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void jwtValidoComPrefixoBasicRetorna401() throws Exception {
        String token = registerAndLogin();

        mockMvc.perform(get("/alunos")
                        .header(HttpHeaders.AUTHORIZATION, "Basic " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void bearerComAssinaturaValidaMasEmissorIncorretoRetorna401() throws Exception {
        DecodedJWT original = JWT.decode(registerAndLogin());
        String token = JWT.create()
                .withSubject(original.getSubject())
                .withIssuer("emissor-inesperado-dos-testes")
                .withExpiresAt(Date.from(Instant.now().plusSeconds(300)))
                .sign(Algorithm.HMAC256(TEST_SECRET));

        mockMvc.perform(get("/alunos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void bearerComAssinaturaValidaMasUsuarioInexistenteRetorna401() throws Exception {
        DecodedJWT original = JWT.decode(registerAndLogin());
        JWTCreator.Builder builder = JWT.create()
                .withSubject(uniqueLogin())
                .withExpiresAt(Date.from(Instant.now().plusSeconds(300)));
        if (original.getIssuer() != null) {
            builder.withIssuer(original.getIssuer());
        }
        String token = builder.sign(Algorithm.HMAC256(TEST_SECRET));

        mockMvc.perform(get("/alunos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void paginaDeRelatoriosPermiteIframeDaMesmaOrigem() throws Exception {
        mockMvc.perform(get("/relatorios.html"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"));
    }

    @Test
    void autenticacaoPreservaValidacaoDosEndpointsAcademicos() throws Exception {
        String token = registerAndLogin();

        mockMvc.perform(post("/disciplina")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void swaggerDocumentaBearerNasRotasProtegidasELiberaLoginECadastro() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes['bearer-key'].type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes['bearer-key'].scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes['bearer-key'].bearerFormat").value("JWT"))
                .andReturn();

        Map<String, Object> document = JsonPath.read(result.getResponse().getContentAsString(), "$");
        List<Map<String, Object>> protectedSecurity = effectiveSecurity(document, "/alunos", "get");
        assertThat(protectedSecurity).anySatisfy(requirement ->
                assertThat(requirement).containsKey("bearer-key"));
        assertThat(effectiveSecurity(document, "/login", "post")).isEmpty();
        assertThat(effectiveSecurity(document, "/usuarios", "post")).isEmpty();

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    private org.springframework.test.web.servlet.ResultActions register(String login) throws Exception {
        return mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(credentials(login, PASSWORD)));
    }

    private String registerAndLogin() throws Exception {
        String login = uniqueLogin();
        register(login).andExpect(status().isCreated());
        MvcResult result = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(login, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    private static String credentials(String login, String password) {
        return "{\"login\":\"%s\",\"senha\":\"%s\"}".formatted(login, password);
    }

    private static String uniqueLogin() {
        return "teste-" + UUID.randomUUID();
    }

    private static String signedToken(DecodedJWT original, Instant expiration, String secret) {
        JWTCreator.Builder builder = JWT.create()
                .withSubject(original.getSubject())
                .withExpiresAt(Date.from(expiration));
        if (original.getIssuer() != null) {
            builder.withIssuer(original.getIssuer());
        }
        return builder.sign(Algorithm.HMAC256(secret));
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> effectiveSecurity(
            Map<String, Object> document, String path, String method) {
        Map<String, Object> paths = (Map<String, Object>) document.get("paths");
        assertThat(paths).containsKey(path);
        Map<String, Object> pathItem = (Map<String, Object>) paths.get(path);
        assertThat(pathItem).containsKey(method);
        Map<String, Object> operation = (Map<String, Object>) pathItem.get(method);
        return (List<Map<String, Object>>) operation.getOrDefault(
                "security", document.getOrDefault("security", List.of()));
    }
}
