package com.curatedblogs.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.curatedblogs.app.R;
import com.curatedblogs.app.common.BaseActivity;
import com.curatedblogs.app.domain.Blog;
import com.curatedblogs.app.domain.Bookmark;
import com.curatedblogs.app.interfaces.IParseQueryRunner;
import com.curatedblogs.app.utils.OnSwipeListener;
import com.curatedblogs.app.utils.SwipeDetector;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import bolts.Task;

import static android.R.style.Theme_Black_NoTitleBar_Fullscreen;

/**
 * Created by Anshu on 28/10/15.
 */
public class BlogActivity extends BaseActivity {

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
    Button bookmarkButton;
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
        this.bookmarkButton = (Button) findViewById(R.id.bookmarkButton);
        Bundle b = getIntent().getExtras();
        String key = null;
        boolean isbookmark = false;
        if (null != b) {
            key = b.getString("key");
        }
        if (key != null && key.equals("bookmark")) {
            System.out.println("Running the activity for bookmark");
            isbookmark = true;
        }
        initializeCache();
        initializeBlogs(isbookmark);
        initializeSwipe();
        initializeButtons();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(activity, BlogActivity.class);
        switch(item.getItemId()) {
            case R.id.articles:
                showToast("Selected articles");
                startActivity(intent);
                finish();
                return true;
            case R.id.bookmarks:
                showToast("Selected bookmarks");
                intent.putExtra("key", "bookmark");
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
                Dialog dialog=new Dialog(activity, Theme_Black_NoTitleBar_Fullscreen);
                WebView wv = new WebView(activity);
                wv.loadUrl(url);
                wv.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);

                        return true;
                    }
                });
                dialog.setContentView(wv);
                dialog.show();
            }
        });
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(activity, "coming soon", Toast.LENGTH_SHORT).show();
            }
        });

        bookmarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Blog blogToShow = blogs.get(currentBlog);
                Bookmark bookmark = new Bookmark();
                bookmark.setBlogObjectId(blogToShow.getObjectId());
                bookmark.setUserId(ParseUser.getCurrentUser().getObjectId());
                bookmark.saveInBackground();
                showToast(blogToShow.getTitle() + " bookmarked !");
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
//                    System.out.println("---Current user info----\n ParseUser.getCurrentUser():" + ParseUser.getCurrentUser()
//                            + "\n ParseUser.getCurrentUser().getObjectId()" + ParseUser.getCurrentUser().getObjectId()
//                            + " \n ParseUser.getCurrentUser().getUsername():"
//                            + ParseUser.getCurrentUser().getUsername() + "\n ParseUser.getCurrentUser().getSessionToken():"
//                            + ParseUser.getCurrentUser().getSessionToken());
                    currentBlog ++;
                    Blog blogToShow = blogs.get(currentBlog);
                    Bookmark bookmark = new Bookmark();
                    bookmark.setBlogObjectId(blogToShow.getObjectId());
                    bookmark.setUserId(ParseUser.getCurrentUser().getObjectId());
//                    bookmark.saveInBackground();
                    showBlog(blogToShow);
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

    private void initializeBlogs(boolean isBookMark) {
        if (!isBookMark) {
            ParseQuery<Blog> parseQuery = ParseQuery.getQuery(Blog.class);
            parseQuery.orderByDescending("createdAt");
            parseQuery.whereEqualTo("deleted", false);
            parseQuery.findInBackground(new FindCallback<Blog>() {
                @Override
                public void done(List<Blog> result, ParseException e) {
                    blogs = result;
                    final Blog blog = result.get(0);
                    showBlog(blog);
                    if (currentBlog < blogs.size() - 1) {
                        new BitMapTask(blogs.get(currentBlog + 1).getFileURL(), false).execute(blogs.get(currentBlog + 1).getFileURL());
                    }
                }
            });
        }else {
            String userId = ParseUser.getCurrentUser().getObjectId();
            ParseQuery<Blog> parseQuery = ParseQuery.getQuery(Blog.class);
            parseQuery.orderByDescending("createdAt");
            parseQuery.whereEqualTo("deleted", false);
            ParseQuery<Bookmark> bookmarkParseQuery = ParseQuery.getQuery(Bookmark.class);
            bookmarkParseQuery.whereEqualTo("userId", userId);
            parseQuery.whereMatchesKeyInQuery("objectId", "blogObjectId", bookmarkParseQuery);
            parseQuery.findInBackground(new FindCallback<Blog>() {
                @Override
                public void done(List<Blog> result, ParseException e) {
                    if (result!=null && result.size() > 0) {
                        blogs = result;
                        final Blog blog = result.get(0);
                        showBlog(blog);
                        if (currentBlog < blogs.size() - 1) {
                            new BitMapTask(blogs.get(currentBlog + 1).getFileURL(), false).execute(blogs.get(currentBlog + 1).getFileURL());
                        }
                    } else {
                        showToast("You have no bookmarks");
                    }
                }
            });
        }
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

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
