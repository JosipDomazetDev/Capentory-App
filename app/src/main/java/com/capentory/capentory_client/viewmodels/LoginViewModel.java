package com.capentory.capentory_client.viewmodels;

import androidx.lifecycle.LiveData;

import com.capentory.capentory_client.repos.LoginRepository;
import com.capentory.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;
import com.capentory.capentory_client.viewmodels.wrappers.StatusAwareData;

import javax.inject.Inject;

public class LoginViewModel extends NetworkViewModel<String, LoginRepository> {
    private StatusAwareLiveData<Boolean> logoutSuccessful;


    @Inject
    public LoginViewModel(LoginRepository loginRepository) {
        super(loginRepository);

    }


    @Override
    public void fetchData(String... args) {
        statusAwareLiveData = networkRepository.fetchMainData(args);
    }

    @Override
    public void reloadData(String... args) {
        statusAwareLiveData = networkRepository.fetchMainData();
    }


    public void logout() {
        logoutSuccessful = networkRepository.logout();
    }

    public LiveData<StatusAwareData<Boolean>> getLogoutSuccessful() {
        return logoutSuccessful;
    }

}
