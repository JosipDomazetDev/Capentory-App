package com.example.capentory_client.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.capentory_client.models.MergedItem;
import com.example.capentory_client.repos.MergedItemsRepository;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

public class MergedItemFragmentViewModel extends ViewModel {
    private StatusAwareLiveData<List<MergedItem>> mergedItems;
    private MergedItemsRepository mergedItemsRepository;


    @Inject
    public MergedItemFragmentViewModel(MergedItemsRepository mergedItemsRepository) {
        this.mergedItemsRepository = mergedItemsRepository;
    }

    public void fetchItems(String currentRoomString) {
        if (mergedItems != null) {
            return;
        }

        mergedItems = mergedItemsRepository.getMergedItems(currentRoomString);
    }

    public void reloadItems(String currentRoomString) {
        mergedItems = mergedItemsRepository.getMergedItems(currentRoomString);
    }

    public LiveData<StatusAwareData<List<MergedItem>>> getMergedItems() {
        return mergedItems;
    }

    public void removeItem(MergedItem mergedItem) {
        List<MergedItem> currentItems = Objects.requireNonNull(mergedItems.getValue()).getData();
        if (currentItems == null) return;

        if (currentItems.remove(mergedItem))
            mergedItems.postSuccess(currentItems);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

}
