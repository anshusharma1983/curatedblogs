package com.curatedblogs.app.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anshu on 09/11/15.
 */
public class BlogVO implements Serializable {
    private String title;
    private String article;
    private String fileURL;
    private String objectId;
    private String source;
    private String category;
    private Boolean bookmarked;

    public BlogVO(String title, String article, String fileURL, String source, String objectId, String category, Boolean bookmarked){
        this.title = title;
        this.article = article;
        this.fileURL = fileURL;
        this.source = source;
        this.objectId = objectId;
        this.category = category;
        this.bookmarked = bookmarked;
    }

    public static BlogVO getInstance(Blog blog) {
        return new BlogVO(blog.getTitle(), blog.getUrl(), blog.getFileURL(), blog.getSource(), blog.getObjectId(), blog.getCategory(), blog.isBookmarked());
    }

    public static List<BlogVO> initializeList(List<Blog> blogs){
        List<BlogVO> blogVOs = new ArrayList<>();
        for (Blog blog : blogs) {
            blogVOs.add(getInstance(blog));
        }
        return blogVOs;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public String getFileURL() {
        return fileURL;
    }

    public void setFileURL(String fileURL) {
        this.fileURL = fileURL;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(Boolean bookmarked) {
        this.bookmarked = bookmarked;
    }


}
