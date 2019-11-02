package com.example.capentory_client.viewmodels;

import androidx.lifecycle.LiveData;

import com.example.capentory_client.models.SerializerEntry;
import com.example.capentory_client.models.Stocktaking;
import com.example.capentory_client.repos.StocktakingRepository;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import java.util.List;

import javax.inject.Inject;

public class StocktakingViewModel extends NetworkViewModel<List<SerializerEntry>, StocktakingRepository> {
    private StatusAwareLiveData<Stocktaking> postedStocktaking;

    @Inject
    public StocktakingViewModel(StocktakingRepository jsonRepository) {
        super(jsonRepository);
    }

    @Override
    public void fetchData(String... args) {
        if (statusAwareLiveData != null) {
            return;
        }


        statusAwareLiveData = networkRepository.fetchMainData(args);
    }

    @Override
    public void reloadData(String... args) {
        statusAwareLiveData = networkRepository.fetchMainData(args);
    }

    public void postStocktaking(String name, String comment) {
        postedStocktaking = networkRepository.postStocktaking(name,comment);
    }

    public LiveData<StatusAwareData<Stocktaking>> getPostedStocktaking() {
        return postedStocktaking;
    }
}
