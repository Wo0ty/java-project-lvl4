package hexlet.code.util;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class UrlDetails {
    private String urlName;
    private String lastCheckTime;
    private String statusCode;

    public UrlDetails(Url url) {
        this.urlName = url.getName();

        List<UrlCheck> urlchecks = url.getUrlchecks();

        if (!urlchecks.isEmpty()) {
            UrlCheck lastUrlCheck = urlchecks.get(urlchecks.size() - 1);
            this.lastCheckTime = defineLastCheckTime(lastUrlCheck);
            this.statusCode = defineStatusCode(lastUrlCheck);
        }
    }

    private static String defineStatusCode(UrlCheck lastUrlCheck) {
        return Integer.toString(lastUrlCheck.getStatusCode());
    }

    private static String defineLastCheckTime(UrlCheck lastUrlCheck) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

        Instant createdAt = lastUrlCheck.getCreatedAt();
        return formatter.format(createdAt);
    }

    public String getLastCheckTime() {
        return lastCheckTime;
    }

    public String getStatusCode() {
        return statusCode;
    }
}
