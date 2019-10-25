package com.example.capentory_client.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.capentory_client.repos.JsonRepository;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

public abstract class NetworkViewModel<P, R extends JsonRepository<P>> extends ViewModel {
    protected StatusAwareLiveData<P> statusAwareLiveData;
    protected R jsonRepository;

    public NetworkViewModel(R jsonRepository) {
        this.jsonRepository = jsonRepository;
    }

    public abstract void fetchData(String... args);


    public LiveData<StatusAwareData<P>> getData() {
        return this.statusAwareLiveData;
    }


    public abstract void reloadData(String... args);


}
