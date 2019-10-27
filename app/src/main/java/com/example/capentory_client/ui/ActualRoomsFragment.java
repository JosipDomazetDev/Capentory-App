package com.example.capentory_client.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.capentory_client.R;
import com.example.capentory_client.models.ActualRoom;
import com.example.capentory_client.repos.ActualRoomsRepository;
import com.example.capentory_client.ui.errorhandling.BasicNetworkErrorHandler;
import com.example.capentory_client.viewmodels.RoomFragmentViewModel;
import com.example.capentory_client.viewmodels.ViewModelProviderFactory;
import com.example.capentory_client.viewmodels.adapter.DropDownRoomAdapter;
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
public class ActualRoomsFragment extends NetworkFragment<List<ActualRoom>, ActualRoomsRepository, RoomFragmentViewModel> {
    private Spinner roomDropDown;


    public ActualRoomsFragment() {
        // Required empty public constructor
    }

    @Inject
    ViewModelProviderFactory providerFactory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_actualrooms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Button btn_fragment_room = view.findViewById(R.id.button_fragment_room);
        roomDropDown = view.findViewById(R.id.room_dropdown_fragment_room);

        initWithFetch(ViewModelProviders.of(this, providerFactory).get(RoomFragmentViewModel.class),
                new BasicNetworkErrorHandler(getContext(), view.findViewById(R.id.dropdown_text_fragment_actualroom)),
                view,
                R.id.progress_bar_fragment_actualrooms,
                roomDropDown,
                R.id.swipe_refresh
        );


        final RoomxItemSharedViewModel roomxItemSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(RoomxItemSharedViewModel.class);
        btn_fragment_room.setOnClickListener(v -> {
            ActualRoom selectedRoom = (ActualRoom) roomDropDown.getSelectedItem();
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
                new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                        .setTitle("Inventurvorgang")
                        .setMessage("Inventurvorgang läuft, wollen Sie ihn wirklich beenden?")
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> handleFinishInventory())
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });

    }

    private void handleFinishInventory() {
        NavHostFragment.findNavController(this).popBackStack();
    }

    @Override
    protected void handleSuccess(StatusAwareData<List<ActualRoom>> statusAwareData) {
        super.handleSuccess(statusAwareData);
        DropDownRoomAdapter adapter = new DropDownRoomAdapter(Objects.requireNonNull(getContext()), (ArrayList<ActualRoom>) statusAwareData.getData());
        roomDropDown.setAdapter(adapter);
        //roomDropDown.notify();
    }

}
