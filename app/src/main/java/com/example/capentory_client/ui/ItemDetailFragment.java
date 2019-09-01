package com.example.capentory_client.ui;

import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.capentory_client.R;
import com.example.capentory_client.viewmodels.sharedviewmodels.ItemxDetailSharedViewModel;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class ItemDetailFragment extends Fragment {
    private ItemxDetailSharedViewModel itemxDetailSharedViewModel;

    public ItemDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        itemxDetailSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(ItemxDetailSharedViewModel.class);

        itemxDetailSharedViewModel.getCurrentItem().observe(getViewLifecycleOwner(), mergedItem -> {
            TextView txt = view.findViewById(R.id.dummy);
            if (mergedItem == null) {
                txt.setText("Server gib Formular");
            } else {
                txt.setText(mergedItem.getMergedItemJSONPayload().toString());
            }
        });

        ImageButton validateButton = view.findViewById(R.id.validate_btn_fragment_itemdetail);
        validateButton.setOnClickListener(v -> handleValidate());
    }

    public void handleValidate() {
        itemxDetailSharedViewModel.setCurrentItemValidated(true);
        NavHostFragment.findNavController(this).popBackStack();
    }
}
