package com.example.capentory_client.ui;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.capentory_client.R;
import com.example.capentory_client.viewmodels.adapter.RecyclerViewAdapter;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Inventory.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class Inventory extends Fragment {
    private ArrayList<String> anlage = new ArrayList<>();
    private ArrayList<String> anlage_bez = new ArrayList<>();

    private OnFragmentInteractionListener mListener;

    public Inventory() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_inventory,
                container, false);




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
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(anlage, anlage_bez, getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;

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
