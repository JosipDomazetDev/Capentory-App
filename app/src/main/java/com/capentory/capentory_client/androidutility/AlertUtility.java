package com.capentory.capentory_client.androidutility;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.capentory.capentory_client.R;

public class AlertUtility {
    private static boolean userWasWarned = false;

    // Private constructor to prevent instantiation
    private AlertUtility() {
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

        //long[] mVibratePattern = {0, 200, 100, 300, 100, 400};
        long[] mVibratePattern = {0, 400, 100, 400};

        v.vibrate(mVibratePattern, -1);


        //playWarningSound(context);
    }


    private static void playWarningSound(Context context) {
        try {
            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

            if (alert == null) {
                // alert is null, using backup
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                // I can't see this ever being null (as always have a default notification)
                // but just incase
                if (alert == null) {
                    // alert backup is null, using 2nd backup
                    alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                }
            }
            Ringtone r = RingtoneManager.getRingtone(context.getApplicationContext(), alert);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showVibratorMessageOnce(Context context) {
        ToastUtility.displayCenteredToastMessage(context, context.getString(R.string.error_vibrate), Toast.LENGTH_LONG);
        userWasWarned = true;
    }
}
