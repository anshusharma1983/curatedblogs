package com.curatedblogs.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.LruCache;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.curatedblogs.app.R;
import com.curatedblogs.app.common.BaseActivity;
import com.curatedblogs.app.domain.Blog;
import com.curatedblogs.app.interfaces.IParseQueryRunner;
import com.curatedblogs.app.utils.OnSwipeListener;
import com.curatedblogs.app.utils.SwipeDetector;
import com.parse.ParseQuery;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import bolts.Task;

/**
 * Created by Anshu on 28/10/15.
 */
public class BlogActivity extends BaseActivity{

    Activity activity;
    ImageView imageView;
    TextView titleView;
    TextView articleView;
    List<Blog> blogs;
    int currentBlog = 0;
    private SwipeDetector sd;
    private LruCache<String, Bitmap> mMemoryCache;
    boolean loading = false;
    Button readMoreButton;
    Button shareButton;

//    SwipeDetector sd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.activity = this;
        setContentView(R.layout.blog_activity);
        this.imageView = (ImageView) findViewById(R.id.image);
        this.titleView = (TextView) findViewById(R.id.title);
        this.articleView = (TextView) findViewById(R.id.article);
        this.readMoreButton = (Button) findViewById(R.id.readMoreButton);
        this.shareButton = (Button) findViewById(R.id.shareButton);
        ParseQuery<Blog> parseQuery = ParseQuery.getQuery(Blog.class);
        parseQuery.orderByDescending("createdAt");
        parseQuery.whereEqualTo("deleted", false);
        initializeCache();
        initializeBlogs(parseQuery);
        initializeSwipe();
        initializeButtons();
    }

    private void initializeButtons() {
        readMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = blogs.get(currentBlog).getSource();
                if (url == null || url.equalsIgnoreCase("")) {
                    Toast.makeText(activity, "Sorry no details available.", Toast.LENGTH_SHORT).show();
                    return;
                }
                AlertDialog.Builder alert = new AlertDialog.Builder(activity);
                alert.setTitle("Powered by curatedblogs");
                WebView wv = new WebView(activity);
                wv.loadUrl(url);
                wv.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);

                        return true;
                    }
                });

                alert.setView(wv);
                alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                alert.show();

            }
        });
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(activity, "coming soon", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeSwipe() {
        sd = new SwipeDetector(this, new SwipeDetector.OnSwipeListener() {
            @Override
            public void onSwipeUp(float distance, float velocity) {
                if (loading) {
                    return;
                }
                System.out.println("Swipe up");
                if (currentBlog < blogs.size() - 1) {
                    showBlog(blogs.get(++currentBlog));
                    if (currentBlog < blogs.size() - 1) {
                        new BitMapTask(blogs.get(currentBlog+1).getFileURL(), false).execute(blogs.get(currentBlog+1).getFileURL());
                    }
                }else {
                    showToast("No more articles to show");
                }
            }

            @Override
            public void onSwipeDown(float distance, float velocity) {
                if (loading) {
                    return;
                }
                System.out.println("SwipeDown");
                if (currentBlog > 0) {
                    showBlog(blogs.get(--currentBlog));
                }else {
                    showToast("No more articles to show");
                }
            }

            @Override
            public void onSwipeLeft(float distance, float velocity) {
                System.out.println("SwipeLeft");
            }

            @Override
            public void onSwipeRight(float distance, float velocity) {
                System.out.println("SwipeRight");
            }
        });
    }

    private void initializeBlogs(ParseQuery<Blog> parseQuery) {
        runParseQuery(parseQuery, new IParseQueryRunner<Blog>() {
            @Override
            public void onComplete(final List<Blog> result) {
                for (Blog blog : result) {
//                    new BitMapTask(blog.getFileURL(), false).execute(blog.getFileURL());
                }
                blogs = result;
                final Blog blog = result.get(0);
                showBlog(blog);
                if (currentBlog < blogs.size() - 1) {
                    new BitMapTask(blogs.get(currentBlog + 1).getFileURL(), false).execute(blogs.get(currentBlog + 1).getFileURL());
                }
            }
        });
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return sd.onTouch(null, event);
    }

    private void showBlog(Blog blog) {
        titleView.setText(blog.getTitle());
        titleView.setTextAppearance(this, android.R.style.TextAppearance_Medium);
        articleView.setText(Html.fromHtml(Html.fromHtml(blog.getUrl()).toString()));
//        imageView.setVisibility(View.GONE);
        loading = true;
//        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        imageView.setImageResource(R.drawable.blog);
        new BitMapTask(blog.getFileURL(), true).execute(blog.getFileURL());
    }

    class BitMapTask extends AsyncTask<String, Void, Bitmap> {
        String url;
        Boolean render;

        BitMapTask(String url, Boolean render) {
            this.url = url;
            this.render = render;
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
//                imageView.setVisibility(View.VISIBLE);
                loading = false;
//                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initializeCache() {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        System.out.println("maxMemory:" + maxMemory);
        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 4;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }
}
