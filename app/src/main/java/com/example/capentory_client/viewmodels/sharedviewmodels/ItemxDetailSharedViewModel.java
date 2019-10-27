package com.example.capentory_client.viewmodels.sharedviewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.capentory_client.models.MergedItem;
import com.example.capentory_client.models.ValidationEntry;

public class ItemxDetailSharedViewModel extends ViewModel {
    private final MutableLiveData<MergedItem> currentItem = new MutableLiveData<>();
    private final MutableLiveData<ValidationEntry> validationEntryForCurrentItem =new MutableLiveData<>();

    public void setCurrentItem(MergedItem item) {
        currentItem.setValue(item);
    }

    public MutableLiveData<MergedItem> getCurrentItem() {
        return currentItem;
    }

    public void setValidationEntryForCurrentItem(ValidationEntry validationEntryForCurrentItem) {
        this.validationEntryForCurrentItem.setValue(validationEntryForCurrentItem);
    }

    public MutableLiveData<ValidationEntry> getValidationEntryForCurrentItem() {
        return validationEntryForCurrentItem;
    }
}
