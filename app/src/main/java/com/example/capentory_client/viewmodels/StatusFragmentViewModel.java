package com.example.capentory_client.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.capentory_client.repos.Repository;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

public abstract class StatusFragmentViewModel<L> extends ViewModel {
    protected StatusAwareLiveData<L> statusAwareLiveData;
    protected Repository<L> repository;

    public StatusFragmentViewModel(Repository<L> repository) {
        this.repository = repository;
    }

    public abstract void fetchData();

    public abstract LiveData<StatusAwareData<L>> getData();

    public abstract void reloadData();
}
