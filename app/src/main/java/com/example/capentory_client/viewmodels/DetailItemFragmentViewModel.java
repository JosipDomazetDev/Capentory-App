package com.example.capentory_client.viewmodels;

import androidx.lifecycle.ViewModel;

import com.example.capentory_client.models.MergedItemField;
import com.example.capentory_client.repos.FormRepository;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import java.util.List;

import javax.inject.Inject;

public class DetailItemFragmentViewModel extends ViewModel {
    private StatusAwareLiveData<List<MergedItemField>> fields;
    private FormRepository formRepository;

    @Inject
    public DetailItemFragmentViewModel(FormRepository formRepository) {
        this.formRepository = formRepository;
    }

    public void fetchForm() {
        if (fields != null) {
            return;
        }

        fields = formRepository.getForm();
    }

    public void reloadForm(String currentRoomString) {
        fields = formRepository.getForm();
    }

    public StatusAwareLiveData<List<MergedItemField>> getFields() {
        return fields;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

}
