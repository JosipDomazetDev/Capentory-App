package com.example.capentory_client.viewmodels;

import androidx.lifecycle.LiveData;

import com.example.capentory_client.repos.LoginRepository;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import javax.inject.Inject;

public class LoginViewModel extends NetworkViewModel<String, LoginRepository> {
    private StatusAwareLiveData<Boolean> logoutSuccessful;


    @Inject
    public LoginViewModel(LoginRepository loginRepository) {
        super(loginRepository);

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
