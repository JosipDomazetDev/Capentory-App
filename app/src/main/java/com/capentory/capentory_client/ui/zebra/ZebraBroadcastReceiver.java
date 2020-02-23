package com.capentory.capentory_client.ui.zebra;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.capentory.capentory_client.R;
import com.capentory.capentory_client.ui.errorhandling.ErrorHandler;

public class ZebraBroadcastReceiver extends BroadcastReceiver {

    private ErrorHandler errorHandler;
    private ScanListener scanListener;

    public ZebraBroadcastReceiver(ErrorHandler errorHandler, ScanListener scanListener) {
        this.errorHandler = errorHandler;
        this.scanListener = scanListener;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        assert action != null;
        if (action.equals(context.getResources().getString(R.string.activity_intent_filter_action))) {
            //  Received a barcode scan
            String barcode = intent.getStringExtra(context.getResources().getString(R.string.datawedge_intent_key_data));

            try {
                scanListener.handleZebraScan(barcode);
            } catch (Exception e) {
                //  Catch if the UI does not exist when we receive the broadcast
                if (errorHandler != null)
                    errorHandler.displayTextViewMessage(context.getString(R.string.wait_till_scan_ready_error));
            }
        }
    }

    private void unregister() {
        errorHandler = null;
        scanListener = null;
    }

    public static void registerZebraReceiver(Context context, ZebraBroadcastReceiver zebraBroadcastReceiver) {
        if (context == null) return;

        IntentFilter filter = new IntentFilter();
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(context.getResources().getString(R.string.activity_intent_filter_action));
        context.registerReceiver(zebraBroadcastReceiver, filter);
    }


    public static void unregisterZebraReceiver(Context context, ZebraBroadcastReceiver zebraBroadcastReceiver) {
        if (context == null) return;
        zebraBroadcastReceiver.unregister();
        context.unregisterReceiver(zebraBroadcastReceiver);
    }


    public interface ScanListener {
        void handleZebraScan(String barcode);
    }
}
