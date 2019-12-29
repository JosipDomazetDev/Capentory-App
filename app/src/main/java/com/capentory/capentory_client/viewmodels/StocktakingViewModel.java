package com.capentory.capentory_client.viewmodels;

import androidx.lifecycle.LiveData;

import com.capentory.capentory_client.models.MergedItem;
import com.capentory.capentory_client.models.SerializerEntry;
import com.capentory.capentory_client.models.Stocktaking;
import com.capentory.capentory_client.repos.StocktakingRepository;
import com.capentory.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;
import com.capentory.capentory_client.viewmodels.wrappers.StatusAwareData;

import java.util.List;

import javax.inject.Inject;

public class StocktakingViewModel extends NetworkViewModel<List<SerializerEntry>, StocktakingRepository> {
    private StatusAwareLiveData<List<Stocktaking>> stocktakingsLiveData;
    private StatusAwareLiveData<MergedItem> specificallySearchedForItem;

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
        stocktakingsLiveData = networkRepository.fetchStocktakings();
    }

    public void fetchStocktakings() {
        if (getStocktakings() != null) {
            return;
        }

        stocktakingsLiveData = networkRepository.fetchStocktakings();
    }

    public LiveData<StatusAwareData<List<Stocktaking>>> getStocktakings() {
        return stocktakingsLiveData;
    }


    public void fetchSpecificallySearchedForItem(String barcode) {
        specificallySearchedForItem = networkRepository.fetchSpecificallySearchedForItem(barcode);
    }

    public LiveData<StatusAwareData<MergedItem>> getSpecificallySearchedForItem() {
        return specificallySearchedForItem;
    }

}
