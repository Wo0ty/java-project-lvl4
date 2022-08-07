package hexlet.code.controllers;

import hexlet.code.model.Url;
import hexlet.code.model.query.QUrl;

import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;

import java.net.URL;
import java.util.List;

public class UrlController {
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
        List<Url> dbUrls = new QUrl()
                .findList();

        ctx.attribute("urls", dbUrls);
        ctx.render("urls/list.html");
    };

    private static Handler create = ctx -> {
        String name = ctx.formParam("url");
        String parsedUrl = "";
        try {
            URL aURL = new URL(name);
            parsedUrl = aURL.getProtocol() + "://" +  aURL.getHost();
            if (aURL.getPort() > 0) {
                parsedUrl = parsedUrl + ":" +  aURL.getPort();
            }
        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.render("urls/list.html");
            return;
        }

        Url url = new Url(parsedUrl);
        Url dbUrl = new QUrl()
                .name.equalTo(parsedUrl)
                .findOne();

        if (dbUrl != null) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.attribute("url", url);
            ctx.render("urls/list.html");
            return;
        }

        url.save();

        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flash-type", "success");
        ctx.render("urls/list.html");
    };
}
