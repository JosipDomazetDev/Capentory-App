package com.example.capentory_client.ui.scan_activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capentory_client.R;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.util.List;

import info.androidhive.barcode.BarcodeReader;

public class FlashScanBarcodeActivity extends AppCompatActivity implements BarcodeReader.BarcodeReaderListener {


    private BarcodeReader barcodeReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_barcode_scan_acitivity);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        barcodeReader = (BarcodeReader) getSupportFragmentManager().findFragmentById(R.id.barcode_fragment);
    }


    @Override
    public void onScanned(Barcode barcode) {
        // play beep sound
        barcodeReader.playBeep();
        Intent intent = new Intent();
        intent.putExtra("barcode", String.valueOf(barcode.displayValue));

        setResult(CommonStatusCodes.SUCCESS, intent);
        finish();
    }

    @Override
    public void onScannedMultiple(List<Barcode> list) {

    }

    @Override
    public void onBitmapScanned(SparseArray<Barcode> sparseArray) {

    }

    @Override
    public void onScanError(String s) {

    }

    @Override
    public void onCameraPermissionDenied() {
        Toast.makeText(getApplicationContext(), "Camera permission denied!", Toast.LENGTH_LONG).show();
    }
}
