package com.example.capentory_client.ui.errorhandling;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.ClientError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

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
        int characterLimit = 130;
        error.printStackTrace();

        String errorMsg = "";
        if (error instanceof CustomException) {
            errorMsg = error.getMessage();
            characterLimit = errorMsg.length();
        } else if (error instanceof JSONException) {
            errorMsg = "Server verwendet ein nicht unterstütztes JSON-Format!";
        } else if (error instanceof TimeoutError) {
            errorMsg = "Zeitüberschreitungsfehler ist aufgetreten!";
        } else if (error instanceof VolleyError) {
            if (error instanceof ClientError) {
                int statusCode = ((ClientError) error).networkResponse.statusCode;

                if (statusCode == 400) {
                    errorMsg = "Benutzername oder Passwort war falsch!";
                } else if (statusCode == 404)
                    errorMsg = "Unerwarterter Scan!";

            } else if (error instanceof AuthFailureError) {
                errorMsg = "Credentials sind ungültig oder Sie haben zu wenige Rechte. Bitte versuchen Sie sich nochmal anzumelden!";
            } else {
                errorMsg = "Ein Verbindungsfehler ist aufgetreten!";
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

