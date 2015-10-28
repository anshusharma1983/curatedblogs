package com.curatedblogs.app.common;

import android.app.Application;
import android.content.SharedPreferences;
import android.view.ViewConfiguration;
import android.widget.ImageView;

import com.curatedblogs.app.domain.Blog;
import com.facebook.AccessToken;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCrashReporting;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.pubnub.api.Pubnub;

import java.lang.reflect.Field;

public class BlogApplication extends Application {

    public static SharedPreferences preferences;
    static BlogApplication blogApplication;
    public ParseUser currentUser;
    Pubnub pubnub;
    private String currentPackageName;

    public synchronized static BlogApplication getInstance() {
        return blogApplication;
    }

    public ParseUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(ParseUser currentUser) {
        this.currentUser = currentUser;
    }

    public AccessToken getAccessToken() {
        return AccessToken.getCurrentAccessToken();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        blogApplication = this;
        ParseCrashReporting.enable(this);
        Parse.enableLocalDatastore(this);
        ParseUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);
        registerSubClasses();
        currentPackageName = getPackageName();
        Parse.initialize(this, "Hb2JQX1qTc5Jf6wNqLgTXSDFvP9xgrCHVgAkfKEv",
                "wZR8mpIeBYcqcvePn5bE1b3VwJmIe13PERyregDO");
        ParseInstallation.getCurrentInstallation().saveInBackground();

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }
    }

    private void registerSubClasses() {
        ParseObject.registerSubclass(Blog.class);
    }

    public String getCurrentPackageName() {
        return currentPackageName;
    }

    public void setCurrentPackageName(String currentPackageName) {
        this.currentPackageName = currentPackageName;
    }
}
