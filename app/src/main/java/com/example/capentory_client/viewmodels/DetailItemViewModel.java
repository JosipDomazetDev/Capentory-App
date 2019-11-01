package com.example.capentory_client.viewmodels;

import com.example.capentory_client.models.MergedItemField;
import com.example.capentory_client.repos.DetailItemRepository;

import java.util.Map;

import javax.inject.Inject;

public class DetailItemViewModel extends NetworkViewModel<Map<String, MergedItemField>, DetailItemRepository> {

    @Inject
    public DetailItemViewModel(DetailItemRepository detailItemRepository) {
        super(detailItemRepository);
    }


    @Override
    public void fetchData(String... args) {
        if (statusAwareLiveData != null) {
            return;
        }

        statusAwareLiveData = jsonRepository.fetchMainData();
    }


    @Override
    public void reloadData(String... args) {
        statusAwareLiveData = jsonRepository.fetchMainData();
    }
}
