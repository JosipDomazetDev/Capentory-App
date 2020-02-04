package com.capentory.capentory_client.ui;


import android.app.Dialog;
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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.capentory.capentory_client.R;
import com.capentory.capentory_client.androidutility.PopUtility;
import com.capentory.capentory_client.androidutility.ToastUtility;
import com.capentory.capentory_client.models.MergedItem;
import com.capentory.capentory_client.models.SerializerEntry;
import com.capentory.capentory_client.models.Stocktaking;
import com.capentory.capentory_client.repos.StocktakingRepository;
import com.capentory.capentory_client.ui.errorhandling.ErrorHandler;
import com.capentory.capentory_client.ui.scanactivities.ScanBarcodeActivity;
import com.capentory.capentory_client.ui.zebra.ZebraBroadcastReceiver;
import com.capentory.capentory_client.viewmodels.StocktakingViewModel;
import com.capentory.capentory_client.viewmodels.ViewModelProviderFactory;
import com.capentory.capentory_client.viewmodels.adapter.GenericDropDownAdapter;
import com.capentory.capentory_client.viewmodels.wrappers.StatusAwareData;
import com.google.android.gms.common.api.CommonStatusCodes;

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
    static final int NOTIFICATION_INV_STARTED_ID = 10;
    private Spinner stocktakingDropDown;
    private ZebraBroadcastReceiver zebraBroadcastReceiver = new ZebraBroadcastReceiver(errorHandler, barcode -> {
        setSerializer();
        networkViewModel.fetchSpecificallySearchedForItem(barcode);
        observeSpecificLiveData(networkViewModel.getSpecificallySearchedForItem(), liveData ->
                showPopup(liveData.getData(), new Dialog(Objects.requireNonNull(getContext()))));
    });
    ;


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
        serializerDropDown = view.findViewById(R.id.db_dropdown_serializer_fragment_stocktaking);
        stocktakingDropDown = view.findViewById(R.id.db_dropdown_stocktaking_fragment_stocktaking);
        Log.e("hhjheeeeee", "eeeeeefefefeeeeeeeeee");
        Log.e("hhjheeeeeeeeee", "eeeeeefefefeeeeeeeeee");
        Log.e("hhjheeeeeeeeee", "eeeeeefefefeeeeeeeeee");
        Log.e("hhj34ee", "eeeeeefefefeeeeeeeeee");
        Log.e("24343hhjheeeeee", "eeeeeefefefeeeeeeeeee");
        Log.e("24343hhjheeeeee", "eeeeeeeeeefefefeeeeeeeeee");

        initWithFetch(ViewModelProviders.of(this, providerFactory).get(StocktakingViewModel.class),
                new ErrorHandler(getContext(), view.findViewById(R.id.dropdown_text_fragment_stocktaking)),
                view,
                R.id.progress_bar_fragment_stocktaking,
                view.findViewById(R.id.content_stocktaking_fragment),
                R.id.swipe_refresh_fragment_stocktaking
        );

        //networkViewModel.postStocktaking(name.getText().toString(), comment.getText().toString());
        networkViewModel.fetchStocktakings();
        observeSpecificLiveData(networkViewModel.getStocktakings(), liveData -> {
            if (liveData == null || liveData.getData() == null) return;

            // Stocktaking was created, now we can start with the inventory process itself!
            if (liveData.getData().isEmpty())
                ToastUtility.displayCenteredToastMessage(getContext(), getString(R.string.error_missing_inventory_fragment_stocktaking), Toast.LENGTH_LONG);


            GenericDropDownAdapter<Stocktaking> adapter =
                    new GenericDropDownAdapter<>(Objects.requireNonNull(getContext()), (ArrayList<Stocktaking>) liveData.getData());
            stocktakingDropDown.setAdapter(adapter);


        });

        btnStocktaking.setOnClickListener(v -> tryToStartInventory(view));


        view.findViewById(R.id.button_specific_search_fragment_stocktaking).setOnClickListener(v -> {
            setSerializer();
            Intent intent = new Intent(getContext(), ScanBarcodeActivity.class);
            startActivityForResult(intent, 0);
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 0) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    String barcode = data.getStringExtra("barcode");
                    networkViewModel.fetchSpecificallySearchedForItem(barcode);
                    observeSpecificLiveData(networkViewModel.getSpecificallySearchedForItem(), liveData ->
                            showPopup(liveData.getData(), new Dialog(Objects.requireNonNull(getContext()))));
                } else {
                    Toast.makeText(getContext(), getString(R.string.scan_failed_scan_fragments), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    public void showPopup(MergedItem mergedItem, Dialog dialog) {
        hideProgressBarAndShowContent();
        dialog.setContentView(R.layout.itempopup);

        dialog.setCancelable(true);
        dialog.findViewById(R.id.ok_popup).setOnClickListener(v -> dialog.dismiss());
        TextView titel = dialog.findViewById(R.id.titel_popup);

        if (mergedItem.isNewItem()) {
            titel.setText(getString(R.string.not_found_titel_popup));
            dialog.findViewById(R.id.content_popup).setVisibility(View.GONE);
            TextView status = dialog.findViewById(R.id.status_popup);
            status.setVisibility(View.VISIBLE);
            status.setText(getString(R.string.status_popup, mergedItem.getBarcode()));
        } else {
            titel.setText(getString(R.string.found_titel_popup));

            TextView room = dialog.findViewById(R.id.room_popup);
            TextView barcode = dialog.findViewById(R.id.barcode_popup);
            TextView displayname = dialog.findViewById(R.id.displayname_popup);
            TextView displaydesc = dialog.findViewById(R.id.displaydescription_popup);

            room.setText(PopUtility.getHTMLFromStringRessources(R.string.room_popup, mergedItem.getDescriptionaryRoom(), getContext()));
            barcode.setText(PopUtility.getHTMLFromStringRessources(R.string.barcode_popup, mergedItem.getBarcode(), getContext()));
            displayname.setText(PopUtility.getHTMLFromStringRessources(R.string.displayname_popup, mergedItem.getDisplayName(), getContext()));
            displaydesc.setText(PopUtility.getHTMLFromStringRessources(R.string.displaydescription_popup, mergedItem.getDisplayDescription(), getContext()));
        }

        dialog.show();
    }

    private void tryToStartInventory(@NonNull View view) {
        if (!setSerializer()) return;
        if (!setStocktaking()) return;

        createStartNotification();
        NavHostFragment.findNavController(this).popBackStack();
        Navigation.findNavController(view).navigate(R.id.roomFragment);
    }

    private boolean setStocktaking() {
        Stocktaking selectedStocktaking = (Stocktaking) stocktakingDropDown.getSelectedItem();
        if (selectedStocktaking == null) {
            ToastUtility.displayCenteredToastMessage(getContext(), getString(R.string.error_missing_inventory_fragment_stocktaking), Toast.LENGTH_LONG);
            return false;
        }
        MainActivity.setStocktaking(selectedStocktaking);
        return true;
    }

    private boolean setSerializer() {
        SerializerEntry selectedSerializer = (SerializerEntry) serializerDropDown.getSelectedItem();
        if (selectedSerializer == null) {
            ToastUtility.displayCenteredToastMessage(getContext(), getString(R.string.error_missing_serializer_fragment_stocktaking), Toast.LENGTH_LONG);
            return false;
        }
        MainActivity.setSerializer(selectedSerializer);
        return true;
    }

    @Override
    protected void handleSuccess(StatusAwareData<List<SerializerEntry>> statusAwareData) {
        super.handleSuccess(statusAwareData);
        if (statusAwareData.getData() == null) return;
        if (statusAwareData.getData().isEmpty())
            ToastUtility.displayCenteredToastMessage(getContext(),
                    getString(R.string.error_missing_serializer_fragment_stocktaking), Toast.LENGTH_LONG);

        GenericDropDownAdapter<SerializerEntry> adapter = new GenericDropDownAdapter<>(Objects.requireNonNull(getContext()), (ArrayList<SerializerEntry>) statusAwareData.getData());
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
                .setContentTitle(getString(R.string.title_notification_fragment_stocktaking))
                .setContentText(getString(R.string.msg_notification_fragment_stocktaking))
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


    @Override
    public void onResume() {
        super.onResume();
        ZebraBroadcastReceiver.registerZebraReceiver(getContext(), zebraBroadcastReceiver);
    }


    @Override
    public void onPause() {
        super.onPause();
        ZebraBroadcastReceiver.unregisterZebraReceiver(getContext(), zebraBroadcastReceiver);
    }

}
