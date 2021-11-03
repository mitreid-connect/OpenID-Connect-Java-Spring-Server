package cz.muni.ics.oidc.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * AUP object model.
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>
 */
public class Aup {

    public static final String SIGNED_ON = "signed_on";

    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String version;
    private String date;
    private String link;
    private String text;

    @JsonProperty(SIGNED_ON)
    private String signedOn = null;

    public Aup() {
    }

    public Aup(String version, String date, String link, String text, String signedOn) {
        this.setVersion(version);
        this.setDate(date);
        this.setLink(link);
        this.setText(text);
        this.setSignedOn(signedOn);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        if (Strings.isNullOrEmpty(version)) {
            throw new IllegalArgumentException("version cannot be null or empty");
        }

        this.version = version;
    }

    public String getDate() {
        return date;
    }

    @JsonIgnore
    public LocalDate getDateAsLocalDate() {
        return LocalDate.parse(date, format);
    }

    public void setDate(String date) {
        if (Strings.isNullOrEmpty(date)) {
            throw new IllegalArgumentException("date cannot be null or empty");
        }

        this.date = date;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        if (Strings.isNullOrEmpty(link)) {
            throw new IllegalArgumentException("link cannot be null or empty");
        }

        this.link = link;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (text == null) {
            throw new IllegalArgumentException("version cannot be null");
        }

        this.text = text;
    }

    public String getSignedOn() {
        return signedOn;
    }

    public void setSignedOn(String signedOn) {
        this.signedOn = signedOn;
    }

    @Override
    public String toString() {
        return "Aup{" +
                "version='" + version + '\'' +
                ", date='" + date + '\'' +
                ", link='" + link + '\'' +
                ", text='" + text + '\'' +
                ", signedOn='" + signedOn + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Aup aup = (Aup) o;
        return Objects.equals(version, aup.version) &&
                Objects.equals(date, aup.date) &&
                Objects.equals(link, aup.link) &&
                Objects.equals(text, aup.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, date, link, text);
    }
}
