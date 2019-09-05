package com.example.capentory_client.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.capentory_client.R;

import java.util.Locale;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class HomeScreenFragment extends Fragment {
    private TextToSpeech mTTS;
    //https://stackoverflow.com/questions/5608720/android-preventing-double-click-on-a-button/9950832
    private long lastClickTime = 0;

    private static final String CHANNEL_ID = "inventory_channel_01";


    public HomeScreenFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_screen, container, false);
        createNotificationChannel();

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_start_inventory_fragment_home_screen).setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - lastClickTime < 500) {
                return;
            }
            lastClickTime = SystemClock.elapsedRealtime();

            startInventory();
            Navigation.findNavController(v).navigate(R.id.action_homeScreenFragment_to_roomFragment);
        });

    }

    private void initTTS(View view) {
        final Button ttsBtn = view.findViewById(R.id.btn_start_inventory_fragment_home_screen);

        mTTS = new TextToSpeech(getContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = mTTS.setLanguage(Locale.GERMAN);

                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language not supported");
                } else {
                    ttsBtn.setEnabled(true);
                }
            } else {
                Log.e("TTS", "Init failed");
            }
        });


        ttsBtn.setOnClickListener(v -> speak());
    }


    private void speak() {
        String text = "test";
        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
    }


    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = Objects.requireNonNull(getActivity()).getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    public void startInventory() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(Objects.requireNonNull(getContext()), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_info_outline_black_24dp)
                .setContentTitle("Inventur-Status")
                .setContentText("Inventur läuft! Klicken um zurückzugelangen.")
                .setColor(Color.parseColor("#2196F3"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0,
                intent, 0);
        builder.setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(10, builder.build());

    }


}
