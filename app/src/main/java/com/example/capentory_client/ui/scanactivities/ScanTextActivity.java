package com.example.capentory_client.ui.scanactivities;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.capentory_client.R;
import com.example.capentory_client.ui.scanactivities.modifiedgoogleapi.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ScanTextActivity extends AppCompatActivity {
    private static final Integer CERTAINTY_THRESHOLD_SCAN_AMOUNT = 3;
    private static final Integer MIN_LENGTH_OF_BARCODE = 8;
    private String TAG = "OPT";
    private SurfaceView cameraPreview;
    private TextView textPreview;
    private CameraSource cameraSource;
    private StringBuilder readMessage = new StringBuilder();
    private Button btnUnlock;
    private boolean useFlash = false;

    private static final int requestPermissionID = 101;
    private int longestCode = 0;
    private int textFilterMode;
    private Map<String, Integer> map = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_text);

        cameraPreview = findViewById(R.id.surfaceView_activity_scan_text);
        textPreview = findViewById(R.id.text_preview_activity_scan_text);
        textFilterMode = getTextFilterMode();
        btnUnlock = findViewById(R.id.unlock_button_activity_scan_text);
        startCameraSource();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != requestPermissionID) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA},
                            ScanBarcodeActivity.MY_PERMISSION_REQUEST_CAMERA);
                    return;
                }
                cameraSource.start(cameraPreview.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startCameraSource() {

        //Create the TextRecognizer
        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!textRecognizer.isOperational()) {
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
            }
        } else {
            cameraSource = new CameraSource.Builder(this, textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1600, 1024)
                    .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO).build();


            cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {

                        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(ScanTextActivity.this, new String[]{Manifest.permission.CAMERA}, requestPermissionID);
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

            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {
                    cameraSource.release();
                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if (items.size() != 0) {
                        textPreview.post(new Runnable() {
                            @Override
                            public void run() {
                                if (textFilterMode != 3)
                                    readMessage = new StringBuilder();

                                for (int i = 0; i < items.size(); i++) {
                                    //Get current string from text block
                                    String currentString = items.valueAt(i).getValue();
                                    Log.e(TAG, currentString + " => " + getMostFrequentCode() + "/" + map.get(getMostFrequentCode()));
                                    currentString = getFilteredString(i, currentString, textFilterMode, textRecognizer);

                                    if (textFilterMode == 3) {
                                        //Barcode Optimization
                                        fillMap(currentString);
                                        String mostFrequentCode = getMostFrequentCode();
                                        if (determineDisplayedCode(i, currentString, mostFrequentCode, textRecognizer))
                                            break;
                                    }

                                    if (!currentString.isEmpty()) {
                                        readMessage.append(currentString).append("\n");
                                    }
                                }
                                textPreview.setText(readMessage.toString());
                            }
                        });
                    }
                }
            });
        }
    }

    private String getMostFrequentCode() {
        String mostFrequentCode = "";
        if (!map.isEmpty()) {
            int maxValueInMap = (Collections.max(map.values()));
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                if (entry.getValue() == maxValueInMap) {
                    mostFrequentCode = entry.getKey();
                }
            }
        }
        return mostFrequentCode;
    }

    private boolean determineDisplayedCode(int i, String currentString, String mostFrequentCode, TextRecognizer textRecognizer) {
        String regex = getRegexForMode(textFilterMode);

        //After certainty threshold display the most frequent rather than the longest code
        if ((Collections.max(map.values())) > CERTAINTY_THRESHOLD_SCAN_AMOUNT) {
            readMessage.setLength(0);
            readMessage.append(mostFrequentCode).append("\n");
            if (btnUnlock.getVisibility() == View.GONE) {
                btnUnlock.setVisibility(View.VISIBLE);
            }
            return true;
        } else {
            if (currentString.length() > longestCode) {
                readMessage.setLength(0);
                longestCode = currentString.length();
                if (currentString.matches(regex)) {
                    //Focus on longest matching string
                    textRecognizer.setFocus(i);
                }
            } else {
                return true;
            }
        }
        return false;
    }

    private void fillMap(String currentString) {
        if (map.containsKey(currentString)) {
            Integer c = map.get(currentString);
            if (c == null)
                c = 0;
            if (currentString.length() < MIN_LENGTH_OF_BARCODE) return;
            c++;
            map.put(currentString, c);
        } else map.put(currentString, 1);
    }


    private String getFilteredString(int i, String currentString, int textFilterMode, TextRecognizer textRecognizer) {
        String regexFilter = getRegexForMode(textFilterMode);
        //No filter
        if (textFilterMode == 0) return currentString;

        //Numeric optimization for barcodes (has its own setFocus optimization)
        if (textFilterMode == 3) {
            return optimizeForNumericalBarcode(currentString);
        } else if (currentString.matches(regexFilter)) {
            textRecognizer.setFocus(i);
        }

        // Take the current string and make a regexFilter confirm string out of it
        currentString = currentString.replaceAll(getAntiRegexForMode(textFilterMode), "").trim();
        return currentString;
    }

    @NonNull
    private String optimizeForNumericalBarcode(String currentString) {
        double count = 0;
        for (char c : currentString.toCharArray()) {
            if (c >= '0' && c <= '9') {
                count++;
            }
        }

        double length = currentString.length();
        // If less than 65% of the characters are digits correction probably is not possible, or the original text is a mixture of digits and characters
        if (count / length < 0.65)
            return currentString.replaceAll(getAntiRegexForMode(textFilterMode), "");

        // https://stackoverflow.com/questions/5455794/removing-whitespace-from-strings-in-java
        return currentString
                .replaceAll("O", "0")
                .replaceAll("o", "0")
                .replaceAll("c", "0")
                .replaceAll("d", "0")
                .replaceAll("D", "0")
                .replaceAll("B", "8")
                .replaceAll("S", "5")
                .replaceAll("Z", "2")
                .replaceAll("b", "6")
                .replaceAll("G", "6")
                .replaceAll(getAntiRegexForMode(textFilterMode), "");
    }

    private int getTextFilterMode() {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int text_filter_mode;
        try {
            text_filter_mode = Integer.parseInt(preference.getString("text_filter_mode", "-1"));
        } catch (NullPointerException | NumberFormatException e) {
            text_filter_mode = 0;
        }
        return text_filter_mode;
    }

    @NonNull
    private String getRegexForMode(int text_filter_mode) {
        switch (text_filter_mode) {
            case 1:
                //Alphanumerisch(Deutsch)
                return "[ÄäÖöÜüßßA-Za-z0-9 ]+";

            case 2:
                //Alphabetisch (Deutsch)
                return "[ÄäÖöÜüßßA-Za-z ]+";
            case 3:
                //Nummern (z.B. Für Barcodes)
                return "[0-9 ]+";
            case 4:
                // IPs
                return "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                        + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                        + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                        + "|[1-9][0-9]|[0-9]))";

            default:
                //Kein Filter
                return ".*";
        }
    }


    @NonNull
    private String getAntiRegexForMode(int text_filter_mode) {
        switch (text_filter_mode) {
            case 1:
                //Alphanumerisch(Deutsch)
                return "[^ÄäÖöÜüßßA-Za-z0-9 ]";

            case 2:
                //Alphabetisch (Deutsch)
                return "[ÄäÖöÜüßßA-Za-z ]";
            case 3:
                //Nummern (z.B. Für Barcodes)
                return "[^0-9]";
            case 4:
                // IPs
                return "[^0-9.]";
            default:
                return "";
        }
    }

    public void copyText(View view) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String msg = readMessage.toString().trim();
                if (msg.isEmpty()) return;
                ClipboardManager clipboard = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text", msg);
                clipboard.setPrimaryClip(clip);

                Toast toast = Toast.makeText(getBaseContext(), "Kopiert! \n" + msg, Toast.LENGTH_LONG);
                TextView v = toast.getView().findViewById(android.R.id.message);
                if (v != null) v.setGravity(Gravity.CENTER);

                toast.show();
            }
        });
        finish();
    }

    public void unlock(View view) {
        map.clear();
        longestCode = 0;
        view.setVisibility(View.GONE);
    }

    public void toggleFlashText(View view) {
        useFlash = !useFlash;
        if (useFlash) {
            cameraSource.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            ((ImageButton) view).setImageResource(R.drawable.ic_flash_off_white_24dp);
        } else {
            cameraSource.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            ((ImageButton) view).setImageResource(R.drawable.ic_flash_on_white_24dp);
        }
    }
}