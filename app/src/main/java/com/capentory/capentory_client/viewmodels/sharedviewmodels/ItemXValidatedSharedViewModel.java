package com.capentory.capentory_client.viewmodels.sharedviewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.capentory.capentory_client.models.MergedItem;
import com.capentory.capentory_client.models.RecyclerViewItem;

import java.util.ArrayList;
import java.util.List;

public class ItemXValidatedSharedViewModel extends ViewModel {
    private final MutableLiveData<List<RecyclerViewItem>> alreadyValidatedItems = new MutableLiveData<>();
    private final MutableLiveData<Boolean> itemsShouldBeRevised = new MutableLiveData<>();
    private final List<MergedItem> itemsToRevise = new ArrayList<>();
    private final MutableLiveData<String> reviseMessage = new MutableLiveData<>();


    public void setAlreadyValidatedItems(List<RecyclerViewItem> items) {
        alreadyValidatedItems.setValue(items);
    }

    public LiveData<List<RecyclerViewItem>> getAlreadyValidatedItems() {
        return alreadyValidatedItems;
    }


    public List<MergedItem> getItemsToRevise() {
        return itemsToRevise;
    }


    public void addItemsToRevise(MergedItem mergedItem) {
        List<RecyclerViewItem> alreadyValidatedItemsValue = alreadyValidatedItems.getValue();
        if (alreadyValidatedItemsValue == null) return;
        alreadyValidatedItemsValue.remove(mergedItem);
        alreadyValidatedItems.postValue(alreadyValidatedItemsValue);

        itemsToRevise.add(mergedItem);
        itemsShouldBeRevised.postValue(true);
        reviseMessage.postValue(String.valueOf(itemsToRevise.size()));
    }

    public LiveData<Boolean> getItemsShouldBeRevised() {
        return itemsShouldBeRevised;
    }

    public void clearItemsToRevise() {
        itemsToRevise.clear();
        reviseMessage.postValue(String.valueOf(itemsToRevise.size()));
    }
}
