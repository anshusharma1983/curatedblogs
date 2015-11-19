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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import static android.R.style.Theme_Black_NoTitleBar_Fullscreen;
import static android.provider.MediaStore.Images.Media.insertImage;

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
    private ProgressDialog progress;


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
        ImageButton bookmarkButton = (ImageButton) rootView.findViewById(R.id.bookmarkButton);
        if (blog.getCategory() != null && !blog.getCategory().equals("")) {
            if (blog.getCategory().equals("CuratedBlogs")) {
                articleSource.setText("Growtist");
            }else {
                articleSource.setText(blog.getCategory());
            }
        }
        article.setText(Html.fromHtml(Html.fromHtml(blog.getArticle()).toString(), null, new MyTagHandler()));
        TextView bookmarkText = null;
        bookmarkText = (TextView) rootView.findViewById(R.id.bookmarkText);
        if (blog.getBookmarked()) {
            bookmarkButton.setBackgroundResource(R.drawable.ic_bookmark_black_18dp);
            bookmarkText.setText("Remove");
        }
        ImageButton readMoreButton = null, shareButton = null;
        TextView readMoreText = null,  shareText = null;
        readMoreButton = (ImageButton) rootView.findViewById(R.id.readMoreButton);
        shareButton = (ImageButton) rootView.findViewById(R.id.shareButton);
        readMoreText = (TextView) rootView.findViewById(R.id.readMoreText);
        RelativeLayout bookmarkLayout, readmoreLayout, shareLayout;
        bookmarkLayout = (RelativeLayout) rootView.findViewById(R.id.bookmarkLayout);
        readmoreLayout = (RelativeLayout) rootView.findViewById(R.id.readmoreLayout);
        shareLayout = (RelativeLayout) rootView.findViewById(R.id.shareLayout);
        shareText = (TextView) rootView.findViewById(R.id.shareText);
        View.OnClickListener shareOnClickListener = new ShareOnClickListener(getActivity(), rootView, shareButton);
        View.OnClickListener readMoreOnClickListener = new ReadMoreOnClickListener(getActivity(), readMoreButton);
        View.OnClickListener bookmarkOnClickListener = new BookmarkOnClickListener(getActivity(), bookmarkButton, bookmarkText);
        shareButton.setOnClickListener(shareOnClickListener);
        shareText.setOnClickListener(shareOnClickListener);
        readMoreButton.setOnClickListener(readMoreOnClickListener);
        readMoreText.setOnClickListener(readMoreOnClickListener);
        bookmarkButton.setOnClickListener(bookmarkOnClickListener);
        bookmarkText.setOnClickListener(bookmarkOnClickListener);
        bookmarkLayout.setOnClickListener(bookmarkOnClickListener);
        readmoreLayout.setOnClickListener(readMoreOnClickListener);
        shareLayout.setOnClickListener(shareOnClickListener);
        new BitMapTask(blog.getFileURL(), true, imageView).execute(blog.getFileURL());
        return rootView;
    }

    private class ShareOnClickListener implements View.OnClickListener{
        private Activity activity;
        View root;
        ImageButton shareButton;
        public ShareOnClickListener(Activity activity, ViewGroup rootView, ImageButton shareButton) {
            this.activity = activity;
            this.root = rootView;
            this.shareButton = shareButton;
        }
        @Override
        public void onClick(View view) {
            System.out.println("Clicked share button, playing animation !");
            shareButton.startAnimation(animScale);
//            new Thread(new Runnable(){
//                @Override
//                public void run() {
//                    shareButton.post(new Runnable() {
//                        @Override
//                        public void run() {
//
//                        }
//                    });
//                }
//            });
//            shareButton.startAnimation(animScale);
//            MediaPlayer mp = MediaPlayer.create(activity, R.raw.voicebegin);
//            mp.start();
//            Vibrator v = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
//            v.vibrate(100);
//            new Thread()
            Bitmap bitmap = Bitmap.createBitmap(root.getWidth(), root.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            root.draw(canvas);

//            try {
//                FileOutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/growtistShare.png");
//                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
//                output.close();
//                System.out.println("Written file successfully");
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            String pathofBmp = insertImage(getActivity().getContentResolver(), bitmap, "title", null);
            Uri bmpUri = Uri.parse(pathofBmp);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/jpeg");
            share.putExtra(Intent.EXTRA_STREAM, bmpUri);
            share.putExtra(Intent.EXTRA_TEXT, "read more at " + blog.getSource());
            startActivity(Intent.createChooser(share, "Share Image"));
        }
    }

    private class BookmarkOnClickListener implements View.OnClickListener {

        private final ImageButton bookmarkButton;
        private Activity activity;
        private TextView bookmarkText;
        public BookmarkOnClickListener(Activity activity, ImageButton bookmarkButton, TextView bookmarkText) {
            this.activity = activity;
            this.bookmarkButton = bookmarkButton;
            this.bookmarkText = bookmarkText;
        }

        @Override
        public void onClick(View view) {
            boolean animate = false;
            bookmarkButton.startAnimation(animScale);
            System.out.println("Blog bookmarked:" + blog.getBookmarked());
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
                        if (list != null & list.size() > 0) {
                            Bookmark bookmark = list.get(0);
                            System.out.print("Deleting bookmark, ObjectId:" + bookmark.getObjectId());
                            bookmark.deleteInBackground();
                        }
                    }
                });
                Toast.makeText(activity, "Bookmark removed !", Toast.LENGTH_SHORT).show();
                bookmarkButton.setBackgroundResource(0);
                bookmarkButton.setBackgroundColor(Color.TRANSPARENT);
                bookmarkText.setText("Select");
