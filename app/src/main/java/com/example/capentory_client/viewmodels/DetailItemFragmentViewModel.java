package com.example.capentory_client.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.capentory_client.models.MergedItemField;
import com.example.capentory_client.repos.FormRepository;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import java.util.Map;

import javax.inject.Inject;

public class DetailItemFragmentViewModel extends ViewModel {
    private StatusAwareLiveData<Map<String, MergedItemField>> fields;
    private FormRepository formRepository;

    @Inject
    public DetailItemFragmentViewModel(FormRepository formRepository) {
        this.formRepository = formRepository;
    }

    public void fetchForm() {
        if (fields != null) {
            return;
        }

        fields = formRepository.fetchData();
    }

    public void reloadForm() {
        fields = formRepository.resetForm();
    }

    public LiveData<StatusAwareData<Map<String, MergedItemField>>> getFields() {
        return fields;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

}
