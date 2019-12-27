package com.example.capentory_client.ui.errorhandling;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.ClientError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.example.capentory_client.R;

import org.json.JSONException;

public class BasicNetworkErrorHandler {
    private View errorView;
    private Context context;


    public BasicNetworkErrorHandler(Context context, View errorView) {
        this.errorView = errorView;
        this.context = context;
    }

    public BasicNetworkErrorHandler(Context context) {
        this.context = context;
    }


    public void displayTextViewMessage(String msg) {
        if (errorView == null || !(errorView instanceof TextView)) return;

        TextView textView = (TextView) errorView;
        textView.setFocusable(true);
        textView.setClickable(true);
        textView.setFocusableInTouchMode(true);
        textView.setError(msg);
        textView.requestFocus();
    }


    public void displayTextViewErrorMessage(Throwable error) {
        if (errorView == null || !(errorView instanceof TextView)) return;

        TextView textView = (TextView) errorView;
        textView.setFocusable(true);
        textView.setClickable(true);
        textView.setFocusableInTouchMode(true);
        textView.setError(getCentralizedErrorMessage(error));
        textView.requestFocus();
    }


    private String getCentralizedErrorMessage(Throwable error) {
        if (error == null) return null;
        int characterLimit = 200;
        error.printStackTrace();

        String errorMsg = "";
        if (error instanceof CustomException) {
            errorMsg = error.getMessage();
            characterLimit = errorMsg.length();
        } else if (error instanceof JSONException) {
            errorMsg = context.getString(R.string.json_error);
        } else if (error instanceof TimeoutError) {
            errorMsg = context.getString(R.string.timeout_error);
        } else if (error instanceof VolleyError) {
            if (error instanceof ClientError) {
                int statusCode = ((ClientError) error).networkResponse.statusCode;

                if (statusCode == 400) {
                    errorMsg = context.getString(R.string.wrong_username_error);
                } else if (statusCode == 404)
                    errorMsg = context.getString(R.string.invalid_scan_error);

            } else if (error instanceof AuthFailureError) {
                errorMsg = context.getString(R.string.authfailure_error);
            } else {
                errorMsg = context.getString(R.string.connection_error);
            }
        }
        error.printStackTrace();

        String exceptionMsg = "";
        String fullExceptionMsg = error.getMessage();
        if (fullExceptionMsg != null)
            exceptionMsg = "\n" + fullExceptionMsg.substring(0, Math.min(fullExceptionMsg.length(), characterLimit)) + "....";

        return errorMsg + exceptionMsg;
    }


    public void reset() {
        if (errorView == null || !(errorView instanceof TextView)) return;

        TextView textView = (TextView) errorView;
        textView.setError(null);
    }
}

