package com.example.capentory_client.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.capentory_client.models.MergedItemField;
import com.example.capentory_client.repos.FormRepository;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class DetailItemFragmentViewModel extends StatusFragmentViewModel<Map<String, MergedItemField>> {

    @Inject
    public DetailItemFragmentViewModel(FormRepository formRepository) {
        super(formRepository);
    }


    @Override
    public void fetchData(String... args) {
        if (statusAwareLiveData != null) {
            return;
        }

        statusAwareLiveData = repository.getData();
    }


    @Override
    public void reloadData(String... args) {
        statusAwareLiveData = repository.getData();
    }
}
