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

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.curatedblogs.app.R;
import com.curatedblogs.app.common.BitMapTask;
import com.curatedblogs.app.common.MyTagHandler;
import com.curatedblogs.app.domain.Blog;
import com.curatedblogs.app.domain.BlogVO;
import com.curatedblogs.app.domain.Bookmark;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import static android.R.style.Theme_Black_NoTitleBar_Fullscreen;
import static com.curatedblogs.app.activity.BlogActivity.*;

/**
 * A fragment representing a single step in a wizard. The fragment shows a dummy title indicating
 * the page number, along with some dummy text.
 */
public class ScreenSlidePageFragment extends Fragment {
    /**
     * The argument key for the page number this fragment represents.
     */
    public static final String ARG_PAGE = "page";

    /**
     * The fragment's page number, which is set to the argument value for {@link #ARG_PAGE}.
     */
    private int mPageNumber;
    private BlogVO blog;
    private Animation animScale;


    /**
     * Factory method for this fragment class. Constructs a new fragment for the given page number.
     */
    public static ScreenSlidePageFragment create(int pageNumber, BlogVO blog) {
        ScreenSlidePageFragment fragment = new ScreenSlidePageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        args.putSerializable("blog", blog);
        fragment.setArguments(args);
        return fragment;
    }

    public ScreenSlidePageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);
        this.blog = (BlogVO) getArguments().getSerializable("blog");
        this.animScale = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_scale);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.
        ViewGroup rootView = (ViewGroup) inflater
                .inflate(R.layout.fragment_screen_slide_page, container, false);

        // Set the title view to show the page number.
//        ((TextView) rootView.findViewById(android.R.id.text1)).setText("Sample");
        TextView title = (TextView) rootView.findViewById(R.id.title);
//        System.out.println("Setting title:" + blog.getTitle());
        title.setText(blog.getTitle());
        TextView article = (TextView) rootView.findViewById(R.id.article);
        ImageView imageView = (ImageView) rootView.findViewById(R.id.image);
        TextView articleSource = (TextView) rootView.findViewById(R.id.articleSource);
        Button bookmarkButton = (Button) rootView.findViewById(R.id.bookmarkButton);
        if (blog.getCategory() != null && !blog.getCategory().equals("")) {
            articleSource.setText(blog.getCategory());
        }
        article.setText(Html.fromHtml(Html.fromHtml(blog.getArticle()).toString(), null, new MyTagHandler()));
        if (blog.getBookmarked()) {
            bookmarkButton.setBackgroundResource(R.drawable.ic_bookmark_black_18dp);
        }
        Button readMoreButton = null;
        readMoreButton = (Button) rootView.findViewById(R.id.readMoreButton);
        initializeButtons(readMoreButton, bookmarkButton, blog, getActivity());
        new BitMapTask(blog.getFileURL(), true, imageView).execute(blog.getFileURL());
        return rootView;
    }

    private void initializeButtons(Button readMoreButton, final Button bookmarkButton, final BlogVO blog, final Activity activity) {
        readMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(animScale);
                String url = blog.getSource();
                if (url == null || url.equalsIgnoreCase("")) {
                    Toast.makeText(activity, "Sorry no details available.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Dialog dialog = new Dialog(activity, Theme_Black_NoTitleBar_Fullscreen);
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

        bookmarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(animScale);
                if (blog.getBookmarked()) {
                    ParseQuery<Bookmark> parseQuery = ParseQuery.getQuery(Bookmark.class);
                    parseQuery.whereEqualTo("blogObjectId", blog.getObjectId());
                    parseQuery.whereEqualTo("userId", ParseUser.getCurrentUser().getObjectId());
                    System.out.println("Finding bookmark with:\n blogObjectId" + blog.getObjectId()
                            + "\nuserId:" + ParseUser.getCurrentUser().getObjectId());
                    blog.setBookmarked(false);
                    parseQuery.findInBackground(new FindCallback<Bookmark>() {
                        @Override
                        public void done(List<Bookmark> list, ParseException e) {
                            // would definitely get a single object here
                            if (list!=null & list.size() > 0) {
                                Bookmark bookmark = list.get(0);
                                System.out.print("Deleting bookmark, ObjectId:" + bookmark.getObjectId());
                                bookmark.deleteInBackground();
                            }
                        }
                    });
                    Toast.makeText(activity, "Bookmark removed !", Toast.LENGTH_SHORT).show();
                    view.setBackgroundResource(R.drawable.ic_bookmark_border_black_18dp);
                }else {
                    Bookmark bookmark = new Bookmark();
                    System.out.println("Saving bookmark, \nblogObjectId:" + blog.getObjectId()
                            + ", \nuserObjectId:" + ParseUser.getCurrentUser().getObjectId());
                    bookmark.setBlogObjectId(blog.getObjectId());
                    bookmark.setUserId(ParseUser.getCurrentUser().getObjectId());
                    bookmark.saveInBackground();
                    Toast.makeText(activity, "Article bookmarked !", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Returns the page number represented by this fragment object.
     */
    public int getPageNumber() {
        return mPageNumber;
    }
}
