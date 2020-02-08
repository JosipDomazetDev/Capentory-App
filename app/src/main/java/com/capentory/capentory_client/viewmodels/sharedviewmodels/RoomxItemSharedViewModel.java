package com.capentory.capentory_client.viewmodels.sharedviewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.capentory.capentory_client.models.Room;

public class RoomxItemSharedViewModel extends ViewModel {
    private final MutableLiveData<Room> currentRoom = new MutableLiveData<>();
    private final MutableLiveData<Boolean> currentRoomValidated = new MutableLiveData<>();


    public void setCurrentRoom(Room room) {
        currentRoom.setValue(room);
    }

    public LiveData<Room> getCurrentRoom() {
        return currentRoom;
    }


    public void setCurrentRoomValidated(Boolean b) {
        currentRoomValidated.setValue(b);
    }

    public LiveData<Boolean> getCurrentRoomValidated() {
        return currentRoomValidated;
    }
}
