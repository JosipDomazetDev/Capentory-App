package com.example.capentory_client.ui;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.capentory_client.R;
import com.example.capentory_client.models.ActualRoom;
import com.example.capentory_client.models.MergedItem;
import com.example.capentory_client.viewmodels.ItemFragmentViewModel;
import com.example.capentory_client.viewmodels.RoomFragmentViewModel;
import com.example.capentory_client.viewmodels.ViewModelProviderFactory;
import com.example.capentory_client.viewmodels.adapter.DropDownRoomAdapter;
import com.example.capentory_client.viewmodels.adapter.RecyclerViewAdapter;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MergedItemsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class MergedItemsFragment extends DaggerFragment implements RecyclerViewAdapter.ItemClickListener {
    private static final String ARG_PARAM1 = "room_number";

    private OnFragmentInteractionListener mListener;
    private String currentRoomNumber;

    private ItemFragmentViewModel itemFragmentViewModel;
    private RecyclerView recyclerView;

    @Inject
    ViewModelProviderFactory providerFactory;

    public MergedItemsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentRoomNumber = getArguments().getString(ARG_PARAM1);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mergeditems,
                container, false);


        TextView currentRoomTextView = view.findViewById(R.id.room_number_fragment_actualrooms);
        currentRoomTextView.setText(currentRoomNumber);

/*
        ((Button)view.findViewById(R.id.start)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Navigation.findNavController(view).navigate(R.id.action_inventory_to_flashScanBarcodeAcitivity);
            }
        });*/


        itemFragmentViewModel = ViewModelProviders.of(this, providerFactory).get(ItemFragmentViewModel.class);
        itemFragmentViewModel.init();


        recyclerView = view.findViewById(R.id.recyclerv_view);
        initRecyclerView();

        itemFragmentViewModel.getMergedItems().observe(getViewLifecycleOwner(), new Observer<StatusAwareData<List<MergedItem>>>() {
            @Override
            public void onChanged(StatusAwareData<List<MergedItem>> statusAwareActualRooms) {
                switch (statusAwareActualRooms.getStatus()) {
                    case SUCCESS:
                        recyclerView.notify();
                        break;
                    case ERROR:
                        break;
                }

            }

        });


        return view;

    }


    private void initRecyclerView() {
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(itemFragmentViewModel.getMergedItems().getValue(), this);
        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(int position, View v) {
        Navigation.findNavController(v).navigate(R.id.itemDetailFragment);
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
