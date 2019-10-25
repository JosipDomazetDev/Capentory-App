package com.example.capentory_client.viewmodels;

import androidx.lifecycle.LiveData;

import com.example.capentory_client.repos.AuthRepository;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import javax.inject.Inject;

public class LoginFragmentViewModel extends NetworkViewModel<String, AuthRepository> {
    protected StatusAwareLiveData<Boolean> logoutSuccessful;


    @Inject
    public LoginFragmentViewModel(AuthRepository authRepository) {
        super(authRepository);

    }


    @Override
    public void fetchData(String... args) {
        statusAwareLiveData = jsonRepository.fetchMainData(args);
    }

    @Override
    public void reloadData(String... args) {
        statusAwareLiveData = jsonRepository.fetchMainData();
    }


    public void logout() {
        logoutSuccessful = jsonRepository.logout();
    }

    public LiveData<StatusAwareData<Boolean>> getLogoutSuccessful() {
        return logoutSuccessful;
    }

}
