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
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.capentory_client.R;
import com.example.capentory_client.androidutility.ToastUtility;
import com.example.capentory_client.models.SerializerEntry;
import com.example.capentory_client.repos.StocktakingRepository;
import com.example.capentory_client.ui.errorhandling.BasicNetworkErrorHandler;
import com.example.capentory_client.viewmodels.StocktakingViewModel;
import com.example.capentory_client.viewmodels.ViewModelProviderFactory;
import com.example.capentory_client.viewmodels.adapter.DropDownSerializerAdapter;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

/**
 * A simple {@link Fragment} subclass.
 */
public class StocktakingFragment extends NetworkFragment<List<SerializerEntry>, StocktakingRepository, StocktakingViewModel> {
    private Spinner serializerDropDown;
    private static final String CHANNEL_ID = "inventory_channel_01";
    public static final int NOTIFICATION_INV_STARTED_ID = 10;

    public StocktakingFragment() {
        // Required empty public constructor
    }


    @Inject
    ViewModelProviderFactory providerFactory;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_stocktaking, container, false);
        createNotificationChannel();
        return inflate;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Button btnStocktaking = view.findViewById(R.id.button_fragment_stocktaking);
        serializerDropDown = view.findViewById(R.id.db_dropdown_fragment_stocktaking);

        initWithFetch(ViewModelProviders.of(this, providerFactory).get(StocktakingViewModel.class),
                new BasicNetworkErrorHandler(getContext(), view.findViewById(R.id.dropdown_text_fragment_stocktaking)),
                view,
                R.id.progress_bar_fragment_stocktaking,
                view.findViewById(R.id.content_stocktaking_fragment),
                R.id.swipe_refresh_fragment_stocktaking
        );

        btnStocktaking.setOnClickListener(v -> tryToStartInventory(view));
    }

    private void tryToStartInventory(@NonNull View view) {
        SerializerEntry selectedSerializer = (SerializerEntry) serializerDropDown.getSelectedItem();
        if (selectedSerializer == null) return;

        EditText name = view.findViewById(R.id.name_edittext_stocktacking_fragment_stocktaking);
        EditText comment = view.findViewById(R.id.comment_edittext_stocktacking_fragment_stocktaking);

        if (TextUtils.isEmpty(name.getText())) {
            ToastUtility.displayCenteredToastMessage(getContext(), "Ihre Inventur muss einen Namen haben!", Toast.LENGTH_SHORT);
            return;
        }

        networkViewModel.postStocktaking(name.getText().toString(), comment.getText().toString());
        observeSpecificLiveData(networkViewModel.getPostedStocktaking(), liveData -> {
            if (liveData == null || liveData.getData() == null) return;
            // Stocktaking was created, now we can start with the inventory process itself!

            createStartNotification();
            MainActivity.setStocktaking(liveData.getData());
            MainActivity.setSerializer(selectedSerializer);
            NavHostFragment.findNavController(this).popBackStack();
            Navigation.findNavController(view).navigate(R.id.roomFragment);
        });
    }

    @Override
    protected void handleSuccess(StatusAwareData<List<SerializerEntry>> statusAwareData) {
        super.handleSuccess(statusAwareData);
        DropDownSerializerAdapter adapter = new DropDownSerializerAdapter(Objects.requireNonNull(getContext()), (ArrayList<SerializerEntry>) statusAwareData.getData());
        serializerDropDown.setAdapter(adapter);
        //serializerDropDown.notify();
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


    public void createStartNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(Objects.requireNonNull(getContext()), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_info_outline_white_24dp)
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
        notificationManager.notify(NOTIFICATION_INV_STARTED_ID, builder.build());
    }


}
