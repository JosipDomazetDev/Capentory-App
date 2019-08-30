package com.example.capentory_client.ui;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.capentory_client.R;
import com.example.capentory_client.models.MergedItem;
import com.example.capentory_client.viewmodels.sharedviewmodels.IaDSharedViewModel;

import java.util.Objects;
import java.util.Observer;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class ItemDetailFragment extends Fragment {


    public ItemDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_item_detail, container, false);
        IaDSharedViewModel iaDSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(IaDSharedViewModel.class);

        iaDSharedViewModel.getCurrentItem().observe(getViewLifecycleOwner(), mergedItem -> {
            TextView txt = view.findViewById(R.id.dummy);
            txt.setText(mergedItem.getMergedItemJSONPayload().toString());
        });
        return view;
    }


}
