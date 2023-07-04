package hexlet.code;

import hexlet.code.domain.Url;
import io.ebean.DB;
import io.ebean.Database;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public final class AppTest {
    private static Javalin app;
    private static String baseUrl;
    private static Url existingUrl;
    private static Database database;
    private static MockWebServer mockWebServer;

   @BeforeAll
    public static void beforeAll() throws IOException {
        app = App.getApp();
        app.start(8090);
        int port = app.port();
        baseUrl = "http://localhost:" + port;

        database = DB.getDefault();

       mockWebServer = new MockWebServer();
       mockWebServer.start();
    }

    @BeforeEach
    void beforeEach() {
        database.script().run("/truncate.sql");
        database.script().run("/seed-test-db.sql");
    }


    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    @Test
    void testIndex() {
        HttpResponse<String> response = Unirest.get(baseUrl).asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains("Page Analyzer");
    }
}
