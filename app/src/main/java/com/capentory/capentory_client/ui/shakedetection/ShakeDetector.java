package com.capentory.capentory_client.ui.shakedetection;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.capentory.capentory_client.androidutility.PreferenceUtility;
import com.capentory.capentory_client.ui.SettingsFragment;


public class ShakeDetector implements SensorEventListener {
    private Context context;
    private ShakeListener shakeListener;
    private SensorManager mSensorManager;

    private long lastUpdate = 0;
    private float last_x = 0;
    private float last_y = 0;
    private float last_z = 0;
    private int sensitivity = -2;

    public ShakeDetector(Context context, ShakeListener shakeListener) {
        this.context = context;
        mSensorManager = (SensorManager) context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        this.shakeListener = shakeListener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // https://stackoverflow.com/questions/5271448/how-to-detect-shake-event-with-android
        if (event.sensor.equals(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER))) {
            long curTime = System.currentTimeMillis();
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                if (sensitivity == -2) {
                    sensitivity = getSensitivity();
                } else if (sensitivity == -1) return;

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > sensitivity) {
                    shakeListener.handleShake();
                }
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private int getSensitivity() {
        return Integer.parseInt(PreferenceUtility.getString(context, SettingsFragment.SHAKE_SENSITIVITY_KEY));
    }

    public interface ShakeListener {
        void handleShake();
    }

    public void registerShakeDetector() {
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);
    }

    public void unregisterShakeDetector() {
        mSensorManager.unregisterListener(this);
    }
}
