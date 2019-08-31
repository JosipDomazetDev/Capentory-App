package com.example.capentory_client.viewmodels.sharedviewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.capentory_client.models.ActualRoom;

public class RoomxItemSharedViewModel extends ViewModel {
    private final MutableLiveData<ActualRoom> currentRoom = new MutableLiveData<>();
    private final MutableLiveData<Boolean> currentRoomValidated = new MutableLiveData<>();


    public void setCurrentRoom(ActualRoom actualRoom) {
        currentRoom.setValue(actualRoom);
    }

    public LiveData<ActualRoom> getCurrentRoom() {
        return currentRoom;
    }


    public void setCurrentRoomValidated(Boolean b) {
        currentRoomValidated.setValue(b);
    }

    public MutableLiveData<Boolean> getCurrentRoomValidated() {
        return currentRoomValidated;
    }
}
