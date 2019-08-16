package com.example.capentory_client;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RoomFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class RoomFragment extends Fragment {
    private RoomFragmentViewModel roomFragmentViewModel;

    private OnFragmentInteractionListener mListener;

    public RoomFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_room, container, false);

        view.findViewById(R.id.button_fragment_room).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.inventory, null));

        roomFragmentViewModel = ViewModelProviders.of(this).get(RoomFragmentViewModel.class);

        roomFragmentViewModel.init();

        roomFragmentViewModel.getRooms().observe(this, new Observer<List<JSONObject>>() {
            @Override
            public void onChanged(@Nullable List<JSONObject> rooms) {
                ArrayList<String> options = new ArrayList<String>();

                options.add("option 1");
                options.add("option 2");
                options.add("option 3");

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(Objects.requireNonNull(getContext()), R.layout.support_simple_spinner_dropdown_item, options);
                ((Spinner) view.findViewById(R.id.room_dropdown_fragment_room)).setAdapter(adapter);
            }
        });

        return view;
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
