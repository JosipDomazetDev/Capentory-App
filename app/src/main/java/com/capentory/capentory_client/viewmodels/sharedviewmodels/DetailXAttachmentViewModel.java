package com.capentory.capentory_client.viewmodels.sharedviewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.capentory.capentory_client.models.MergedItem;

public class DetailXAttachmentViewModel extends ViewModel {
    private final MutableLiveData<MergedItem> currentItem = new MutableLiveData<>();
    private final MutableLiveData<Boolean> exitedAttachmentScreen = new MutableLiveData<>(false);


    public void setCurrentItem(MergedItem item) {
        currentItem.setValue(item);
    }

    public MergedItem getCurrentItem() {
        return currentItem.getValue();
    }


    public void setExitedAttachmentScreen(Boolean b) {
        exitedAttachmentScreen.setValue(b);
    }

    public  MutableLiveData<Boolean> getExitedAttachmentScreen() {
        return exitedAttachmentScreen;
    }
}