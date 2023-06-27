package hexlet.code;

import hexlet.code.model.Url;
import io.ebean.DB;
import io.ebean.Transaction;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public final class AppTest {
    private static Javalin app;
    private static String baseUrl;
    private static Url existingUrl;
    private static Transaction transaction;
    private final int successStatus = 200;
    private final int foundStatus = 302;

    //@BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;

        existingUrl = new Url(baseUrl);
        existingUrl.save();
    }

    //@AfterAll
    public static void afterAll() {
        app.stop();
    }

    //@BeforeEach
    void beforeEach() {
        transaction = DB.beginTransaction();
    }

    //@AfterEach
    void afterEach() {
        transaction.rollback();
    }

    //@Nested
    class RootTest {
        @Test
        void testIndex() {
            HttpResponse<String> response = Unirest.get(baseUrl).asString();

            assertThat(response.getStatus()).isEqualTo(successStatus);
            assertThat(response.getBody()).contains("Анализатор страниц");
        }
    }

    //@Nested
    class UrlTest {
        //@Test
        void testIndex() {
            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls")
                    .asString();

            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(successStatus);
            assertThat(body).contains(existingUrl.getName());
        }

        //@Test
        void testShow() {
            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls/" + existingUrl.getId())
                    .asString();

            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(successStatus);
            assertThat(body).contains(existingUrl.getName());
        }

        //@Test
//        void testCreate() {
//            String inputName = "https://www.google.com/";
//            HttpResponse<String> responsePost = Unirest
//                    .post(baseUrl + "/urls")
//                    .field("url", inputName).;
//
//            assertThat(responsePost.getStatus()).isEqualTo(foundStatus);
//            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");
//
//            HttpResponse<String> response = Unirest
//                    .get(baseUrl + "/urls")
//                    .asString();
//            String body = response.getBody();
//
//            assertThat(response.getStatus()).isEqualTo(successStatus);
//            assertThat(body).contains(inputName);
//            assertThat(body).contains("Страница успешно добавлена");
//
//            Url actualUrl = new QUrl()
//                    .name.equalTo(inputName)
//                    .findOne();
//
//            assertThat(actualUrl).isNotNull();
//            assertThat(actualUrl.getName()).isEqualTo(inputName);
//        }
    }
}
