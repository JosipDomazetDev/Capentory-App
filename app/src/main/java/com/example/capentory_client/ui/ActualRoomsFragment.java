package com.example.capentory_client.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.VolleyError;
import com.example.capentory_client.R;
import com.example.capentory_client.models.ActualRoom;
import com.example.capentory_client.ui.errorhandling.BasicNetworkErrorHandler;
import com.example.capentory_client.viewmodels.RoomFragmentViewModel;
import com.example.capentory_client.viewmodels.ViewModelProviderFactory;
import com.example.capentory_client.viewmodels.adapter.DropDownRoomAdapter;
import com.example.capentory_client.viewmodels.sharedviewmodels.RoomxItemSharedViewModel;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Objects;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 */
public class ActualRoomsFragment extends DaggerFragment {
    private RoomFragmentViewModel roomFragmentViewModel;
    private Spinner roomDropDown;
    private ProgressBar progressBar;


    public ActualRoomsFragment() {
        // Required empty public constructor
    }

    @Inject
    ViewModelProviderFactory providerFactory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_actualrooms, container, false);
        final SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        final Button btn_fragment_room = view.findViewById(R.id.button_fragment_room);
        roomDropDown = view.findViewById(R.id.room_dropdown_fragment_room);
        progressBar = view.findViewById(R.id.progress_bar_fragment_actualrooms);
        progressBar.bringToFront();
        BasicNetworkErrorHandler basicNetworkErrorHandler = new BasicNetworkErrorHandler(getContext(), view.findViewById(R.id.dropdown_text_fragment_actualroom));


        final RoomxItemSharedViewModel roomxItemSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(RoomxItemSharedViewModel.class);
        btn_fragment_room.setOnClickListener(v -> {
            ActualRoom selectedRoom = (ActualRoom) roomDropDown.getSelectedItem();
            if (selectedRoom == null) return;
            roomxItemSharedViewModel.setCurrentRoom(selectedRoom);
            Navigation.findNavController(view).navigate(R.id.action_roomFragment_to_itemsFragment);
        });


        roomFragmentViewModel = ViewModelProviders.of(this, providerFactory).get(RoomFragmentViewModel.class);
        roomFragmentViewModel.fetchRooms();


        roomFragmentViewModel.getRooms().observe(getViewLifecycleOwner(), statusAwareActualRooms -> {
            switch (statusAwareActualRooms.getStatus()) {
                case SUCCESS:
                    DropDownRoomAdapter adapter = new DropDownRoomAdapter(Objects.requireNonNull(getContext()), (ArrayList<ActualRoom>) statusAwareActualRooms.getData());
                    roomDropDown.setAdapter(adapter);
                    hideProgressBarAndShowContent();
                    break;
                case ERROR:
                    basicNetworkErrorHandler.displayTextViewMessage(statusAwareActualRooms.getError());
                    hideProgressBarAndHideContent();
                    break;
                case FETCHING:
                    displayProgressbarAndHideContent();
                    break;
            }
            if (statusAwareActualRooms.getStatus() != StatusAwareData.State.ERROR)
                basicNetworkErrorHandler.reset();


        });

        swipeRefreshLayout.setOnRefreshListener(
                () -> {
                    roomFragmentViewModel.reloadRooms();
                    swipeRefreshLayout.setRefreshing(false);
                }
        );

        roomxItemSharedViewModel.getCurrentRoomValidated().observe(getViewLifecycleOwner(), b -> {
            if (b) {
                roomFragmentViewModel.removeRoom(roomxItemSharedViewModel.getCurrentRoom().getValue());
            } else
                roomxItemSharedViewModel.setCurrentRoomValidated(false);
        });

        return view;
    }

    private void displayProgressbarAndHideContent() {
        progressBar.setVisibility(View.VISIBLE);
        roomDropDown.setVisibility(View.GONE);
    }


    private void hideProgressBarAndShowContent() {
        progressBar.setVisibility(View.GONE);
        roomDropDown.setVisibility(View.VISIBLE);
    }

    private void hideProgressBarAndHideContent() {
        progressBar.setVisibility(View.GONE);
        roomDropDown.setVisibility(View.GONE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
