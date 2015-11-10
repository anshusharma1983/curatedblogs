package com.curatedblogs.app.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.LruCache;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.curatedblogs.app.R;
import com.curatedblogs.app.common.BaseActivity;
import com.curatedblogs.app.common.BitMapTask;
import com.curatedblogs.app.domain.Blog;
import com.curatedblogs.app.domain.BlogVO;
import com.curatedblogs.app.domain.BlogsWrapper;
import com.curatedblogs.app.domain.Bookmark;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import java.util.List;

/**
 * Created by Anshu on 28/10/15.
 */
public class BlogActivity extends BaseActivity {

    Activity activity;
    List<Blog> blogs;
    Boolean isbookmark;
//    ImageView loadingImageView;


    @Override
    protected void onStart() {
        super.onStart();
        try {
            initializeCache();
            initializeBlogs(isbookmark);
            System.out.println("Launching screenSlideActivity ");
        }catch (ParseException ex){
            ex.printStackTrace();;
            System.out.println(ex);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.activity = this;
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        setContentView(R.layout.blog_activity);
//        this.loadingImageView = (ImageView) findViewById(R.id.loadingImage);
        Bundle b = getIntent().getExtras();
        String key = null;
        isbookmark = false;
        if (null != b) {
            key = b.getString("key");
        }
        if (key != null && key.equals("bookmark")) {
            System.out.println("Running the activity for bookmark");
            isbookmark = true;
        }
        System.out.println("Showing loadingImageView");
    }



    private void initializeBlogs(boolean isBookMark) throws ParseException{
        if (!isBookMark) {
            ParseQuery<Blog> parseQuery = ParseQuery.getQuery(Blog.class);
            parseQuery.orderByDescending("createdAt");
            parseQuery.whereEqualTo("deleted", false);
            parseQuery.findInBackground(new FindCallback<Blog>() {
                @Override
                public void done(List<Blog> list, ParseException e) {
                    blogs = list;
                    startScreenSlideActivity();
                }
            });
//            loadingImageView.setVisibility(View.GONE);
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
                        startScreenSlideActivity();
                    } else {
                        showToast("You have no bookmarks");
//                        loadingImageView.setVisibility(View.GONE);
                        Intent intent = new Intent(activity, BlogActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            });

        }
    }

    private void startScreenSlideActivity() {
        Intent screenSlideActivity = new Intent(activity, ScreenSlideActivity.class);
        screenSlideActivity.putExtra("blogWrapper", new BlogsWrapper(BlogVO.initializeList(blogs)));
        startActivity(screenSlideActivity);
        finish();
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
