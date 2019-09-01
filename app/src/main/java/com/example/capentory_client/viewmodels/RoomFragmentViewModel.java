package com.example.capentory_client.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.capentory_client.models.ActualRoom;
import com.example.capentory_client.repos.ActualRoomsRepository;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

public class RoomFragmentViewModel extends ViewModel {
    private StatusAwareLiveData<List<ActualRoom>> rooms;
    private ActualRoomsRepository ralphRepository;


    @Inject
    public RoomFragmentViewModel(ActualRoomsRepository ralphRepository) {
        this.ralphRepository = ralphRepository;
    }

    public void fetchRooms() {
        if (rooms != null) {
            return;
        }
        rooms = ralphRepository.getRooms();
    }

    public void reloadRooms() {
        rooms = ralphRepository.getRooms();
    }

    public LiveData<StatusAwareData<List<ActualRoom>>> getRooms() {
        return rooms;
    }

    public void removeRoom(ActualRoom actualRoom) {
        List<ActualRoom> currentRooms = Objects.requireNonNull(rooms.getValue()).getData();
        if (currentRooms == null) return;

        if (currentRooms.remove(actualRoom))
            rooms.postSuccess(currentRooms);
    }


    @Override
    protected void onCleared() {
        super.onCleared();

    }

}
