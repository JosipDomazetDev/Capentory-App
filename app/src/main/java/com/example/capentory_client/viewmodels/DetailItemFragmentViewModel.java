package com.example.capentory_client.viewmodels;

import com.example.capentory_client.models.MergedItemField;
import com.example.capentory_client.repos.FormRepository;

import java.util.Map;

import javax.inject.Inject;

public class DetailItemFragmentViewModel extends NetworkViewModel<Map<String, MergedItemField>> {

    @Inject
    public DetailItemFragmentViewModel(FormRepository formRepository) {
        super(formRepository);
    }


    @Override
    public void fetchData(String... args) {
        if (statusAwareLiveData != null) {
            return;
        }

        statusAwareLiveData = jsonRepository.fetchData();
    }


    @Override
    public void reloadData(String... args) {
        statusAwareLiveData = jsonRepository.fetchData();
    }
}
