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

    public BlogVO(String title, String article, String fileURL){
        this.title = title;
        this.article = article;
        this.fileURL = fileURL;
    }

    public static BlogVO getInstance(Blog blog) {
        return new BlogVO(blog.getTitle(), blog.getUrl(), blog.getFileURL());
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


}
