package com.curatedblogs.app.common;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Anshu on 09/11/15.
 */
public class BitMapTask extends AsyncTask<String, Void, Bitmap> {
    String url;
    Boolean render;
    ImageView imageView;
    public static LruCache<String, Bitmap> mMemoryCache;

    public BitMapTask(String url, Boolean render, ImageView imageView) {
        this.url = url;
        this.render = render;
        this.imageView = imageView;
    }

    private Exception exception;

    protected Bitmap doInBackground(String... params) {
        try {
            Bitmap bitmap = getBitmapFromMemCache(url);
            if (bitmap != null) {
                System.out.println("Voila, got the image from cache!");
                return bitmap;
            }else {
                Log.e("src", url);
                URL url1 = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) url1.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
                addBitmapToMemoryCache(url, bitmap);
                Log.e("Bitmap", "returned");
                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Exception", e.getMessage());
            return null;
        }
    }

    protected void onPostExecute(Bitmap bitmap) {
        if (render) {
            imageView.setImageBitmap(bitmap);
        }
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }
}