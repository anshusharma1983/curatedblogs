package com.curatedblogs.app.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import com.curatedblogs.app.domain.Version;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anshu on 28/10/15.
 */
public class BlogActivity extends BaseActivity {

    Activity activity;
    List<Blog> blogs;
    Boolean isbookmark = false;
    ImageView loadingImageView;
    Integer appVersion = 1;

    @Override
    protected void onStart() {
        super.onStart();
        checkVersion();
        System.out.println("Launching screenSlideActivity ");
    }

    private void checkVersion(){
        ParseQuery<Version> parseQuery = ParseQuery.getQuery(Version.class);
        try {
            Version version = parseQuery.find().get(0);
            System.out.println("Current version:" + appVersion + ", server version:" + version.getVersion());
            if (version.getVersion() > appVersion) {
                new AlertDialog.Builder(activity)
                        .setTitle("Update application")
                        .setMessage("This version is not supported, please update the app from the app store")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:Growtist")));
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }else {
                initializeCache();
                initializeBlogs(isbookmark);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public void onBackPressed() {
//        System.out.println("back pressed!. isBookmark:" + isbookmark);
//        if (isbookmark) {
//            Intent intent = new Intent(activity, BlogActivity.class);
//            startActivity(intent);
//            finish();
//        }
//        super.onBackPressed();
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.activity = this;
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        setContentView(R.layout.blog_activity);
//        this.loadingImageView = (ImageView) findViewById(R.id.loadingImage);
//        loadingImageView.setBackgroundResource(R.drawable.front_page);
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
        final String userId = ParseUser.getCurrentUser().getObjectId();
        if (!isBookMark) {
            ParseQuery<Blog> parseQuery = ParseQuery.getQuery(Blog.class);
            parseQuery.orderByDescending("createdAt");
            parseQuery.whereEqualTo("deleted", false);
            parseQuery.findInBackground(new FindCallback<Blog>() {
                @Override
                public void done(List<Blog> list, ParseException e) {
                    blogs = list;
                    ParseQuery<Bookmark> bookmarkParseQuery = ParseQuery.getQuery(Bookmark.class);
                    bookmarkParseQuery.whereEqualTo("userId", userId);
                    List<Bookmark> bookmarks = null;
                    try {
                        bookmarks = bookmarkParseQuery.find();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                    if (list != null && list.size()>0) {
                        updateBookmarks(blogs, bookmarks, false);
                    }
                    startScreenSlideActivity(false);
                }
            });
//            loadingImageView.setVisibility(View.GONE);
        }else {
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
                        updateBookmarks(blogs, null, true);
                        startScreenSlideActivity(true);

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

    private void updateBookmarks(List<Blog> blogs, List<Bookmark> list, Boolean setAllBookmarked) {
        List<String> bookmarks = new ArrayList<String>();
        if (!setAllBookmarked) {
            for (Bookmark bookmark : list) {
                bookmarks.add(bookmark.getBlogObjectId());
            }
        }

        for (Blog blog : blogs) {
            if (setAllBookmarked) {
                blog.setBookmarked(true);
                continue;
            }
            if (bookmarks.contains(blog.getObjectId())) {
                System.out.println("blog:" + blog.getTitle() + " is bookmarked!");
                blog.setBookmarked(true);
            }
        }
    }

    private void startScreenSlideActivity(Boolean allBookmarks) {
        Intent screenSlideActivity = new Intent(activity, ScreenSlideActivity.class);
        screenSlideActivity.putExtra("blogWrapper", new BlogsWrapper(BlogVO.initializeList(blogs), isbookmark));
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
