package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class UrlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlController.class);

    public static Handler listUrl() {
        return list;
    }

    public static Handler createUrl() {
        return create;
    }

    public static Handler showUrl() {
        return show;
    }

    private static Handler show = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);

        Url dbUrl = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (dbUrl == null) {
            throw new NotFoundResponse();
        }

        ctx.attribute("url", dbUrl);
        ctx.render("urls/show.html");
    };

    private static Handler list = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;
        final int rowsPerPage = 10;

        LOGGER.info("Request URL list");
        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(page * rowsPerPage)
                .setMaxRows(rowsPerPage)
                .orderBy()
                .id.asc()
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

    private static Handler create = ctx -> {
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
}
