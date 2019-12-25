package com.example.capentory_client.ui;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.capentory_client.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class AttachmentsFragment extends Fragment {


    public AttachmentsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_attachments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

      /*  String url = myUrls.get(position);

        Glide
                .with(myFragment)
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.loading_spinner)
                .into(myImageView);

        return myImageView;*/
    }
}
