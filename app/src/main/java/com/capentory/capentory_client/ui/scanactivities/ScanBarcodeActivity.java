package com.capentory.capentory_client.ui.scanactivities;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.capentory.capentory_client.R;
import com.capentory.capentory_client.androidutility.PreferenceUtility;
import com.capentory.capentory_client.androidutility.ToastUtility;
import com.capentory.capentory_client.ui.SettingsFragment;
import com.capentory.capentory_client.ui.scanactivities.modifiedgoogleapi.CameraSource;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

public class ScanBarcodeActivity extends Activity {
    private SurfaceView cameraPreview;
    private boolean utilityModeActivated = false;
    private boolean useFlash = false;
    private CameraSource cameraSource;
    private MediaPlayer mediaPlayer;
    private boolean lockedOnFirst = false;
    public static final String FOCUS_MODE = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
    public static final int[] PREVIEW_SIZE = {1920, 1080};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_barcode);
        if (getIntent().getExtras() != null)
            utilityModeActivated = ScanBarcodeActivityArgs.fromBundle(getIntent().getExtras()).getUtilityModeActivated();

        //PermissionHandler.requestCameraPermission(this);
        mediaPlayer = MediaPlayer.create(this, R.raw.beep);
        cameraPreview = findViewById(R.id.camera_preview);

        PermissionHandler.requestCameraPermission(this);

        if (PermissionHandler.checkPermission(this)) {
            startCameraSource();
        } else cameraPreview.setVisibility(View.INVISIBLE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != PermissionHandler.MY_PERMISSION_REQUEST_CAMERA) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (PermissionHandler.verifyPermissions(grantResults)) {
            startCameraSource();
            cameraPreview.setVisibility(View.VISIBLE);
        } else {
            finish();
        }
    }


  /*  private void startCameraSource2() {


        FirebaseVisionBarcodeDetectorOptions options =
                new FirebaseVisionBarcodeDetectorOptions.Builder()
                        .setBarcodeFormats(
                                FirebaseVisionBarcode.FORMAT_CODE_93,
                                FirebaseVisionBarcode.FORMAT_CODE_39,
                                FirebaseVisionBarcode.FORMAT_AZTEC)
                        .build();
       *//* com.google.android.gms.vision.CameraSource cameraSource = new com.google.android.gms.vision.CameraSource.Builder(this, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .setAutoFocusEnabled(true).build();*//*

        FirebaseVisionBarcodeDetector barcodeDetector =         FirebaseVision.getInstance().getVisionBarcodeDetector(options);

        PreviewConfi




        if (!barcodeDetector.isOperational()) {
            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.

            IntentFilter lowStorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowStorageFilter) != null;

            if (hasLowStorage) {
                ToastUtility.displayCenteredToastMessage(this, getString(R.string.low_storage_error_activity_scanners), Toast.LENGTH_LONG);
            }
        } else {
            if (PreferenceUtility.getBoolean(ScanBarcodeActivity.this, SettingsFragment.LIGHTNING_KEY, true)) {
                cameraSource = new CameraSource.Builder(this, barcodeDetector)
                        .setFacing(CameraSource.CAMERA_FACING_BACK)
                        .setFlashMode(Camera.Parameters.FLASH_MODE_TORCH)
                        .setRequestedPreviewSize(1600, 1024)
                        .setFocusMode(FOCUS_MODE).build();

                *//*List<android.hardware.Camera.Size> supportedPreviewSizes =
                        Camera.Parameters.getSupportedPreviewSizes();*//*

                ((ImageButton) findViewById(R.id.btn_flash_activity_scan_barcode)).setImageResource(R.drawable.ic_flash_off_white_24dp);
                useFlash = true;
            } else {
                cameraSource = new CameraSource.Builder(this, barcodeDetector)
                        .setFacing(CameraSource.CAMERA_FACING_BACK)
                        .setFlashMode(Camera.Parameters.FLASH_MODE_OFF)
                        .setRequestedPreviewSize(1600, 1024)
                        .setFocusMode(FOCUS_MODE).build();

                ((ImageButton) findViewById(R.id.btn_flash_activity_scan_barcode)).setImageResource(R.drawable.ic_flash_on_white_24dp);
                useFlash = false;
            }


            cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {

                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {
                        if (PermissionHandler.checkPermission(ScanBarcodeActivity.this)) {
                            cameraSource.start(cameraPreview.getHolder());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    cameraSource.stop();
                }
            });

            barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {

                @Override
                public void release() {
                }

                @Override
                public void receiveDetections(Detector.Detections<Barcode> detections) {
                    if (lockedOnFirst) return;

                    final SparseArray<Barcode> barcodeSparseArray = detections.getDetectedItems();
                    if (barcodeSparseArray.size() > 0) {
                        lockedOnFirst = true;
                        Intent intent = new Intent();

                        final String barcode = barcodeSparseArray.valueAt(0).rawValue;
                        final String format = getGoogleBarcodeFormat(barcodeSparseArray.valueAt(0).format);

                        intent.putExtra("barcode", barcode);
                        setResult(CommonStatusCodes.SUCCESS, intent);
                        startBeep();
                        finish();

                        if (utilityModeActivated) {
                            utilityCopyToClipboard(barcode, format);
                        }
                    }
                }
            });
        }

    }*/


    private void startCameraSource() {
        final BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(getApplicationContext()).setBarcodeFormats(getSelectedFormats()).build();

       /* com.google.android.gms.vision.CameraSource cameraSource = new com.google.android.gms.vision.CameraSource.Builder(this, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .setAutoFocusEnabled(true).build();*/

        if (!barcodeDetector.isOperational()) {
            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.

            IntentFilter lowStorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowStorageFilter) != null;

            if (hasLowStorage) {
                ToastUtility.displayCenteredToastMessage(this, getString(R.string.low_storage_error_activity_scanners), Toast.LENGTH_LONG);
            }
        } else {
            if (PreferenceUtility.getBoolean(ScanBarcodeActivity.this, SettingsFragment.LIGHTNING_KEY, true)) {
                cameraSource = new CameraSource.Builder(this, barcodeDetector)
                        .setFacing(CameraSource.CAMERA_FACING_BACK)
                        .setFlashMode(Camera.Parameters.FLASH_MODE_TORCH)
                        .setRequestedPreviewSize(PREVIEW_SIZE[0], PREVIEW_SIZE[1])
                        .setFocusMode(FOCUS_MODE).build();

                /*List<android.hardware.Camera.Size> supportedPreviewSizes =
                        Camera.Parameters.getSupportedPreviewSizes();*/

                ((ImageButton) findViewById(R.id.btn_flash_activity_scan_barcode)).setImageResource(R.drawable.ic_flash_off_white_24dp);
                useFlash = true;
            } else {
                cameraSource = new CameraSource.Builder(this, barcodeDetector)
                        .setFacing(CameraSource.CAMERA_FACING_BACK)
                        .setFlashMode(Camera.Parameters.FLASH_MODE_OFF)
                        .setRequestedPreviewSize(PREVIEW_SIZE[0], PREVIEW_SIZE[1])
                        .setFocusMode(FOCUS_MODE).build();

                ((ImageButton) findViewById(R.id.btn_flash_activity_scan_barcode)).setImageResource(R.drawable.ic_flash_on_white_24dp);
                useFlash = false;
            }


            cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {

                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {
                        if (PermissionHandler.checkPermission(ScanBarcodeActivity.this)) {
                            cameraSource.start(cameraPreview.getHolder());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    cameraSource.stop();
                }
            });

            barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {


                @Override
                public void release() {
                }

                @Override
                public void receiveDetections(Detector.Detections<Barcode> detections) {
                    if (lockedOnFirst) return;

                    SparseArray<Barcode> barcodeSparseArray = detections.getDetectedItems();
                    if (barcodeSparseArray.size() > 0) {
                        lockedOnFirst = true;
                        Intent intent = new Intent();

                        String barcode = barcodeSparseArray.valueAt(0).rawValue;
                        String format = getGoogleBarcodeFormat(barcodeSparseArray.valueAt(0).format);

                        intent.putExtra("barcode", barcode);
                        setResult(CommonStatusCodes.SUCCESS, intent);
                        startBeep();
                        finish();

                        if (utilityModeActivated) {
                            utilityCopyToClipboard(barcode, format);
                        }
                    }
                }
            });
        }

    }

    private void utilityCopyToClipboard(final String barcode, final String format) {
        runOnUiThread(() -> {
            ClipboardManager clipboard = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("barcode", barcode);
            clipboard.setPrimaryClip(clip);

            String msg = getString(R.string.copy_message_scanbarcode_activity, barcode, format);

            ToastUtility.displayCenteredToastMessage(getBaseContext(), msg, Toast.LENGTH_LONG);
        });
    }

    private String getGoogleBarcodeFormat(int format) {
        switch (format) {
            case Barcode.CODE_128:
                return "CODE_128";
            case Barcode.CODE_39:
                return "CODE_39";
            case Barcode.CODE_93:
                return "CODE_93";
            case Barcode.CODABAR:
                return "CODABAR";
            case Barcode.DATA_MATRIX:
                return "DATA_MATRIX";
            case Barcode.EAN_13:
                return "EAN_13";
            case Barcode.EAN_8:
                return "EAN_8";
            case Barcode.ITF:
                return "ITF";
            case Barcode.QR_CODE:
                return "QR_CODE";
            case Barcode.UPC_A:
                return "UPC_A";
            case Barcode.UPC_E:
                return "UPC_E";
            case Barcode.PDF417:
                return "PDF417";
            case Barcode.AZTEC:
                return "AZTEC";
            default:
                return "Unbekannt";
        }
    }

    public void toggleFlash(View view) {
        if (useFlash) {
            cameraSource.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            ((ImageButton) view).setImageResource(R.drawable.ic_flash_on_white_24dp);
            useFlash = false;
        } else {
            cameraSource.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            ((ImageButton) view).setImageResource(R.drawable.ic_flash_off_white_24dp);
            useFlash = true;
        }
    }

    private int getSelectedFormats() {
        if (utilityModeActivated) return 0;
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int supportedBarcodeFormats = 0;

        try {
            Set<String> barcodeFormatsString = preference.getStringSet(SettingsFragment.BARCODE_FORMATS_KEY, Collections.singleton("0"));
            assert barcodeFormatsString != null;

            for (String barcodeFormat : barcodeFormatsString) {
                if (barcodeFormat.equals("0")) return 0;

                supportedBarcodeFormats |= Integer.parseInt(barcodeFormat);
            }
        } catch (NullPointerException | NumberFormatException e) {
            supportedBarcodeFormats = 0;
        }

        return supportedBarcodeFormats;
    }

    public void startBeep() {
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = MediaPlayer.create(this, R.raw.beep);
            }

            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null)
            mediaPlayer.release();
        mediaPlayer = null;
    }
}
