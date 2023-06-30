package hexlet.code.util;

import kong.unirest.HttpResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public final class ResponseParser {
    private Document body;
    private String title;
    private String h1;
    private String description;
    private int statusCode;
    public ResponseParser(HttpResponse<String> response) {
        body = Jsoup.parse(response.getBody());

        statusCode = response.getStatus();

        title = body.title();
        h1 = body.selectFirst("h1") != null ? body.selectFirst("h1").text() : null;
        description = body.selectFirst("meta[name=description]") != null
                ? body.selectFirst("meta[name=description]").attr("content") : null;
    }

    public String getTitle() {
        return title;
    }

    public String getH1() {
        return h1;
    }

    public String getDescription() {
        return description;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
