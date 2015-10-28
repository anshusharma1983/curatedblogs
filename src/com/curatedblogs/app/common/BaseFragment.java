package com.curatedblogs.app.common;


import android.app.ProgressDialog;
import android.support.v4.app.Fragment;

import com.curatedblogs.app.utils.Helpers;

public class BaseFragment extends Fragment {
    private ProgressDialog progressDialog;

    public ProgressDialog getProgressDialog() {

        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this.getCurrentActivity());
        }

        return progressDialog;
    }

    public void logDebug(String message) {
        Helpers.logDebug(message);
    }

    public BaseActivity getCurrentActivity() {
        return (BaseActivity) getActivity();
    }

    public void showProgress() {
        getCurrentActivity().showProgress(progressDialog);
    }

    public void hideProgress() {
        getCurrentActivity().hideProgress(progressDialog);
    }

    public void showToast(String message) {
        getCurrentActivity().showToast(message);
    }
}
