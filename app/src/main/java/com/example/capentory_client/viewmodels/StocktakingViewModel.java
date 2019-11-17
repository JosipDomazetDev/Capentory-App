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
    private StatusAwareLiveData<List<Stocktaking>> stocktakingsLiveData;

    @Inject
    public StocktakingViewModel(StocktakingRepository jsonRepository) {
        super(jsonRepository);
    }

    @Override
    public void fetchData(String... args) {
        statusAwareLiveData = networkRepository.fetchMainData(args);
    }

    @Override
    public void reloadData(String... args) {
        statusAwareLiveData = networkRepository.fetchMainData(args);
        stocktakingsLiveData = networkRepository.fetchStocktakings();
    }

    public void fetchStocktakings() {
        stocktakingsLiveData = networkRepository.fetchStocktakings();
    }

    public LiveData<StatusAwareData<List<Stocktaking>>> getStocktakings() {
        return stocktakingsLiveData;
    }


}
