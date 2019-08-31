package com.example.capentory_client.ui;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.capentory_client.R;

import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class HomeScreenFragment extends Fragment {
    private TextToSpeech mTTS;


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

        view.findViewById(R.id.btn_start_inventory_fragment_home_screen).setOnClickListener(
                Navigation.createNavigateOnClickListener(R.id.action_homeScreenFragment_to_roomFragment, null)
        );

        //initTTS(view);
        return view;
    }

    private void initTTS(View view) {
        final Button ttsBtn = view.findViewById(R.id.btn_start_inventory_fragment_home_screen);

        mTTS = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
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
            }
        });


        ttsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak();
            }
        });
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

}
