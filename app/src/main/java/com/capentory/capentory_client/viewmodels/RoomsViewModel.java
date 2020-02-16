package com.capentory.capentory_client.viewmodels;

import androidx.lifecycle.LiveData;

import com.capentory.capentory_client.models.Room;
import com.capentory.capentory_client.repos.RoomsRepository;
import com.capentory.capentory_client.ui.MainActivity;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

public class RoomsViewModel extends NetworkViewModel<List<Room>, RoomsRepository> {
    private int amountOfValidatedRooms = 0;

    @Inject
    public RoomsViewModel(RoomsRepository ralphRepository) {
        super(ralphRepository);
    }


    public boolean noRoomsLeft() {
        try {
            if (statusAwareLiveData.getValue() == null) return true;
            if (statusAwareLiveData.getValue().getData() == null) return true;

            return statusAwareLiveData.getValue().getData().isEmpty();
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
        // No need to reload on never ending stocktaking, since rooms won't be removed
        if (!MainActivity.getStocktaking().isNeverEndingStocktaking()) {
            reloadData();
        }
    }
}
