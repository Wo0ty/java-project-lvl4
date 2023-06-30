package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import hexlet.code.util.ResponseParser;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
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

        ctx.attribute("pages", pages);
        ctx.attribute("currentPage", currentPage);

        ctx.attribute("urls", urls);
        ctx.render("urls.html");
    };

    public static final Handler CREATE_URL = ctx -> {
        String name = ctx.formParam("url");
        String checkedUrl = "";

        try {
            URL aURL = new URL(name);
            checkedUrl = aURL.getProtocol() + "://" +  aURL.getHost();
            if (aURL.getPort() > 0) {
                checkedUrl = checkedUrl + ":" +  aURL.getPort();
            }
        } catch (Exception e) {
            LOGGER.info("Invalid URL");
            ctx.sessionAttribute("flash", "Incorrect URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.render("/index.html");
            return;
        }

        Url url = new Url(checkedUrl);
        Url dbUrl = new QUrl()
                .name.equalTo(checkedUrl)
                .findOne();

        if (dbUrl != null) {
            ctx.sessionAttribute("flash", "Url already exists");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.attribute("url", url);
            ctx.render("/index.html");
            return;
        }

        url.save();

        ctx.sessionAttribute("flash", "Page added successfully");
        ctx.sessionAttribute("flash-type", "success");
        ctx.render("/index.html");
    };

    public static final Handler SHOW_URL = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (url == null) {
            throw new NotFoundResponse();
        }

        List<UrlCheck> urlChecks = new QUrlCheck()
                .url.equalTo(url)
                .orderBy().id.asc()
                .findList();

        ctx.attribute("url", url);
        ctx.attribute("urlChecks", urlChecks);
        ctx.render("show.html");
    };

    public static final Handler CHECK_URL = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (url == null) {
            throw new NotFoundResponse();
        }

        try {
            HttpResponse<String> response = Unirest
                    .get(url.getName())
                    .asString();

            ResponseParser urlParser = new ResponseParser(response);

            int statusCode = urlParser.getStatusCode();
            String title = urlParser.getTitle();
            String h1 = urlParser.getH1();
            String description = urlParser.getDescription();

            UrlCheck checkResult = new UrlCheck(statusCode, title, h1, description, url);

            checkResult.save();

            ctx.sessionAttribute("flash", "Page verified successfully");
            ctx.sessionAttribute("flash-type", "success");
        } catch (UnirestException e) {
            ctx.sessionAttribute("flash", "Failed to check page");
            ctx.sessionAttribute("flash-type", "danger");
        }
        ctx.redirect("/urls/" + id);
    };
}
