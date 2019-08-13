package com.example.capentory_client;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class ScanBarcodeActivity extends Activity {
    SurfaceView cameraPreview;
    private static final int MY_PERMISSION_REQUEST_CAMERA = 2569;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_barcode);
        cameraPreview = (SurfaceView) findViewById(R.id.camera_preview);
        createCameraSource();



        String url = "http://my-json-feed";

    }


    private void createCameraSource() {
        final BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this).build();
        final CameraSource cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .setAutoFocusEnabled(true).build();
        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {


            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    Activity#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for Activity#requestPermissions for more details.
                        requestPermissions(new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSION_REQUEST_CAMERA);
                        return;
                    }
                    cameraSource.start(cameraPreview.getHolder());
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
                final SparseArray<Barcode> barcodeSparseArray = detections.getDetectedItems();
                if (barcodeSparseArray.size() > 0) {
                    Intent intent = new Intent();
                    intent.putExtra("barcode", barcodeSparseArray.valueAt(0));
                    setResult(CommonStatusCodes.SUCCESS, intent);
                    finish();
                }
            }
        });

    }

}
