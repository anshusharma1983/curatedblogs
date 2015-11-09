/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.curatedblogs.app.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.curatedblogs.app.R;
import com.curatedblogs.app.common.BitMapTask;
import com.curatedblogs.app.common.DepthPageTransformer;
import com.curatedblogs.app.common.ZoomOutPageTransformer;
import com.curatedblogs.app.domain.Blog;
import com.curatedblogs.app.domain.BlogVO;
import com.curatedblogs.app.domain.BlogsWrapper;
import com.curatedblogs.app.domain.Bookmark;
import com.parse.ParseUser;

import java.util.List;

import static android.R.style.Theme_Black_NoTitleBar_Fullscreen;

/**
 * Demonstrates a "screen-slide" animation using a {@link ViewPager}. Because {@link ViewPager}
 * automatically plays such an animation when calling {@link ViewPager#setCurrentItem(int)}, there
 * isn't any animation-specific code in this sample.
 *
 * <p>This sample shows a "next" button that advances the user to the next step in a wizard,
 * animating the current screen out (to the left) and the next screen in (from the right). The
 * reverse animation is played when the user presses the "previous" button.</p>
 *
 * @see ScreenSlidePageFragment
 */
public class ScreenSlideActivity extends FragmentActivity {
    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static int NUM_PAGES;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;
    List<BlogVO> blogs;
    private Activity activity;
//    Button readMoreButton;
//    Button bookmarkButton;
    private int currrentItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        ActionBar actionBar = getActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        getActionBar().setDisplayShowTitleEnabled(false);
        setContentView(R.layout.activity_screen_slide);
        this.activity = this;
//        this.readMoreButton = (Button) findViewById(R.id.readMoreButton);
//        this.bookmarkButton = (Button) findViewById(R.id.bookmarkButton);

        // Instantiate a ViewPager and a PagerAdapter.
        BlogsWrapper blogsWrapper = (BlogsWrapper) getIntent().getSerializableExtra("blogWrapper");
        blogs = blogsWrapper.getBlogs();
        NUM_PAGES = blogs.size();
        System.out.println("No. of pages:" + NUM_PAGES);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager(), blogs);
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageTransformer(true, new ZoomOutPageTransformer());
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When changing pages, reset the action bar actions since they are dependent
                // on which page is currently active. An alternative approach is to have each
                // fragment expose actions itself (rather than the activity exposing actions),
                // but for simplicity, the activity provides the actions in this sample.
                invalidateOptionsMenu();
            }
        });
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

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * A simple pager adapter that represents 5 {@link ScreenSlidePageFragment} objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private List<BlogVO> list;
        public ScreenSlidePagerAdapter(FragmentManager fm, List<BlogVO> blogs) {
            super(fm);
            this.list = blogs;
        }

        @Override
        public Fragment getItem(int position) {
            System.out.println("Showing item:" + position);
            BlogVO blog = list.get(position);
            new BitMapTask(blog.getFileURL(), false, null).execute(blog.getFileURL());
            if (position < blogs.size() - 1) {
                BlogVO next = blogs.get(position + 1);
                new BitMapTask(next.getFileURL(), false, null).execute(next.getFileURL());
            }
            return ScreenSlidePageFragment.create(position, blog);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
