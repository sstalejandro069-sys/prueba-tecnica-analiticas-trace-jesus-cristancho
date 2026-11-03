package co.analiticas.trace;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pruebas de integración de punta a punta sobre la API y la documentación OpenAPI. */
@SpringBootTest
@AutoConfigureMockMvc
class AnaliticasTraceApplicationTests {

    @Autowired
    private MockMvc mvc;

    private String token;

    @BeforeEach
    void login() throws Exception {
        var body = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        token = JsonPath.read(body, "$.token");
    }

    @Test
    void invalidCredentialsReturn401WithApiError() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"mala\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Usuario o contraseña incorrectos."));
    }

    @Test
    void emptyCredentialsReturn400() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void protectedEndpointsRequireToken() throws Exception {
        mvc.perform(get("/api/analytics"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void uploadCsvThenQueryRecordsAndAnalytics() throws Exception {
        mvc.perform(multipart("/api/inspections/upload").file(sample("inspecciones.csv")).header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRows").value(20))
                .andExpect(jsonPath("$.validRows").value(20))
                .andExpect(jsonPath("$.errors.length()").value(0));

        mvc.perform(get("/api/inspections").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(20))
                .andExpect(jsonPath("$[0].status").value("Aprobado"));

        mvc.perform(get("/api/analytics").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecords").value(20))
                .andExpect(jsonPath("$.byLocation[0].label").value("Frente Norte"))
                .andExpect(jsonPath("$.summary").value(containsString("Frente Norte la zona con mayor actividad")));
    }

    @Test
    void uploadXlsxSample() throws Exception {
        mvc.perform(multipart("/api/inspections/upload").file(sample("inspecciones.xlsx")).header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.validRows").value(20));
    }

    @Test
    void uploadWithRowErrorsLoadsValidRowsAndReportsTheRest() throws Exception {
        mvc.perform(multipart("/api/inspections/upload").file(sample("inspecciones_con_errores.csv")).header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRows").value(7))
                .andExpect(jsonPath("$.validRows").value(3))
                .andExpect(jsonPath("$.errors.length()").value(4));
    }

    @Test
    void uploadWithWrongSchemaReturns422() throws Exception {
        mvc.perform(multipart("/api/inspections/upload").file(sample("inspecciones_esquema_invalido.csv")).header("Authorization", bearer()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("INVALID_FILE_CONTENT"))
                .andExpect(jsonPath("$.details[0]").value(containsString("Columnas faltantes")));
    }

    @Test
    void uploadUnsupportedExtensionReturns400() throws Exception {
        var file = new MockMultipartFile("file", "reporte.pdf", "application/pdf", "x".getBytes());
        mvc.perform(multipart("/api/inspections/upload").file(file).header("Authorization", bearer()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_FILE_TYPE"));
    }

    @Test
    void uploadWithoutFileReturns400() throws Exception {
        mvc.perform(multipart("/api/inspections/upload").header("Authorization", bearer()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("FILE_REQUIRED"));
    }

    @Test
    void openApiDocumentIsPublishedWithBearerSecurity() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Analíticas Trace API"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.paths['/api/inspections/upload'].post").exists());
    }

    private String bearer() {
        return "Bearer " + token;
    }

    private static MockMultipartFile sample(String name) throws Exception {
        return new MockMultipartFile("file", name, "application/octet-stream", Files.readAllBytes(Path.of("samples", name)));
    }
}
