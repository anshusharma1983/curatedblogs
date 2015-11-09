package com.curatedblogs.app.domain;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Anshu on 09/11/15.
 */

public class BlogsWrapper implements Serializable {
    private List<BlogVO> blogs;

    public BlogsWrapper(List<BlogVO> blogs) {
        this.blogs = blogs;
    }
    public List<BlogVO> getBlogs() {
        return blogs;
    }
}
