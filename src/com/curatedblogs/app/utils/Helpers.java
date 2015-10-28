package com.curatedblogs.app.utils;


import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.curatedblogs.app.common.Constants;

import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Helpers {

    public static final DateFormat formatter = new SimpleDateFormat("EEE, MMM d hh:mm aaa");

    public static void logDebug(String message) {
        d(Constants.APP_TAG, message);
    }

    private static void d(String TAG, String message) {
        int maxLogSize = 1000;
        for (int index = 0; index <= message.length() / maxLogSize; index++) {
            int start = index * maxLogSize;
            int end = (index + 1) * maxLogSize;
            end = end > message.length() ? message.length() : end;
            Log.d(TAG, message.substring(start, end));
        }
    }


    public static void getKeyHash(Activity activity) {
        try {
            PackageInfo info = activity.getPackageManager().getPackageInfo(
                    "com.curatedblogs.app",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                logDebug(Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static <T> String serialize(T obj) {
        Gson gson = new Gson();
        return gson.toJson(obj);

    }

    public static <T> T deSerialize(String jsonString, Class<T> tClass) throws ClassNotFoundException {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(jsonString, tClass);
    }

    public static String getDateAsString(Date date) {
        return formatter.format(date);
    }

    public static Date getStringAsDate(String s) {
        try {
            return formatter.parse(s);
        } catch (ParseException e) {
            logDebug(e.getMessage());
        }
        return new Date();
    }

    public static List<String> getCSVUsersAsList(String users) {
        List<String> otherusers = new ArrayList<String>();
        for (String user : users.split(",")) {
            if (!user.trim().equals("")) {
                otherusers.add(user.trim());
            }
        }
        return otherusers;
    }

    public static String getUsersListAsCSV(List<String> users) {
        StringBuilder sb = new StringBuilder();
        char separator = ',';
        // all but last
        for(int i = 0; i < users.size() - 1 ; i++) {
            sb.append(users.get(i));
            sb.append(separator);
        }

        // last string, no separator
        if(users.size() > 0){
            sb.append(users.get(users.size()-1));
        }

        return sb.toString();
    }
}
