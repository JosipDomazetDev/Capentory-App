package com.example.capentory_client.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.capentory_client.repos.AuthRepository;
import com.example.capentory_client.repos.FormRepository;
import com.example.capentory_client.repos.JsonRepository;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import javax.inject.Inject;

public class LoginFragmentViewModel extends NetworkViewModel<String> {
    protected StatusAwareLiveData<Boolean> logoutSuccessful;


    @Inject
    public LoginFragmentViewModel(AuthRepository authRepository) {
        super(authRepository);
    }


    @Override
    public void fetchData(String... args) {
        statusAwareLiveData = jsonRepository.fetchData(args);
    }

    @Override
    public void reloadData(String... args) {
        statusAwareLiveData = jsonRepository.fetchData();
    }


    public void logout() {
        logoutSuccessful = ((AuthRepository) jsonRepository).logout();
    }

    public LiveData<StatusAwareData<Boolean>> getLogoutSuccessful() {
        return logoutSuccessful;
    }

}
