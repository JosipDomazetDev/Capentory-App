package com.example.capentory_client.viewmodels;

import androidx.lifecycle.ViewModel;

import com.example.capentory_client.models.ActualRoom;
import com.example.capentory_client.models.MergedItem;
import com.example.capentory_client.repos.MergedItemsRepository;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

public class ItemFragmentViewModel extends ViewModel {
    private StatusAwareLiveData<List<MergedItem>> mergedItems;
    private MergedItemsRepository mergedItemsRepository;


    public ItemFragmentViewModel() {
    }

    @Inject
    public ItemFragmentViewModel(MergedItemsRepository mergedItemsRepository) {
        this.mergedItemsRepository = mergedItemsRepository;
    }



    public void init() {
        if (mergedItems != null) {
            return;
        }
        mergedItems = mergedItemsRepository.getMergedItems();
    }

    public void reloadRooms() {
        mergedItems = mergedItemsRepository.getMergedItems();
    }

    public StatusAwareLiveData<List<MergedItem>> getMergedItems() {
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
