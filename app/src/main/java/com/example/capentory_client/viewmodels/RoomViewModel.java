package com.example.capentory_client.viewmodels;

import com.example.capentory_client.models.Room;
import com.example.capentory_client.repos.RoomsRepository;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

public class RoomViewModel extends NetworkViewModel<List<Room>, RoomsRepository> {
    private boolean startedRemoving = false;

    @Inject
    public RoomViewModel(RoomsRepository ralphRepository) {
        super(ralphRepository);
    }


    public void removeRoom(Room room) {
        List<Room> currentRooms = Objects.requireNonNull(statusAwareLiveData.getValue()).getData();
        if (currentRooms == null) return;

        if (currentRooms.remove(room)) {
            statusAwareLiveData.postSuccess(currentRooms);
            startedRemoving = true;
        }
    }


    @Override
    public void fetchData(String... args) {
        if (statusAwareLiveData != null || startedRemoving) {
            return;
        }

        statusAwareLiveData = networkRepository.fetchMainData(args);
    }

    @Override
    public void reloadData(String... args) {
        statusAwareLiveData = networkRepository.fetchMainData(args);
    }
}
