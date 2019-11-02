package com.example.capentory_client.ui;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.capentory_client.R;
import com.example.capentory_client.androidutility.ToastUtility;
import com.example.capentory_client.models.SerializerEntry;
import com.example.capentory_client.repos.StocktakingRepository;
import com.example.capentory_client.ui.errorhandling.BasicNetworkErrorHandler;
import com.example.capentory_client.viewmodels.StocktakingViewModel;
import com.example.capentory_client.viewmodels.ViewModelProviderFactory;
import com.example.capentory_client.viewmodels.adapter.DropDownSerializerAdapter;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

/**
 * A simple {@link Fragment} subclass.
 */
public class StocktakingFragment extends NetworkFragment<List<SerializerEntry>, StocktakingRepository, StocktakingViewModel> {
    private Spinner serializerDropDown;


    public StocktakingFragment() {
        // Required empty public constructor
    }


    @Inject
    ViewModelProviderFactory providerFactory;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stocktaking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Button btnStocktaking = view.findViewById(R.id.button_fragment_stocktaking);
        serializerDropDown = view.findViewById(R.id.db_dropdown_fragment_stocktaking);

        Log.e("XXXX", "XXXXX");
        initWithFetch(ViewModelProviders.of(this, providerFactory).get(StocktakingViewModel.class),
                new BasicNetworkErrorHandler(getContext(), view.findViewById(R.id.dropdown_text_fragment_stocktaking)),
                view,
                R.id.progress_bar_fragment_stocktaking,
                view.findViewById(R.id.content_stocktaking_fragment),
                R.id.swipe_refresh_fragment_stocktaking
        );

        btnStocktaking.setOnClickListener(v -> {
            SerializerEntry selectedSerializer = (SerializerEntry) serializerDropDown.getSelectedItem();
            if (selectedSerializer == null) return;

            EditText name = view.findViewById(R.id.name_edittext_stocktacking_fragment_stocktaking);
            EditText comment = view.findViewById(R.id.comment_edittext_stocktacking_fragment_stocktaking);

            if (TextUtils.isEmpty(name.getText())) {
                ToastUtility.displayCenteredToastMessage(getContext(), "Ihre Inventur muss einen Namen haben!", Toast.LENGTH_SHORT);
                return;
            }

            networkViewModel.postStocktaking(name.getText().toString(), comment.getText().toString());
            observeSpecificLiveData(networkViewModel.getPostedStocktaking(), liveData -> {
                if (liveData == null || liveData.getData() == null) return;

                MainActivity.setSerializer(selectedSerializer);
                MainActivity.setStocktaking(liveData.getData());
                Navigation.findNavController(view).navigate(R.id.action_stocktakingFragment_to_roomFragment);
            });

        });
    }

    @Override
    protected void handleSuccess(StatusAwareData<List<SerializerEntry>> statusAwareData) {
        super.handleSuccess(statusAwareData);
        DropDownSerializerAdapter adapter = new DropDownSerializerAdapter(Objects.requireNonNull(getContext()), (ArrayList<SerializerEntry>) statusAwareData.getData());
        serializerDropDown.setAdapter(adapter);
        //serializerDropDown.notify();
    }


}