//                bookmarkButton.setBackgroundResource(R.drawable.ic_bookmark_border_black_18dp);
            }else {
                Bookmark bookmark = new Bookmark();
                System.out.println("Saving bookmark, \nblogObjectId:" + blog.getObjectId()
                        + ", \nuserObjectId:" + ParseUser.getCurrentUser().getObjectId());
                bookmark.setBlogObjectId(blog.getObjectId());
                bookmark.setUserId(ParseUser.getCurrentUser().getObjectId());
                bookmark.saveInBackground();
                bookmarkButton.setBackgroundResource(R.drawable.ic_bookmark_black_18dp);
                blog.setBookmarked(true);
                bookmarkText.setText("Remove");
                Toast.makeText(activity, "Article bookmarked !", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ReadMoreOnClickListener implements View.OnClickListener{
        private Activity activity;
        private ImageButton readMoreButton;
        public ReadMoreOnClickListener(Activity activity, ImageButton readMoreButton) {
            this.activity = activity;
            this.readMoreButton = readMoreButton;
        }
        @Override
        public void onClick(View view) {
//            boolean animate = false;
            readMoreButton.startAnimation(animScale);
//            new Thread(new Runnable(){
//                @Override
//                public void run() {
//                    readMoreButton.post(new Runnable() {
//                        @Override
//                        public void run() {
//
//                        }
//                    });
//                }
//            });
            String url = blog.getSource();

            MediaPlayer mp = MediaPlayer.create(activity, R.raw.voicebegin);
            mp.start();
            Vibrator v = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(100);

            if (url == null || url.equalsIgnoreCase("")) {
                Toast.makeText(activity, "Sorry no details available.", Toast.LENGTH_SHORT).show();
                return;
            }
            Dialog dialog = new Dialog(activity, Theme_Black_NoTitleBar_Fullscreen);
            WebView wv = new WebView(activity);
            wv.loadUrl(url);
            wv.setWebViewClient(new WebViewClient(){
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });
            wv.setWebChromeClient(new WebChromeClient(){
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    super.onProgressChanged(view, newProgress);
                    if (newProgress > 0) {
                        showProgressDialog("Loading..");
                    }
                    if (newProgress >= 90) {
                        hideProgressDialog();
                    }
                }
            });

            dialog.setContentView(wv);
            dialog.show();
        }
    };

    public void showProgressDialog(final String msg) {

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if (progress == null || !progress.isShowing()) {
                    progress = ProgressDialog.show(getActivity(), "", msg);
                }
            }
        });
    }

    public void hideProgressDialog() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (progress.isShowing())
                        progress.dismiss();
                } catch (Throwable e) {
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

    private class WriteImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... objects) {
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap o) {
            super.onPostExecute(o);
        }
    }
}
