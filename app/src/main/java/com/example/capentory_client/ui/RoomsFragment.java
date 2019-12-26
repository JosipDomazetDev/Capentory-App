package com.example.capentory_client.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.capentory_client.R;
import com.example.capentory_client.androidutility.ToastUtility;
import com.example.capentory_client.models.Room;
import com.example.capentory_client.repos.RoomsRepository;
import com.example.capentory_client.ui.errorhandling.BasicNetworkErrorHandler;
import com.example.capentory_client.ui.scanactivities.ScanBarcodeActivity;
import com.example.capentory_client.viewmodels.RoomViewModel;
import com.example.capentory_client.viewmodels.ViewModelProviderFactory;
import com.example.capentory_client.viewmodels.adapter.GenericDropDownAdapter;
import com.example.capentory_client.viewmodels.sharedviewmodels.RoomxItemSharedViewModel;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;
import com.google.android.gms.common.api.CommonStatusCodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 */
public class RoomsFragment extends NetworkFragment<List<Room>, RoomsRepository, RoomViewModel> {
    private Spinner roomDropDown;
    private RoomxItemSharedViewModel roomxItemSharedViewModel;
    private TextView finishedText;
    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            assert action != null;
            if (action.equals(getResources().getString(R.string.activity_intent_filter_action))) {
                //  Received a barcode scan
                try {
                    String barcode = intent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_data));
                    navigateByBarcode(barcode);
                } catch (Exception e) {
                    //  Catch if the UI does not exist when we receive the broadcast
                    basicNetworkErrorHandler.displayTextViewMessage(getString(R.string.wait_till_scan_ready_error));
                }
            }
        }
    };


    public RoomsFragment() {
        // Required empty public constructor
    }

    @Inject
    ViewModelProviderFactory providerFactory;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rooms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            final Button chooseRoomButton = view.findViewById(R.id.choose_room_button_fragment_room);
            final Button endInventoryButton = view.findViewById(R.id.end_inventory_button_fragment_room);

            roomDropDown = view.findViewById(R.id.room_dropdown_fragment_room);
            finishedText = view.findViewById(R.id.no_rooms_fragment_rooms);
            ((TextView) view.findViewById(R.id.started_stocktaking_text_fragment_actualroom)).setText(
                    getString(R.string.started_inventory_fragment_rooms, MainActivity.getStocktaking(getContext()).getName()));

            initWithFetch(ViewModelProviders.of(this, providerFactory).get(RoomViewModel.class),
                    new BasicNetworkErrorHandler(getContext(), view.findViewById(R.id.dropdown_text_fragment_actualroom)),
                    view,
                    R.id.progress_bar_fragment_actualrooms,
                    view.findViewById(R.id.cardview_rooms_dropdown_fragment_rooms),
                    R.id.swipe_refresh
            );


            roomxItemSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(RoomxItemSharedViewModel.class);
            chooseRoomButton.setOnClickListener(v -> {
                Room selectedRoom = (Room) roomDropDown.getSelectedItem();
                if (selectedRoom == null) return;
                roomxItemSharedViewModel.setCurrentRoom(selectedRoom);
                Navigation.findNavController(view).navigate(R.id.action_roomFragment_to_itemsFragment);
            });

            roomxItemSharedViewModel.getCurrentRoomValidated().observe(getViewLifecycleOwner(), b -> {
                if (b) {
                    networkViewModel.finishRoom();
                    roomxItemSharedViewModel.setCurrentRoomValidated(false);
                }
            });


            requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    handleFinishInventory();
                }
            });

            endInventoryButton.setOnClickListener(v -> handleFinishInventory());

            view.findViewById(R.id.scan_room_floatingbtn_fragment_room).setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), ScanBarcodeActivity.class);
                startActivityForResult(intent, 0);
            });
        } catch (Exception e) {
            ((TextView) view.findViewById(R.id.started_stocktaking_text_fragment_actualroom)).setText(getString(R.string.inventory_finished_fragment_rooms));
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 0) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    String barcode = data.getStringExtra("barcode");
                    navigateByBarcode(barcode);
                } else {
                    Toast.makeText(getContext(), getString(R.string.scan_failed_scan_fragments), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void navigateByBarcode(String barcode) {
        StatusAwareData<List<Room>> roomsLiveData = networkViewModel.getData().getValue();
        if (roomsLiveData == null || roomsLiveData.getData() == null) return;
        for (Room room : roomsLiveData.getData()) {
            if (room.equalsBarcode(barcode)) {
                roomxItemSharedViewModel.setCurrentRoom(room);
                NavHostFragment.findNavController(this).navigate(R.id.action_roomFragment_to_itemsFragment);
                return;
            }
        }
        ToastUtility.displayCenteredToastMessage(getContext(), getString(R.string.no_room_for_barcode_fragment_rooms), Toast.LENGTH_LONG);
    }

    private void handleFinishInventory() {
        if (MainActivity.getStocktaking(getContext()).isNeverEndingStocktkaking()) {
            finishInventory();
        } else
            new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                    .setTitle(getString(R.string.title_handle_finish_fragment_rooms))
                    .setMessage(getString(R.string.message_handle_finish_fragment_rooms, networkViewModel.getAmountOfValidatedRooms()))
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> finishInventory())
                    .setNegativeButton(android.R.string.no, null)
                    .show();
    }

    private void finishInventory() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(Objects.requireNonNull(getContext()));
        notificationManager.cancel(StocktakingFragment.NOTIFICATION_INV_STARTED_ID);
        ToastUtility.displayCenteredToastMessage(getContext(), getString(R.string.done_fragment_rooms), Toast.LENGTH_LONG);
        MainActivity.clearInventory();
        NavHostFragment.findNavController(this).popBackStack();
    }

    @Override
    protected void handleSuccess(StatusAwareData<List<Room>> statusAwareData) {
        super.handleSuccess(statusAwareData);
        try {
            if (networkViewModel.noRoomsLeft()) {
                roomDropDown.setVisibility(View.GONE);
                finishedText.setVisibility(View.VISIBLE);
            } else {
                GenericDropDownAdapter<Room> adapter =
                        new GenericDropDownAdapter<>(Objects.requireNonNull(getContext()), (ArrayList<Room>) statusAwareData.getData());
                roomDropDown.setAdapter(adapter);
            }
        } catch (Exception e) {
            roomDropDown.setVisibility(View.GONE);
            finishedText.setVisibility(View.VISIBLE);
        }
        //roomDropDown.notify();
    }


    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(getResources().getString(R.string.activity_intent_filter_action));
        Objects.requireNonNull(getContext()).registerReceiver(myBroadcastReceiver, filter);
    }


    @Override
    public void onPause() {
        super.onPause();
        Objects.requireNonNull(getContext()).unregisterReceiver(myBroadcastReceiver);
    }


}
