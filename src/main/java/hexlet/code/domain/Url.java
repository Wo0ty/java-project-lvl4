package hexlet.code.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import java.time.Instant;

@Entity
public final class Url extends Model {
    @Id @GeneratedValue
    private long id;

    private String name;

    @WhenCreated
    private Instant createdAt;

    public Url(String urlName) {
        this.name = urlName;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
