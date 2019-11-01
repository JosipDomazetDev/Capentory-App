package com.example.capentory_client.ui.errorhandling;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.ClientError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.example.capentory_client.R;
import com.example.capentory_client.androidutility.PreferenceUtility;
import com.example.capentory_client.androidutility.ToastUtility;

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


    public void displayTextViewMessage(Throwable error) {
        if (errorView == null || !(errorView instanceof TextView)) return;

        TextView textView = (TextView) errorView;
        textView.setFocusable(true);
        textView.setClickable(true);
        textView.setFocusableInTouchMode(true);
        textView.setError(getCentralizedErrorMessage(error, context));
        textView.requestFocus();
    }


    private String getCentralizedErrorMessage(Throwable error, Context context) {
        if (error == null) return null;
        error.printStackTrace();

        String errorMsg = "";
        if (error instanceof JSONException) {
            error.printStackTrace();
            errorMsg = "Server verwendet ein nicht unterstütztes JSON-Format!";
        } else if (error instanceof TimeoutError) {
            errorMsg = "Zeitüberschreitungsfehler ist aufgetreten!";
            error.printStackTrace();
        } else if (error instanceof VolleyError) {
            if (error instanceof ClientError && ((ClientError) error).networkResponse.statusCode != 404) {
                errorMsg = "Benutzername oder Passwort war falsch!";
                error.printStackTrace();
            } else if (error instanceof AuthFailureError) {
                errorMsg = "Credentials sind ungültig oder Sie haben zu wenige Rechte. Bitte versuchen Sie sich nochmal anzumelden!";
                error.printStackTrace();
            } else {
                errorMsg = "Ein Verbindungsfehler ist aufgetreten!";
                error.printStackTrace();
            }
        }

        String exceptionMsg = "";
        String fullExceptionMsg = error.getMessage();
        if (fullExceptionMsg != null)
            exceptionMsg = "\n" + fullExceptionMsg.substring(0, Math.min(fullExceptionMsg.length(), 100)) + "....";

        return errorMsg + exceptionMsg;
    }


    public void reset() {
        if (errorView == null || !(errorView instanceof TextView)) return;

        TextView textView = (TextView) errorView;
        textView.setError(getCentralizedErrorMessage(null, context));
    }
}

