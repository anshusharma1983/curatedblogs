package com.curatedblogs.app.common;


import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.curatedblogs.app.interfaces.IParseQueryRunner;
import com.curatedblogs.app.utils.Helpers;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class BaseActivity extends Activity {
    private ProgressDialog progressDialog;

    public ParseUser getCurrentUser() {
        return BlogApplication.getInstance().getCurrentUser();
    }

    public void setCurrentUser(ParseUser currentUser) {
        BlogApplication.getInstance().setCurrentUser(currentUser);
    }

    public AccessToken getAccessToken() {
        return BlogApplication.getInstance().getAccessToken();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public ProgressDialog getProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        return progressDialog;
    }

    public void showProgress(ProgressDialog progressDialog) {
        if (progressDialog == null) {
            return;
        }
        if (this.isFinishing()) {
            return;
        }
        progressDialog.setMessage("Please wait..");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void hideProgress(ProgressDialog progressDialog) {
        if (this.isFinishing()) {
            return;
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public void logDebug(String message) {
        Helpers.logDebug(message);
    }

    public void logDebug(Object object) {
        logDebug(Helpers.serialize(object));
    }

    public void logDebug(Exception exception) {
        StringWriter errors = new StringWriter();
        exception.printStackTrace(new PrintWriter(errors));
        logDebug(errors.toString());
    }


    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public <T extends ParseObject> void runParseQuery(ParseQuery<T> query, IParseQueryRunner<T> parseQueryRunner) {
        runParseQuery(query, parseQueryRunner, getProgressDialog());
    }


    public <T extends ParseObject> void runParseQuery(ParseQuery<T> query, IParseQueryRunner<T> parseQueryRunner, ProgressDialog progressDialog) {
        QueryRunner<T> queryRunner = new QueryRunner<T>();
        queryRunner.setQuery(query);
        queryRunner.setProgressDialog(progressDialog);
        queryRunner.setRunner(parseQueryRunner);
        queryRunner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class QueryRunner<T extends ParseObject> extends AsyncTask<Void, Void, Void> {
        IParseQueryRunner<T> runner;
        List<T> result;
        ParseQuery<T> query;
        ProgressDialog progressDialog;

        public QueryRunner() {
        }

        public ProgressDialog getProgressDialog() {
            return progressDialog;
        }

        public void setProgressDialog(ProgressDialog progressDialog) {
            this.progressDialog = progressDialog;
        }

        public ParseQuery<T> getQuery() {
            return query;
        }

        public void setQuery(ParseQuery<T> query) {
            this.query = query;
        }

        public IParseQueryRunner<T> getRunner() {
            return runner;
        }

        public void setRunner(IParseQueryRunner<T> runner) {
            this.runner = runner;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(getProgressDialog());
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                result = getQuery().find();
            } catch (Exception exception) {
                logDebug(exception.toString());
                exception.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            getRunner().onComplete(this.result);
            hideProgress(getProgressDialog());
        }
    }
}
