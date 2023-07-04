package hexlet.code.controller;

import io.javalin.http.Handler;

public final class RootController {

    public static Handler getIndex() {
        return index;
    }

    private static Handler index = ctx -> {
        ctx.render("index.html");
    };
}
