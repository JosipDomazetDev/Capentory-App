package com.example.capentory_client.viewmodels;

import com.example.capentory_client.models.MergedItem;
import com.example.capentory_client.repos.MergedItemsRepository;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

public class MergedItemFragmentViewModel extends NetworkViewModel<List<MergedItem>, MergedItemsRepository> {


    @Inject
    public MergedItemFragmentViewModel(MergedItemsRepository mergedItemsRepository) {
        super(mergedItemsRepository);
    }


    public void removeItem(MergedItem mergedItem) {
        List<MergedItem> currentItems = Objects.requireNonNull(statusAwareLiveData.getValue()).getData();
        if (currentItems == null) return;

        if (currentItems.remove(mergedItem))
            statusAwareLiveData.postSuccess(currentItems);
    }

    @Override
    public void fetchData(String... args) {
        if (statusAwareLiveData != null) {
            return;
        }

        statusAwareLiveData = jsonRepository.fetchMainData(args);
    }


    @Override
    public void reloadData(String... args) {
        statusAwareLiveData = jsonRepository.fetchMainData(args);
    }
}
