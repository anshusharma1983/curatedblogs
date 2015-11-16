package com.curatedblogs.app.domain;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Anshu on 09/11/15.
 */

public class BlogsWrapper implements Serializable {
    private Boolean isBookmark;
    private List<BlogVO> blogs;

    public BlogsWrapper(List<BlogVO> blogs, Boolean isBookmark) {
        this.blogs = blogs;
        this.isBookmark = isBookmark;
    }
    public List<BlogVO> getBlogs() {
        return blogs;
    }
    public Boolean getIsBookmark() {
        return isBookmark;
    }

    public void setIsBookmark(Boolean isBookmark) {
        this.isBookmark = isBookmark;
    }


}
