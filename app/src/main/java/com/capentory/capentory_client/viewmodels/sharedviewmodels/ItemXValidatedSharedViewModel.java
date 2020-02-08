package com.capentory.capentory_client.viewmodels.sharedviewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.capentory.capentory_client.models.RecyclerViewItem;

import java.util.List;

public class ItemXValidatedSharedViewModel  extends ViewModel {
    private final MutableLiveData<List<RecyclerViewItem>> alreadyValidatedItems = new MutableLiveData<>();


    public void setAlreadyValidatedItems(List<RecyclerViewItem> items) {
        alreadyValidatedItems.setValue(items);
    }

    public LiveData<List<RecyclerViewItem>> getAlreadyValidatedItems() {
        return alreadyValidatedItems;
    }


}
