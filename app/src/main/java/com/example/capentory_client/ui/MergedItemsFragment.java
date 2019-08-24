package com.example.capentory_client.ui;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.capentory_client.R;
import com.example.capentory_client.viewmodels.adapter.RecyclerViewAdapter;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MergedItemsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class MergedItemsFragment extends Fragment implements RecyclerViewAdapter.ItemClickListener {
    private static final String ARG_PARAM1 = "room_number";
    private ArrayList<String> anlage = new ArrayList<>();
    private ArrayList<String> anlage_bez = new ArrayList<>();

    private OnFragmentInteractionListener mListener;
    private String currentRoomNumber;

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


        anlage.add("80003");
        anlage_bez.add("AK-47");

        anlage.add("400000410024");
        anlage_bez.add("HP ProDesk 400 MT, Win 8.1, I5-4570, 8GB, 1TB HDD");

        anlage.add("400000410025");
        anlage_bez.add("HP ProDesk 400 MT, Win 8.1, I5-4570, 8GB, 1TB HDD");

        anlage.add("400000410027");
        anlage_bez.add("HP ProDesk 400 MT, Win 8.1, I5-4570, 8GB, 1TB HDD");

        RecyclerView recyclerView = view.findViewById(R.id.recyclerv_view);


        RecyclerViewAdapter adapter = new RecyclerViewAdapter(anlage, anlage_bez, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;

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
