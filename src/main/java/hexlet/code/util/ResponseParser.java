package hexlet.code.util;

import kong.unirest.HttpResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
        h1 = setH1();
        description = setDescription();
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

    private String setH1() {
        Element element = body.selectFirst("h1");
        if (element != null) {
            return element.text();
        }
        return null;
    }

    private String setDescription() {
        Element element = body.selectFirst("meta[name=description]");
        if (element != null) {
            return element.attr("content");
        }
        return null;
    }
}
