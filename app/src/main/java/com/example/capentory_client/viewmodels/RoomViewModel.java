package com.example.capentory_client.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.capentory_client.models.Room;
import com.example.capentory_client.models.Stocktaking;
import com.example.capentory_client.repos.RoomsRepository;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

public class RoomViewModel extends NetworkViewModel<List<Room>, RoomsRepository> {
    private int amountOfValidatedRooms = 0;

    @Inject
    public RoomViewModel(RoomsRepository ralphRepository) {
        super(ralphRepository);
    }


    public boolean noRoomsLeft() {
        try {
            return Objects.requireNonNull(Objects.requireNonNull(statusAwareLiveData.getValue()).getData()).isEmpty();
        } catch (NullPointerException e) {
            return false;
        }
    }

    public int getAmountOfValidatedRooms() {
        return amountOfValidatedRooms;
    }

    @Override
    public void fetchData(String... args) {
        if (statusAwareLiveData != null || amountOfValidatedRooms > 0) {
            return;
        }

        statusAwareLiveData = networkRepository.fetchMainData(args);
    }

    @Override
    public void reloadData(String... args) {
        statusAwareLiveData = networkRepository.fetchMainData(args);
    }

    public void finishRoom() {
        amountOfValidatedRooms++;
        reloadData();
    }
}
