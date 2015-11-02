package com.curatedblogs.app.domain;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by Anshu on 01/11/15.
 */
@ParseClassName("Bookmark")
public class Bookmark extends ParseObject{
    private String blogObjectId;
    private String userId;

    public String getBlogObjectId() {
        return getString("blogObjectId");
    }

    public void setBlogObjectId(String blogObjectId) {
        put("blogObjectId", blogObjectId);
    }

    public String getUserId() {
        return getString("userId");
    }

    public void setUserId(String userId) {
        put("userId", userId);
    }
}
