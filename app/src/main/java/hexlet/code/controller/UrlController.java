package hexlet.code.controller;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class UrlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlController.class);

    public static final Handler URL_LIST = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;
        final int rowsPerPage = 10;

        LOGGER.info("Request URL list");
        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(page * rowsPerPage)
                .setMaxRows(rowsPerPage)
                .orderBy().id.asc()
                .findPagedList();

        List<Url> urls = pagedUrls.getList();

        int lastPage = pagedUrls.getTotalPageCount() + 1;
        int currentPage = pagedUrls.getPageIndex() + 1;

        List<Integer> pages = IntStream
                .range(1, lastPage)
                .boxed()
                .collect(Collectors.toList());

        Map<Long, UrlCheck> urlChecks = new QUrlCheck()
                .url.id.asMapKey()
                .orderBy()
                    .createdAt.desc()
                .findMap();

        ctx.attribute("pages", pages);
        ctx.attribute("currentPage", currentPage);
        ctx.attribute("urlChecks", urlChecks);
        ctx.attribute("urls", urls);

        ctx.render("urls.html");
    };

    public static final Handler ADD_URL = ctx -> {
        String name = ctx.formParam("url");
        String checkedUrl = "";
        URL parsedUrl;

        LOGGER.info("Start adding URL '{}' to the database", name);

        try {
            parsedUrl = new URL(name);
        } catch (MalformedURLException e) {
            LOGGER.info("Invalid URL");
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }

        checkedUrl = parsedUrl.getProtocol() + "://" +  parsedUrl.getHost();

        if (parsedUrl.getPort() > 0) {
            checkedUrl = checkedUrl + ":" +  parsedUrl.getPort();
        }

        Url url = new Url(checkedUrl);
        Url dbUrl = new QUrl()
                .name.equalTo(checkedUrl)
                .findOne();

        if (dbUrl != null) {
            LOGGER.info("URL '{}' has already added to the database", checkedUrl);
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.attribute("url", url);
            ctx.redirect("/urls");
            return;
        }

        url.save();
        LOGGER.info("URL '{}' successfully added to the database", checkedUrl);
        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls");
    };

    public static final Handler LIST_URLS = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);

        LOGGER.info("Preparing page for url with ID {} ", id);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (url == null) {
            throw new NotFoundResponse();
        }

        LOGGER.info("Request URL check list");
        List<UrlCheck> urlChecks = new QUrlCheck()
                .url.equalTo(url)
                .orderBy().id.asc()
                .findList();

        LOGGER.info("Found {} url checks for ID {} ", urlChecks.size(), id);

        ctx.attribute("url", url);
        ctx.attribute("urlChecks", urlChecks);
        ctx.render("show.html");
    };

    public static final Handler CHECK_URL = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);

        LOGGER.info("Checking URL with ID {}", id);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (url == null) {
            throw new NotFoundResponse();
        }

        try {
            LOGGER.info("GET request to {}", url.getName());
            HttpResponse<String> response = Unirest
                    .get(url.getName())
                    .asString();

            Document body = Jsoup.parse(response.getBody());

            int statusCode = response.getStatus();
            String title = body.title();

            Element headerElement = body.selectFirst("h1");
            String h1 = headerElement != null ? headerElement.text() : "";

            Element descriptionElement = body.selectFirst("meta[name=description]");
            String description = descriptionElement != null ? descriptionElement.attr("content") : "";

            UrlCheck checkResult = new UrlCheck(statusCode, title, h1, description, url);

            LOGGER.info("URL {} was checked, saving parsed data to database", url.getName());
            checkResult.save();

            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flash-type", "success");
        } catch (UnirestException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
        } catch (Exception e) {
            ctx.sessionAttribute("flash", e.getMessage());
            ctx.sessionAttribute("flash-type", "danger");
        }

        ctx.redirect("/urls/" + id);
    };
}
