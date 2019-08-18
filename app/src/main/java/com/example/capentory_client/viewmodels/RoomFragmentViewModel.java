package com.example.capentory_client.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.capentory_client.models.ActualRoom;
import com.example.capentory_client.repos.RalphRepository;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

public class RoomFragmentViewModel extends ViewModel {
    private MutableLiveData<List<ActualRoom>> rooms;
    private MutableLiveData<List<String>> roomNumberStrings;
    private RalphRepository ralphRepository;
    private MutableLiveData<Boolean> mIsUpdating = new MutableLiveData<>();

   /* @Inject
    public RoomFragmentViewModel(RalphRepository ralphRepository) {
        this.ralphRepository=ralphRepository;
    }*/

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


    public LiveData<List<String>> getRoomNumberStrings() {
        MutableLiveData<List<String>> roomNumberLiveData = new MutableLiveData<>();
        List<String> roomNumberList = new ArrayList<>();

        for (ActualRoom room : Objects.requireNonNull(rooms.getValue())) {
            roomNumberList.add(room.getRoomNumber());
        }

        roomNumberLiveData.setValue(roomNumberList);
        return roomNumberLiveData;
    }


    public LiveData<List<ActualRoom>> getRooms() {
        return rooms;
    }



    public LiveData<Boolean> getIsUpdating() {
        return mIsUpdating;
    }

}
