package com.capentory.capentory_client.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import androidx.navigation.fragment.NavHostFragment;

import com.capentory.capentory_client.R;
import com.capentory.capentory_client.androidutility.SearchBarHelperUtility;
import com.capentory.capentory_client.androidutility.ToastUtility;
import com.capentory.capentory_client.androidutility.UserUtility;
import com.capentory.capentory_client.models.Room;
import com.capentory.capentory_client.repos.RoomsRepository;
import com.capentory.capentory_client.ui.errorhandling.ErrorHandler;
import com.capentory.capentory_client.ui.scanactivities.ScanBarcodeActivity;
import com.capentory.capentory_client.ui.zebra.ZebraBroadcastReceiver;
import com.capentory.capentory_client.viewmodels.RoomViewModel;
import com.capentory.capentory_client.viewmodels.ViewModelProviderFactory;
import com.capentory.capentory_client.viewmodels.adapter.GenericDropDownAdapter;
import com.capentory.capentory_client.viewmodels.sharedviewmodels.RoomxItemSharedViewModel;
import com.capentory.capentory_client.viewmodels.wrappers.StatusAwareData;
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
    private ZebraBroadcastReceiver zebraBroadcastReceiver = new ZebraBroadcastReceiver(errorHandler, this::navigateByBarcode);
    private GenericDropDownAdapter<Room> adapter;

    public RoomsFragment() {
        // Required empty public constructor
    }

    @Inject
    ViewModelProviderFactory providerFactory;

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        SearchBarHelperUtility.bindSearchBar(menu, inflater, getActivity(), new SearchBarHelperUtility.SearchHandler() {
            @Override
            public void onQueryTextSubmit(String query) {
                UserUtility.hideKeyboard(getActivity());
                handleRoomSelected();
            }

            @Override
            public void onQueryTextChange(String newText) {
                if (adapter != null)
                    adapter.getFilter().filter(newText);
            }
        });
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // https://stackoverflow.com/questions/12090335/menu-in-fragments-not-showing
        setHasOptionsMenu(true);
    }


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
                    new ErrorHandler(getContext(), view.findViewById(R.id.dropdown_text_fragment_actualroom)),
                    view,
                    R.id.progress_bar_fragment_actualrooms,
                    view.findViewById(R.id.cardview_rooms_dropdown_fragment_rooms),
                    R.id.swipe_refresh
            );

            adapter = new GenericDropDownAdapter<>(Objects.requireNonNull(getContext()));
            roomDropDown.setAdapter(adapter);

            roomxItemSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(RoomxItemSharedViewModel.class);
            chooseRoomButton.setOnClickListener(v -> handleRoomSelected());

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

    private void handleRoomSelected() {
        Room selectedRoom = (Room) roomDropDown.getSelectedItem();
        if (selectedRoom == null) return;
        roomxItemSharedViewModel.setCurrentRoom(selectedRoom);
        NavHostFragment.findNavController(this).navigate(R.id.action_roomFragment_to_itemsFragment);
    }


    private void navigateByBarcode(String barcode) {
        StatusAwareData<List<Room>> roomsLiveData = networkViewModel.getLiveData().getValue();
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
        if (MainActivity.getStocktaking(getContext()).isNeverEndingStocktaking()) {
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
                adapter.fill((ArrayList<Room>) statusAwareData.getData());
            }
        } catch (Exception e) {
            roomDropDown.setVisibility(View.GONE);
            finishedText.setVisibility(View.VISIBLE);
        }
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
