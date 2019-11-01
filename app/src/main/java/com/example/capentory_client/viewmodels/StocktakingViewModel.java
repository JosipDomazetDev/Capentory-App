package com.example.capentory_client.viewmodels;

import androidx.lifecycle.LiveData;

import com.example.capentory_client.models.SerializerEntry;
import com.example.capentory_client.repos.StocktakingRepository;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import java.util.List;

import javax.inject.Inject;

public class StocktakingViewModel extends NetworkViewModel<List<SerializerEntry>, StocktakingRepository> {
    private StatusAwareLiveData<Boolean> postStocktakingSuccessful;


    @Inject
    public StocktakingViewModel(StocktakingRepository jsonRepository) {
        super(jsonRepository);
    }

    @Override
    public void fetchData(String... args) {
        if (statusAwareLiveData != null) {
            return;
        }
        statusAwareLiveData = jsonRepository.fetchMainData(args);
    }

    @Override
    public void reloadData(String... args) {
        statusAwareLiveData = jsonRepository.fetchMainData(args);
    }

    public void postStocktaking(String name) {
        postStocktakingSuccessful = jsonRepository.postStocktaking(name);
    }

    public LiveData<StatusAwareData<Boolean>> getPostStocktakingSuccessful() {
        return postStocktakingSuccessful;
    }
}
