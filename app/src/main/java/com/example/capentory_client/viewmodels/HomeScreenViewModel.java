package com.example.capentory_client.viewmodels;

import com.example.capentory_client.models.MergedItem;
import com.example.capentory_client.repos.HomeScreenRepository;

import javax.inject.Inject;

public class HomeScreenViewModel extends NetworkViewModel<MergedItem, HomeScreenRepository> {
    @Inject
    public HomeScreenViewModel(HomeScreenRepository networkRepository) {
        super(networkRepository);
    }

    @Override
    public void fetchData(String... args) {
        statusAwareLiveData = networkRepository.fetchMainData(args);
    }

    @Override
    public void reloadData(String... args) {
        // Just press the button again
    }
}
