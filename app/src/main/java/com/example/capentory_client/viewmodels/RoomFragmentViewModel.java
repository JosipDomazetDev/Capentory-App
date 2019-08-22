package com.example.capentory_client.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.example.capentory_client.models.ActualRoom;
import com.example.capentory_client.repos.RalphRepository;
import com.example.capentory_client.viewmodels.adapter.RecyclerViewAdapter;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

public class RoomFragmentViewModel extends ViewModel {
    private MutableLiveData<List<ActualRoom>> rooms;
    private RalphRepository ralphRepository;


    @Inject
    public RoomFragmentViewModel(RalphRepository ralphRepository) {
        this.ralphRepository = ralphRepository;
    }

    public void init() {
        if (rooms != null) {
            return;
        }
        rooms = ralphRepository.getRooms();
    }

    public void reloadRooms() {
        rooms = ralphRepository.getRooms();
    }

    public LiveData<List<ActualRoom>> getRooms() {
        return rooms;
    }

    public void removeRoom(ActualRoom actualRoom) {
        List<ActualRoom> currentRooms = rooms.getValue();
        assert currentRooms != null;
        currentRooms.remove(actualRoom);
        rooms.postValue(currentRooms);
    }


    @Override
    protected void onCleared() {
        super.onCleared();

    }

    public LiveData<Exception> getException() {
        return ralphRepository.getException();
    }

    public void resetExceptionState() {
        ralphRepository.resetExceptionState();
    }
}
