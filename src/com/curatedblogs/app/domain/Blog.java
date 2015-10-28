package com.curatedblogs.app.domain;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Anshu on 28/10/15.
 */
@ParseClassName("Blog")
public class Blog extends ParseObject {
    private String author;
    private String title;
    private String url;
    private String fileURL;
    private String source;

    public String getAuthor() {
        return getString("author");
    }

    public void setAuthor(String author) {
        put("author", author);
    }

    public String getTitle() {
        return getString("title");
    }

    public void setTitle(String title) {
        put("title", title);
    }

    public String getUrl() {
        return getString("url");
    }

    public void setUrl(String url) {
        put("url", url);
    }

    public String getFileURL() {
        return getString("fileURL");
    }

    public void setFileURL(String fileURL) {
        put("fileURL", fileURL);
    }

    public String getSource() {
        return getString("source");
    }

    public void setSource(String source) {
        put("source", source);
    }
}
