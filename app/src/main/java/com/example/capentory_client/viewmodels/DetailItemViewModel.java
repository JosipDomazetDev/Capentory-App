package com.example.capentory_client.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.capentory_client.models.MergedItem;
import com.example.capentory_client.models.MergedItemField;
import com.example.capentory_client.repos.DetailItemRepository;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import java.util.Map;

import javax.inject.Inject;

public class DetailItemViewModel extends NetworkViewModel<Map<String, MergedItemField>, DetailItemRepository> {
    private StatusAwareLiveData<MergedItem> searchedForItem;

    @Inject
    public DetailItemViewModel(DetailItemRepository detailItemRepository) {
        super(detailItemRepository);
    }


    @Override
    public void fetchData(String... args) {
        if (statusAwareLiveData != null) {
            return;
        }

        statusAwareLiveData = networkRepository.fetchMainData();
    }


    @Override
    public void reloadData(String... args) {
        statusAwareLiveData = networkRepository.fetchMainData();
    }

    public void fetchSearchedForItem(String barcode) {
        searchedForItem = networkRepository.fetchSearchedForItem(barcode);
    }

    public LiveData<StatusAwareData<MergedItem>> getSearchedForItem() {
        return searchedForItem;
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        statusAwareLiveData = null;
        searchedForItem =null;
    }
}
