package com.example.capentory_client.ui;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.VolleyError;
import com.example.capentory_client.R;
import com.example.capentory_client.models.ActualRoom;
import com.example.capentory_client.viewmodels.RoomFragmentViewModel;
import com.example.capentory_client.viewmodels.ViewModelProviderFactory;
import com.example.capentory_client.viewmodels.adapter.DropDownRoomAdapter;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ActualRoomsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ActualRoomsFragment extends DaggerFragment {
    private RoomFragmentViewModel roomFragmentViewModel;
    private Spinner roomDropDown;

    private OnFragmentInteractionListener mListener;


    public ActualRoomsFragment() {
        // Required empty public constructor
    }

    @Inject
    ViewModelProviderFactory providerFactory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_actualrooms, container, false);

        Button btn_fragment_room = view.findViewById(R.id.button_fragment_room);
        roomDropDown = view.findViewById(R.id.room_dropdown_fragment_room);


        btn_fragment_room.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Object selectedItem = roomDropDown.getSelectedItem();
                if (!(selectedItem instanceof ActualRoom)) return;

                ActualRoom selectedRoom = (ActualRoom) selectedItem;
                Bundle bundle = new Bundle();
                bundle.putString("room_number", selectedRoom.getRoomNumber());

                Navigation.findNavController(view).navigate(R.id.itemsFragment, bundle);
                roomFragmentViewModel.removeRoom(selectedRoom);
            }

        });


        roomFragmentViewModel = ViewModelProviders.of(this, providerFactory).get(RoomFragmentViewModel.class);
        roomFragmentViewModel.init();
        roomDropDown.setAdapter(new ArrayAdapter<>(Objects.requireNonNull(getContext()), R.layout.support_simple_spinner_dropdown_item, Collections.singletonList("Loading...")));

        roomFragmentViewModel.getRooms().observe(getViewLifecycleOwner(), new Observer<StatusAwareData<List<ActualRoom>>>() {
            @Override
            public void onChanged(StatusAwareData<List<ActualRoom>> statusAwareActualRooms) {
                switch (statusAwareActualRooms.getStatus()) {
                    case SUCCESS:
                        DropDownRoomAdapter adapter = new DropDownRoomAdapter(Objects.requireNonNull(getContext()), (ArrayList<ActualRoom>) statusAwareActualRooms.getData());
                        roomDropDown.setAdapter(adapter);
                        break;
                    case ERROR:
                        displayErrorToastMessage(statusAwareActualRooms.getError());
                        break;
                }

            }

        });

        final SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        roomFragmentViewModel.reloadRooms();
                        Toast.makeText(getContext(), "Neuer Fetchversuch...", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        );


        return view;
    }

    private void displayErrorToastMessage(Throwable error) {
        if (error == null) return;

        String errorMsg = "";
        if (error instanceof JSONException) {
            errorMsg = "Server verwendet ein nicht unterstütztes JSON-Format!";
        } else if (error instanceof VolleyError) {
            errorMsg = "Ein Verbindungsfehler ist aufgetreten!";
        }

        String exceptionMsg = "";
        String fullExceptionMsg = error.getMessage();
        if (fullExceptionMsg != null)
            exceptionMsg = "\n" + fullExceptionMsg.substring(0, Math.min(fullExceptionMsg.length(), 100)) + "....";

        Toast.makeText(getContext(), errorMsg + exceptionMsg, Toast.LENGTH_SHORT).show();
        Log.e("ERROR_LOG", "" + error.getLocalizedMessage());

    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


}
