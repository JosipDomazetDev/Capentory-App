package com.example.capentory_client.viewmodels.sharedviewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.capentory_client.models.ActualRoom;

public class RaISharedViewModel extends ViewModel {
    private final MutableLiveData<ActualRoom> currentRoom = new MutableLiveData<>();

    public void setCurrentRoom(ActualRoom actualRoom) {
        currentRoom.setValue(actualRoom);
    }

    public LiveData<ActualRoom> getCurrentRoom() {
        return currentRoom;
    }

}
