package com.capentory.capentory_client.androidutility;

import android.content.Context;
import android.os.Vibrator;
import android.widget.Toast;

import com.capentory.capentory_client.R;

public class VibrateUtility {
    private static boolean userWasWarned = false;

    // Private constructor to prevent instantiation
    private VibrateUtility() {
        throw new UnsupportedOperationException();
    }

    public static void makeNormalVibration(Context context) {
        if (context == null) return;

        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (!userWasWarned) {
            // Only execute this once
            if (!v.hasVibrator()) {
                showVibratorMessageOnce(context);
            }
        }

        v.vibrate(500);
    }

    private static void showVibratorMessageOnce(Context context) {
        ToastUtility.displayCenteredToastMessage(context, context.getString(R.string.error_vibrate), Toast.LENGTH_LONG);
        userWasWarned = true;
    }
}
