package com.curatedblogs.app.domain;

import android.os.Parcelable;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.io.Serializable;

/**
 * Created by Anshu on 28/10/15.
 */
@ParseClassName("Blog")
public class Blog extends ParseObject implements Serializable {
    private String author;
    private String title;
    private String url;
    private String fileURL;
    private String source;
    private Boolean deleted;
    private boolean bookmarked = false;

    public String getCategory() {
        return getString("category");
    }

    public void setCategory(String category) {
        put("category", category);
    }

    private String category;

    public Boolean getDeleted() {
        return getBoolean("deleted");
    }

    public void setDeleted(Boolean deleted) {
        put("deleted", deleted);
    }

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


    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

}
