package com.example.capentory_client.ui;

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
import com.example.capentory_client.viewmodels.RoomViewModel;
import com.example.capentory_client.viewmodels.ViewModelProviderFactory;
import com.example.capentory_client.viewmodels.adapter.GenericDropDownAdapter;
import com.example.capentory_client.viewmodels.sharedviewmodels.RoomxItemSharedViewModel;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

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

        final Button chooseRoomButton = view.findViewById(R.id.choose_room_button_fragment_room);
        final Button endInventoryButton = view.findViewById(R.id.end_inventory_button_fragment_room);

        roomDropDown = view.findViewById(R.id.room_dropdown_fragment_room);
        ((TextView) view.findViewById(R.id.started_stocktaking_text_fragment_actualroom)).setText(
                String.format(getString(R.string.started_inventory_fragment_rooms), MainActivity.getStocktaking().getName()));

        initWithFetch(ViewModelProviders.of(this, providerFactory).get(RoomViewModel.class),
                new BasicNetworkErrorHandler(getContext(), view.findViewById(R.id.dropdown_text_fragment_actualroom)),
                view,
                R.id.progress_bar_fragment_actualrooms,
                roomDropDown,
                R.id.swipe_refresh
        );


        final RoomxItemSharedViewModel roomxItemSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(RoomxItemSharedViewModel.class);
        chooseRoomButton.setOnClickListener(v -> {
            Room selectedRoom = (Room) roomDropDown.getSelectedItem();
            if (selectedRoom == null) return;
            roomxItemSharedViewModel.setCurrentRoom(selectedRoom);
            Navigation.findNavController(view).navigate(R.id.action_roomFragment_to_itemsFragment);
        });


        roomxItemSharedViewModel.getCurrentRoomValidated().observe(getViewLifecycleOwner(), b -> {
            if (b) {
                networkViewModel.removeRoom(roomxItemSharedViewModel.getCurrentRoom().getValue());
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

    }

    private void handleFinishInventory() {
        new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                .setTitle("Inventurvorgang")
                .setMessage("Wollen Sie die aktuelle Inventur abschließen? Anzahl der validierten Räume: " + networkViewModel.getAmountOfValidatedRooms())
                .setPositiveButton(android.R.string.yes, (dialog, which) -> finishInventory())
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void finishInventory() {
        networkViewModel.finishInventory();
        observeSpecificLiveData(networkViewModel.getFinishSuccessful(), liveData -> {
            if (liveData == null || liveData.getData() == null) return;

            if (liveData.getData()) {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(Objects.requireNonNull(getContext()));
                notificationManager.cancel(StocktakingFragment.NOTIFICATION_INV_STARTED_ID);
                ToastUtility.displayCenteredToastMessage(getContext(), "Gratulation! Die Inventur \"" + MainActivity.getStocktaking().getName() + "\" ist abgeschlossen!", Toast.LENGTH_LONG);
                MainActivity.clearInventory();
                NavHostFragment.findNavController(this).popBackStack();
            } else {
                basicNetworkErrorHandler.displayTextViewMessage("Server konnte die Anfrage nicht verarbeiten. Wenn es nach erneutem Versuch nicht funktioniert wenden Sie sich an Capentory!");
            }

        });

    }

    @Override
    protected void handleSuccess(StatusAwareData<List<Room>> statusAwareData) {
        super.handleSuccess(statusAwareData);
        GenericDropDownAdapter<Room> adapter =
                new GenericDropDownAdapter<>(Objects.requireNonNull(getContext()), (ArrayList<Room>) statusAwareData.getData());
        roomDropDown.setAdapter(adapter);
        //roomDropDown.notify();
    }

}
