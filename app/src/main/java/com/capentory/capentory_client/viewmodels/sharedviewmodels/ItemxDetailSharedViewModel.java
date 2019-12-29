package com.capentory.capentory_client.viewmodels.sharedviewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.capentory.capentory_client.models.MergedItem;
import com.capentory.capentory_client.models.Room;
import com.capentory.capentory_client.models.ValidationEntry;

import java.util.List;

public class ItemxDetailSharedViewModel extends ViewModel {
    private final MutableLiveData<MergedItem> currentItem = new MutableLiveData<>();
    private final MutableLiveData<List<Room>> currentRooms = new MutableLiveData<>();
    private final MutableLiveData<ValidationEntry> validationEntryForCurrentItem = new MutableLiveData<>();


    public void setCurrentRooms(List<Room> rooms) {
        currentRooms.setValue(rooms);
    }

    public LiveData<List<Room>> getCurrentRooms() {
        return currentRooms;
    }


    public void setCurrentItem(MergedItem item) {
        currentItem.setValue(item);
    }

    public MergedItem getCurrentItem() {
        return currentItem.getValue();
    }

    public void setValidationEntryForCurrentItem(ValidationEntry validationEntryForCurrentItem) {
        this.validationEntryForCurrentItem.setValue(validationEntryForCurrentItem);
    }

    public LiveData<ValidationEntry> getValidationEntryForCurrentItem() {
        return validationEntryForCurrentItem;
    }


    public boolean areSubRoomsInvolved() {
        // If the size is more than 1 it means that we have to account for subrooms in ItemDetailFragment
        List<Room> rooms = currentRooms.getValue();
        if (rooms == null) return false;
        return rooms.size() > 1;
    }
}
