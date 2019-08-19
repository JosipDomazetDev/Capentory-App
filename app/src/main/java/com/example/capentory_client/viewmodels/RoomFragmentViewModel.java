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
    private MutableLiveData<Boolean> mIsUpdating = new MutableLiveData<>();
    private MutableLiveData<List<String>> roomNumbersLiveData = new MutableLiveData<>();


    @Inject
    public RoomFragmentViewModel(RalphRepository ralphRepository) {
        this.ralphRepository = ralphRepository;
    }

    public void init() {
        if (rooms != null) {
            return;
        }
        rooms = ralphRepository.getRooms();
        Observer<List<ActualRoom>> observer = new Observer<List<ActualRoom>>() {
            @Override
            public void onChanged(List<ActualRoom> actualRooms) {
                List<String> roomNumberList = new ArrayList<>();

                if (actualRooms == null) {
                    roomNumberList.add("Loading...");
                } else
                    for (ActualRoom room : Objects.requireNonNull(actualRooms)) {
                        roomNumberList.add(room.getRoomNumber());
                    }

                roomNumbersLiveData.setValue(roomNumberList);
            }
        };
        rooms.observeForever(observer);
    }


    public LiveData<List<String>> getRoomNumberStrings() {
        return roomNumbersLiveData;
    }


    public LiveData<List<ActualRoom>> getRooms() {
        return rooms;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

    }

    public LiveData<Boolean> getIsUpdating() {
        return mIsUpdating;
    }

}
