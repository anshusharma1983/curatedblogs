package com.curatedblogs.app.utils;

import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.SaveCallback;
import com.parse.SendCallback;

import java.util.List;

public class ParseUtility {

    public static void pushMessageToUser(String to, String msg) {
        String channel = getUserChannel(to);
        pushMessageToChannel(channel, msg);
    }


    public static void pushMessageToChannel(final String channel, final String msg) {
        ParsePush push = new ParsePush();
        push.setMessage(msg);
        push.setChannel(channel);
        push.sendInBackground(new SendCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Helpers.logDebug("Pushed message [" + msg + "], to channel [" + channel + "]");
                } else {
                    Helpers.logDebug(e.toString());
                }
            }
        });
    }

    public static void pushMessageToChannel(String channel, String msg, SendCallback sendCallback) {
        ParsePush push = new ParsePush();
        push.setMessage(msg);
        push.setChannel(channel);
        push.sendInBackground(sendCallback);
    }

    public static void subscribeToUserChannel(String channel) {
        channel = getUserChannel(channel);
        subscribeToChannel(channel);
    }

    public static void subscribeToChannel(final String channel) {
        Helpers.logDebug("Going to subscribe to:" + channel);
        ParsePush.subscribeInBackground(channel,
                new SaveCallback() {
                    @Override
                    public void done(
                            ParseException e) {
                        if (e == null) {
                            Helpers.logDebug("successfully subscribed to the channel:" + channel);
                        } else {
                            Helpers.logDebug("failed to subscribe for push:" + e);
                            e.printStackTrace();
                        }
                    }
                });
    }

    public static void unSubscribeFromChannel(final String channel) {
        Helpers.logDebug("Going to unsubscribe from channel:" + channel);
        ParsePush.unsubscribeInBackground(channel);
    }

    public static String getUserChannel(String user) {
        return "user_" + user.split("\\@")[0].replaceAll("\\.", "");
    }
}
