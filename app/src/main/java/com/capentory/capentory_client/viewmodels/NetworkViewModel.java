package com.capentory.capentory_client.viewmodels;

import android.widget.Button;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.capentory.capentory_client.repos.NetworkRepository;
import com.capentory.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;
import com.capentory.capentory_client.viewmodels.wrappers.StatusAwareData;

public abstract class NetworkViewModel<P, R extends NetworkRepository<P>> extends ViewModel {
    protected StatusAwareLiveData<P> statusAwareLiveData;
    R networkRepository;

    public NetworkViewModel(R networkRepository) {
        this.networkRepository = networkRepository;
    }

    public abstract void fetchData(String... args);


    public LiveData<StatusAwareData<P>> getLiveData() {
        return this.statusAwareLiveData;
    }

    public abstract void reloadData(String... args);


    @Override
    protected void onCleared() {
        super.onCleared();
        networkRepository.clearRequests();
    }
}
