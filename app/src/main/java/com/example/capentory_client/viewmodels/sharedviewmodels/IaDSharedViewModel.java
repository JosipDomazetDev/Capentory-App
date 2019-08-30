package com.example.capentory_client.viewmodels.sharedviewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.capentory_client.models.MergedItem;

public class IaDSharedViewModel extends ViewModel {
    private final MutableLiveData<MergedItem> currentItem = new MutableLiveData<>();

    public void setCurrentItem(MergedItem item) {
        currentItem.setValue(item);
    }

    public MutableLiveData<MergedItem> getCurrentItem() {
        return currentItem;
    }

}
