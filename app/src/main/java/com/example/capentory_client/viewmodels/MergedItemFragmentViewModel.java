package com.example.capentory_client.viewmodels;

import androidx.lifecycle.LiveData;

import com.example.capentory_client.models.MergedItem;
import com.example.capentory_client.repos.MergedItemsRepository;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

public class MergedItemFragmentViewModel extends StatusFragmentViewModel {


    @Inject
    public MergedItemFragmentViewModel(MergedItemsRepository mergedItemsRepository) {
        super(mergedItemsRepository);
    }


    public LiveData<StatusAwareData<List<MergedItem>>> getMergedItems() {
        return statusAwareLiveData;
    }

    public void removeItem(MergedItem mergedItem) {
        List<MergedItem> currentItems = (List<MergedItem>) ((StatusAwareLiveData<StatusAwareData<List<MergedItem>>>) statusAwareLiveData).getValue().getData();
        if (currentItems == null) return;

        if (currentItems.remove(mergedItem))
            statusAwareLiveData.postSuccess(currentItems);
    }

    @Override
    public void fetchData() {
        if (statusAwareLiveData != null) {
            return;
        }

        statusAwareLiveData = repository.fetchData();
    }

    @Override
    public void reloadData() {
        statusAwareLiveData = repository.fetchData();
    }
}
