package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.DB;
import io.ebean.Database;
import io.javalin.Javalin;
import kong.unirest.Empty;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public final class AppTest {
    private static Javalin app;
    private static String baseUrl;
    private static Database database;
    private static MockWebServer mockWebServer;
    public static final int REDIRECT_STATUS_CODE = 302;
    public static final int SUCCESS_STATUS_CODE = 200;

    @BeforeAll
    public static void beforeAll() throws IOException {
        final int port = 8088;
        app = App.getApp();
        app.start(port);
        baseUrl = "http://localhost:" + port;

        database = DB.getDefault();

        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @BeforeEach
     void beforeEach() {
        database.script().run("/truncate.sql");
    }


    @AfterAll
    public static void afterAll() throws IOException {
        app.stop();
        mockWebServer.shutdown();
    }

    @Test
    void testIndexPage() {
        HttpResponse<String> response = Unirest.get(baseUrl).asString();

        assertThat(response.getStatus()).isEqualTo(HttpURLConnection.HTTP_OK);
        assertThat(response.getBody()).contains("Анализатор страниц");
    }

    @Test
    void testShowListUrls() {
        database.script().run("/seed-test-db.sql");
        HttpResponse<String> response = Unirest.get(baseUrl + "/urls").asString();
        String body = response.getBody();

        assertThat(response.getStatus()).isEqualTo(HttpURLConnection.HTTP_OK);
        assertThat(body).contains("https://test.net");
        assertThat(body).contains("01/01/2022 12:00");
        assertThat(body).contains("200");
        assertThat(response.getBody()).contains("Анализатор страниц");
    }

    @Test
    void testAddNewUrl() {
        String mockUrl = mockWebServer.url("").toString().replaceAll("/$", "");

        HttpResponse<String> responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", mockUrl)
                .asString();

        assertThat(responsePost.getStatus()).isEqualTo(REDIRECT_STATUS_CODE);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

        Url url = new QUrl().name.equalTo(mockUrl).findOne();
        assertThat(url).isNotNull();

        HttpResponse<String> responseGet = Unirest
                .get(baseUrl + "/urls")
                .asString();

        String body = responseGet.getBody();
        assertThat(responseGet.getStatus()).isEqualTo(SUCCESS_STATUS_CODE);
        assertThat(body).contains(mockUrl);
        assertThat(body).contains("Страница успешно добавлена");
    }

    @Test
    void testAddNewCheck() throws IOException {
        String mockUrl = mockWebServer.url("").toString().replaceAll("/$", "");
        Path path = Paths.get("src", "test", "resources", "testPageResponse.html").toAbsolutePath().normalize();
        String mockBodyResponse = Files.readString(path);
        MockResponse mockResponse = new MockResponse()
                .setBody(mockBodyResponse)
                .setResponseCode(SUCCESS_STATUS_CODE);

        mockWebServer.enqueue(mockResponse);
        Url url = new Url(mockUrl);
        url.save();
        long id = url.getId();

        assertThat(url).isNotNull();

        HttpResponse<String> responseListUrls = Unirest
                .get(baseUrl + "/urls")
                .asString();

        assertThat(responseListUrls.getStatus()).isEqualTo(SUCCESS_STATUS_CODE);
        assertThat(responseListUrls.getBody()).contains(mockUrl);

        HttpResponse<Empty> responseAddNewCheck = Unirest
                .post(baseUrl + "/urls/" + id + "/checks")
                .asEmpty();

        List<UrlCheck> checkAfterAdding = new QUrlCheck()
                .id.equalTo(id)
                .findList();

        assertThat(checkAfterAdding.size()).isEqualTo(1);

        HttpResponse<String> responseGet = Unirest
                .get(baseUrl + "/urls/" + id)
                .asString();

        String body = responseGet.getBody();

        UrlCheck actualCheckUrl = new QUrlCheck().id.equalTo(id).findOne();

        assertThat(actualCheckUrl).isNotNull();
        assertThat(actualCheckUrl.getStatusCode()).isEqualTo(SUCCESS_STATUS_CODE);
        assertThat(actualCheckUrl.getDescription()).isEqualTo("test description");
        assertThat(actualCheckUrl.getTitle()).isEqualTo("test page");
    }

    @Test
    void testAddInvalidUrl() {
        List<Url> urlsBeforeRequest = new QUrl().findList();

        String invalidUrl = "wrong-url";
        HttpResponse<String> responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", invalidUrl)
                .asString();

        List<Url> urlsAfterRequest = new QUrl().findList();
        assertThat(responsePost.getStatus()).isEqualTo(REDIRECT_STATUS_CODE);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/");

        Assert.assertEquals(urlsBeforeRequest, urlsAfterRequest);
    }

    @Test
    void testAddExistingUrl() {
        String mockUrl = mockWebServer.url("").toString().replaceAll("/$", "");

        HttpResponse<String> responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", mockUrl)
                .asString();

        assertThat(responsePost.getStatus()).isEqualTo(REDIRECT_STATUS_CODE);

        Url url = new QUrl().name.equalTo(mockUrl).findOne();
        List<Url> urlsAfterFirstAdd = new QUrl().findList();

        HttpResponse<String> repeatResponse = Unirest
                .post(baseUrl + "/urls")
                .field("url", mockUrl)
                .asString();

        List<Url> urlsAfterSecondAdd = new QUrl().findList();
        Assert.assertEquals(urlsAfterFirstAdd, urlsAfterSecondAdd);

        assertThat(repeatResponse.getStatus()).isEqualTo(REDIRECT_STATUS_CODE);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");


        HttpResponse<String> responseGet = Unirest
                .get(baseUrl + "/urls")
                .asString();

        String body = responseGet.getBody();
        assertThat(responseGet.getStatus()).isEqualTo(SUCCESS_STATUS_CODE);
        assertThat(responseGet.getBody()).contains(mockUrl);
    }
}
