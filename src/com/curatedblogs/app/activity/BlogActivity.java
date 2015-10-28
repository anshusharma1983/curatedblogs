package com.curatedblogs.app.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
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
//    SwipeDetector sd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.activity = this;
        setContentView(R.layout.blog_activity);
        this.imageView = (ImageView) findViewById(R.id.image);
        this.titleView = (TextView) findViewById(R.id.title);
        this.articleView = (TextView) findViewById(R.id.article);
        ParseQuery<Blog> parseQuery = ParseQuery.getQuery(Blog.class);
        parseQuery.orderByDescending("createdAt");
        runParseQuery(parseQuery, new IParseQueryRunner<Blog>() {
            @Override
            public void onComplete(final List<Blog> result) {
//                for (Blog blog : result) {
//                    System.out.println("title:" + blog.getTitle());
//                }
                blogs = result;
                final Blog blog = result.get(0);
                showBlog(blog);
            }
        });

        sd = new SwipeDetector(this, new SwipeDetector.OnSwipeListener() {
            @Override
            public void onSwipeUp(float distance, float velocity) {
                System.out.println("Swipe up");
                if (currentBlog < blogs.size() - 1) {
                    showBlog(blogs.get(++currentBlog));
                }else {
                    showToast("No more articles to show");
                }
            }

            @Override
            public void onSwipeDown(float distance, float velocity) {
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return sd.onTouch(null, event);
    }

    private void showBlog(Blog blog) {
        titleView.setText(blog.getTitle());
        titleView.setTextAppearance(this, android.R.style.TextAppearance_Large);
        articleView.setText(Html.fromHtml(Html.fromHtml(blog.getUrl()).toString()));
        imageView.setVisibility(View.GONE);
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        new BitMapTask().execute(blog.getFileURL());
    }


    class BitMapTask extends AsyncTask<String, Void, Bitmap> {

        private Exception exception;

        protected Bitmap doInBackground(String... srcs) {
            try {
                Log.e("src",srcs[0]);
                URL url = new URL(srcs[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                Log.e("Bitmap", "returned");
                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Exception", e.getMessage());
                return null;
            }
        }

        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
            imageView.setVisibility(View.VISIBLE);
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
