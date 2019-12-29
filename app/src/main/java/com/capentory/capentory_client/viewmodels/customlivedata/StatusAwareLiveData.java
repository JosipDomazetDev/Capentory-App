package com.capentory.capentory_client.viewmodels.customlivedata;

import androidx.lifecycle.MutableLiveData;

import com.capentory.capentory_client.viewmodels.wrappers.StatusAwareData;

public class StatusAwareLiveData<T> extends MutableLiveData<StatusAwareData<T>> {

    public void postFetching() {
        postValue(new StatusAwareData<T>().fetching());
    }

    public void postError(Exception exception) {
        postValue(new StatusAwareData<T>().error(exception));
    }

    public void postSuccess(T data) {
        postValue(new StatusAwareData<T>().success(data));
    }

}