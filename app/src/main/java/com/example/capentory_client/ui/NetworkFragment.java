package com.example.capentory_client.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.capentory_client.R;
import com.example.capentory_client.models.ActualRoom;
import com.example.capentory_client.viewmodels.ViewModelProviderFactory;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import org.json.JSONException;

import java.util.List;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

import static com.example.capentory_client.viewmodels.wrappers.StatusAwareData.State.ERROR;

public class NetworkFragment extends DaggerFragment {

    @Inject
    ViewModelProviderFactory providerFactory;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item_detail, container, false);

        /*detailItemFragmentViewModel.getFields().observe(getViewLifecycleOwner(), fields -> {

            switch (fields.getStatus()) {

                case SUCCESS:
                    try {
                        displayForm(view, fields.getData());
                    } catch (JSONException e) {
                        basicNetworkErrorHandler.displayTextViewMessage(e);
                    }
                    hideProgressBarAndShowContent();
                    break;
                case ERROR:
                    fields.getError().printStackTrace();
                    basicNetworkErrorHandler.displayTextViewMessage(fields.getError());
                    hideProgressBarAndHideContent();
                    break;
                case FETCHING:
                    displayProgressbarAndHideContent();
                    break;

            }
            if (fields.getStatus() != ERROR)
                basicNetworkErrorHandler.reset();
        });*/

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

}
