package com.curatedblogs.app.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Vibrator;
import android.text.Html;
import android.util.Log;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import com.curatedblogs.app.common.BitMapTask;
import com.curatedblogs.app.common.MyTagHandler;
import com.curatedblogs.app.domain.Blog;
import com.curatedblogs.app.domain.BlogVO;
import com.curatedblogs.app.domain.BlogsWrapper;
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
import java.io.Serializable;
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
    boolean loading = true;

    ImageView loadingImageView;
    //    SwipeDetector sd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.activity = this;
//        /*Saurabh*/
//        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
//        ActionBar actionBar = getActionBar();
//        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
//        getActionBar().setDisplayShowTitleEnabled(false);
//        /*Saurabh*/
        setContentView(R.layout.blog_activity);
        this.loadingImageView = (ImageView) findViewById(R.id.loadingImage);
        this.imageView = (ImageView) findViewById(R.id.image);
        this.titleView = (TextView) findViewById(R.id.title);
        this.articleView = (TextView) findViewById(R.id.article);
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
        loadingImageView.setBackgroundResource(R.drawable.insights_background);
        this.loadingImageView.setVisibility(View.VISIBLE);

        try {
            initializeCache();
            initializeBlogs(isbookmark);
//            initializeSwipe();
            Intent screenSlideActivity = new Intent(activity, ScreenSlideActivity.class);
            screenSlideActivity.putExtra("blogWrapper", new BlogsWrapper(BlogVO.initializeList(blogs)));
            startActivity(screenSlideActivity);
            finish();
        }catch (ParseException ex){
            ex.printStackTrace();;
            System.out.println(ex);
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        Intent intent = new Intent(activity, BlogActivity.class);
//        switch(item.getItemId()) {
//            case R.id.articles:
//                showToast("Selected articles");
//                startActivity(intent);
//                finish();
//                return true;
//            case R.id.bookmarks:
//                showToast("Selected bookmarks");
//                intent.putExtra("key", "bookmark");
//                startActivity(intent);
//                finish();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

//    private void initializeSwipe() {
//        sd = new SwipeDetector(this, new SwipeDetector.OnSwipeListener() {
//            @Override
//            public void onSwipeUp(float distance, float velocity) {
//                if (loading || blogs == null || blogs.size() == 0) {
//                    System.out.println("Swipe not ready");
//                    return;
//                }
//                System.out.println("Swipe up");
//                if (currentBlog < blogs.size() - 1) {
////                    System.out.println("---Current user info----\n ParseUser.getCurrentUser():" + ParseUser.getCurrentUser()
////                            + "\n ParseUser.getCurrentUser().getObjectId()" + ParseUser.getCurrentUser().getObjectId()
////                            + " \n ParseUser.getCurrentUser().getUsername():"
////                            + ParseUser.getCurrentUser().getUsername() + "\n ParseUser.getCurrentUser().getSessionToken():"
////                            + ParseUser.getCurrentUser().getSessionToken());
//                    currentBlog ++;
//                    Blog blogToShow = blogs.get(currentBlog);
//                    Bookmark bookmark = new Bookmark();
//
//// Added by Saurabh on 3 Nov
//                    bookmarkButton.setBackgroundResource(R.drawable.ic_bookmark_border_black_18dp);
//// Added by Saurabh on 3 Nov
//
//                    bookmark.setBlogObjectId(blogToShow.getObjectId());
//                    bookmark.setUserId(ParseUser.getCurrentUser().getObjectId());
////                    bookmark.saveInBackground();
//                    showBlog(blogToShow);
//                    if (currentBlog < blogs.size() - 1) {
//                        new BitMapTask(blogs.get(currentBlog+1).getFileURL(), false, imageView).execute(blogs.get(currentBlog+1).getFileURL());
//                    }
//                }else {
//                    showToast("No more articles to show");
//                }
//            }
//
//            @Override
//            public void onSwipeDown(float distance, float velocity) {
//                if (loading || blogs == null || blogs.size() == 0) {
//                    System.out.println("Swipe not ready");
//                    return;
//                }
//                System.out.println("SwipeDown");
//                if (currentBlog > 0) {
//                    showBlog(blogs.get(--currentBlog));
//                }else {
//                    showToast("No more articles to show");
//                }
//            }
//
//            @Override
//            public void onSwipeLeft(float distance, float velocity) {
//                System.out.println("SwipeLeft");
//            }
//
//            @Override
//            public void onSwipeRight(float distance, float velocity) {
//                System.out.println("SwipeRight");
//            }
//        });
//    }

    private void initializeBlogs(boolean isBookMark) throws ParseException{
        if (!isBookMark) {
            loading = true;
            ParseQuery<Blog> parseQuery = ParseQuery.getQuery(Blog.class);
            parseQuery.orderByDescending("createdAt");
            parseQuery.whereEqualTo("deleted", false);
            blogs = parseQuery.find();
//            blogs = result;
            final Blog blog = blogs.get(0);
            showBlog(blog);
            if (currentBlog < blogs.size() - 1) {
                new BitMapTask(blogs.get(currentBlog + 1).getFileURL(), false, imageView).execute(blogs.get(currentBlog + 1).getFileURL());
            }
            loading = false;
            loadingImageView.setVisibility(View.GONE);
//            parseQuery.findInBackground(new FindCallback<Blog>() {
//                @Override
//                public void done(List<Blog> result, ParseException e) {
//
//                }
//            });
        }else {
            loading = true;
            String userId = ParseUser.getCurrentUser().getObjectId();
            ParseQuery<Blog> parseQuery = ParseQuery.getQuery(Blog.class);
            parseQuery.orderByDescending("createdAt");
            parseQuery.whereEqualTo("deleted", false);
            ParseQuery<Bookmark> bookmarkParseQuery = ParseQuery.getQuery(Bookmark.class);
            bookmarkParseQuery.whereEqualTo("userId", userId);
            parseQuery.whereMatchesKeyInQuery("objectId", "blogObjectId", bookmarkParseQuery);
            List<Blog> result = parseQuery.find();
            if (result!=null && result.size() > 0) {
                blogs = result;
                final Blog blog = result.get(0);
                showBlog(blog);
                if (currentBlog < blogs.size() - 1) {
                    new BitMapTask(blogs.get(currentBlog + 1).getFileURL(), false, imageView).execute(blogs.get(currentBlog + 1).getFileURL());
                }
                loading = false;
                loadingImageView.setVisibility(View.GONE);
            } else {
                showToast("You have no bookmarks");
                loading = false;
                loadingImageView.setVisibility(View.GONE);
                Intent intent = new Intent(activity, BlogActivity.class);
                startActivity(intent);
                finish();
            }
//            parseQuery.findInBackground(new FindCallback<Blog>() {
//                @Override
//                public void done(List<Blog> result, ParseException e) {
//
//
//                }
//            });
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return sd.onTouch(null, event);
    }

    private void showBlog(Blog blog) {
        titleView.setText(blog.getTitle());
        titleView.setTextAppearance(this, android.R.style.TextAppearance_Medium);
        System.out.println("Showing blog:" + blog.getTitle());
        articleView.setText(Html.fromHtml(Html.fromHtml(blog.getUrl()).toString(), null, new MyTagHandler()));
//        imageView.setVisibility(View.GONE);

        loading = true;
//        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        imageView.setImageResource(R.drawable.blog);
        new BitMapTask(blog.getFileURL(), true, imageView).execute(blog.getFileURL());
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

        BitMapTask.mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
