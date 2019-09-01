package com.example.capentory_client.androidutility;

import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

public final class ToastUtility {
    // Private constructor to prevent instantiation
    private ToastUtility() {
        throw new UnsupportedOperationException();
    }

    public static void displayCenteredToastMessage(Context context, String msg, int duration) {
        if (context == null) return;
        Toast toast = Toast.makeText(context, msg, duration);
        TextView v = toast.getView().findViewById(android.R.id.message);
        if (v != null) v.setGravity(Gravity.CENTER);
        toast.show();
    }
}
