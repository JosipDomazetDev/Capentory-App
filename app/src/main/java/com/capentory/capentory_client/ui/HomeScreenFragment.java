package com.capentory.capentory_client.ui;

import android.os.Bundle;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.capentory.capentory_client.R;
import com.capentory.capentory_client.androidutility.PreferenceUtility;
import com.capentory.capentory_client.models.MergedItem;
import com.capentory.capentory_client.repos.HomeScreenRepository;
import com.capentory.capentory_client.ui.errorhandling.ErrorHandler;
import com.capentory.capentory_client.viewmodels.HomeScreenViewModel;
import com.capentory.capentory_client.viewmodels.ViewModelProviderFactory;

import java.util.Locale;

import javax.inject.Inject;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class HomeScreenFragment extends NetworkFragment<MergedItem, HomeScreenRepository, HomeScreenViewModel> {
    private TextToSpeech mTTS;
    //https://stackoverflow.com/questions/5608720/android-preventing-double-click-on-a-button/9950832
    private long lastClickTime = 0;

    @Inject
    ViewModelProviderFactory providerFactory;


    public HomeScreenFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_screen, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initWithoutFetch(ViewModelProviders.of(this, providerFactory).get(HomeScreenViewModel.class),
                new ErrorHandler(getContext(), view.findViewById(R.id.homescreen_into_text)),
                view,
                R.id.progress_bar_fragment_homescreen);

        view.findViewById(R.id.btn_start_inventory_fragment_home_screen).setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - lastClickTime < 700) {
                return;
            }
            lastClickTime = SystemClock.elapsedRealtime();

            if (PreferenceUtility.isLoggedIn(getContext())) {
                Navigation.findNavController(v).navigate(R.id.action_homeScreenFragment_to_stocktakingFragment);
            } else Navigation.findNavController(v).navigate(R.id.loginFragment);
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



}
