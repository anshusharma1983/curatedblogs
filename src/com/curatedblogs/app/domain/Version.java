package com.curatedblogs.app.domain;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Anshu on 19/11/15.
 */
@ParseClassName("Version")
public class Version extends ParseObject{

    public int getVersion() {
        return getInt("version");
    }

    public void setVersion(int version) {
        put("version", version);
    }
}
