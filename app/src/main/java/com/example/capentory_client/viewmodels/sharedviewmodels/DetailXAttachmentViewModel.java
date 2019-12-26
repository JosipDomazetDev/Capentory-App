package com.example.capentory_client.viewmodels.sharedviewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.capentory_client.models.MergedItem;

public class DetailXAttachmentViewModel  extends ViewModel {
    private final MutableLiveData<MergedItem> currentItem = new MutableLiveData<>();



    public void setCurrentItem(MergedItem item) {
        currentItem.setValue(item);
    }

    public MergedItem getCurrentItem() {
        return currentItem.getValue();
    }
}