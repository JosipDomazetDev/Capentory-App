package com.example.capentory_client.viewmodels;

import com.example.capentory_client.models.ActualRoom;
import com.example.capentory_client.repos.ActualRoomsRepository;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

public class RoomFragmentViewModel extends NetworkViewModel<List<ActualRoom>, ActualRoomsRepository> {


    @Inject
    public RoomFragmentViewModel(ActualRoomsRepository ralphRepository) {
        super(ralphRepository);
    }


    public void removeRoom(ActualRoom actualRoom) {
        List<ActualRoom> currentRooms = Objects.requireNonNull(statusAwareLiveData.getValue()).getData();
        if (currentRooms == null) return;

        if (currentRooms.remove(actualRoom))
            statusAwareLiveData.postSuccess(currentRooms);
    }


    @Override
    public void fetchData(String... args) {
        if (statusAwareLiveData != null) {
            return;
        }

        statusAwareLiveData = jsonRepository.fetchMainData(args);
    }

    @Override
    public void reloadData(String... args) {
        statusAwareLiveData = jsonRepository.fetchMainData(args);
    }
}
