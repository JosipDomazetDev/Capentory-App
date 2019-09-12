package com.example.capentory_client.viewmodels;

import androidx.lifecycle.ViewModel;

import com.example.capentory_client.repos.Repository;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

public abstract class StatusFragmentViewModel<T> extends ViewModel {
    protected StatusAwareLiveData<T> statusAwareLiveData;
    protected Repository repository;

    public StatusFragmentViewModel(Repository repository) {
        this.repository = repository;
    }

    public abstract void fetchData();

    public abstract void reloadData();
}
