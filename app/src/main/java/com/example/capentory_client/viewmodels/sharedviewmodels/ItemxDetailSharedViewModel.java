package com.example.capentory_client.viewmodels.sharedviewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.capentory_client.models.MergedItem;
import com.example.capentory_client.models.Room;
import com.example.capentory_client.models.ValidationEntry;

public class ItemxDetailSharedViewModel extends ViewModel {
    private final MutableLiveData<MergedItem> currentItem = new MutableLiveData<>();
    private final MutableLiveData<Room> currentRoom = new MutableLiveData<>();
    private final MutableLiveData<ValidationEntry> validationEntryForCurrentItem = new MutableLiveData<>();


    public void setCurrentRoom(Room room) {
        currentRoom.setValue(room);
    }

    public LiveData<Room> getCurrentRoom() {
        return currentRoom;
    }


    public void setCurrentItem(MergedItem item) {
        currentItem.setValue(item);
    }

    public LiveData<MergedItem> getCurrentItem() {
        return currentItem;
    }

    public void setValidationEntryForCurrentItem(ValidationEntry validationEntryForCurrentItem) {
        this.validationEntryForCurrentItem.setValue(validationEntryForCurrentItem);
    }

    public LiveData<ValidationEntry> getValidationEntryForCurrentItem() {
        return validationEntryForCurrentItem;
    }
}
