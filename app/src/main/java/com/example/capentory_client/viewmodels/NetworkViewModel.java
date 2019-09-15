package com.example.capentory_client.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.capentory_client.models.ActualRoom;
import com.example.capentory_client.repos.JsonRepository;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import java.util.List;
import java.util.Objects;

public abstract class NetworkViewModel<L> extends ViewModel {
    protected StatusAwareLiveData<L> statusAwareLiveData;
    protected JsonRepository<L> jsonRepository;

    public NetworkViewModel(JsonRepository<L> jsonRepository) {
        this.jsonRepository = jsonRepository;
    }

    public abstract void fetchData(String... args);


    public LiveData<StatusAwareData<L>> getData() {
        return statusAwareLiveData;
    }

    public abstract void reloadData(String... args);


}
