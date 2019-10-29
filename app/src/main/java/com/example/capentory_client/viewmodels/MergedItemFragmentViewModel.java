package com.example.capentory_client.viewmodels;

import androidx.lifecycle.LiveData;

import com.example.capentory_client.models.MergedItem;
import com.example.capentory_client.models.ValidationEntry;
import com.example.capentory_client.repos.MergedItemsRepository;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

public class MergedItemFragmentViewModel extends NetworkViewModel<List<MergedItem>, MergedItemsRepository> {
    private List<ValidationEntry> validationEntries = new ArrayList<>();
    private StatusAwareLiveData<Boolean> validateSuccessful = new StatusAwareLiveData<>();


    @Inject
    public MergedItemFragmentViewModel(MergedItemsRepository mergedItemsRepository) {
        super(mergedItemsRepository);
    }


    public void removeItem(MergedItem mergedItem) {
        List<MergedItem> currentItems = Objects.requireNonNull(statusAwareLiveData.getValue()).getData();
        if (currentItems == null) return;

        if (currentItems.remove(mergedItem))
            statusAwareLiveData.postSuccess(currentItems);
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
        if (validationEntries.isEmpty())
            statusAwareLiveData = jsonRepository.fetchMainData(args);
    }

    public void addValidationEntry(ValidationEntry validationEntry) {
        validationEntries.add(validationEntry);
    }

    public void sendValidationEntriesToServer() {

        validateSuccessful = jsonRepository.sendValidationEntriesToServer(ValidationEntry.getValidationEntriesAsJson(validationEntries));
    }

    public LiveData<StatusAwareData<Boolean>> getValidationSuccessful() {
        return validateSuccessful;
    }


    public int getAmountOfItemsLeft() {
        return Objects.requireNonNull(Objects.requireNonNull(statusAwareLiveData.getValue()).getData()).size();
    }
}
