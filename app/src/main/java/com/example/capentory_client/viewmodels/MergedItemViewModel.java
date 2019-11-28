package com.example.capentory_client.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.capentory_client.models.MergedItem;
import com.example.capentory_client.models.RecyclerviewItem;
import com.example.capentory_client.models.Room;
import com.example.capentory_client.models.ValidationEntry;
import com.example.capentory_client.repos.MergedItemsRepository;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import org.json.JSONException;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

public class MergedItemViewModel extends NetworkViewModel<List<RecyclerviewItem>, MergedItemsRepository> {
    private List<ValidationEntry> validationEntries = new ArrayList<>();
    private List<MergedItem> alreadyValidatedItems = new ArrayList<>();
    private StatusAwareLiveData<Boolean> validateSuccessful;
    private MutableLiveData<String> progressMessage = new MutableLiveData<>();
    private boolean startedRemoving;
    private int validatedCount = 0;
    private int totalItemsCount = 0;

    @Inject
    public MergedItemViewModel(MergedItemsRepository mergedItemsRepository) {
        super(mergedItemsRepository);
    }

    public void removeItemByFoundCounterIncrease(MergedItem mergedItem) {
        List<RecyclerviewItem> currentItems = Objects.requireNonNull(statusAwareLiveData.getValue()).getData();
        if (currentItems == null) return;

        mergedItem.increaseTimesFoundCurrent();
        if (mergedItem.isNewItem()) {
            totalItemsCount++;
        }
        validatedCount++;
        updateProgressMessage();
        startedRemoving = true;

        if (mergedItem.getTimesFoundCurrent() >= mergedItem.getTimesFoundLast()) {
            alreadyValidatedItems.add(mergedItem);

            if (currentItems.remove(mergedItem)) {
                statusAwareLiveData.postSuccess(currentItems);
                mergedItem.setExpanded(false);
            }
        }
    }

    public void removeCanceledItemDirectly(MergedItem mergedItem) {
        List<RecyclerviewItem> currentItems = Objects.requireNonNull(statusAwareLiveData.getValue()).getData();
        if (currentItems == null) return;

        if (currentItems.remove(mergedItem)) {
            // New item cannot be removed, therefore we can increase the validated count without further checks
            validatedCount++;
            statusAwareLiveData.postSuccess(currentItems);
            startedRemoving = true;
            mergedItem.setExpanded(false);
        }
    }

    @Override
    public void fetchData(String... args) {
        if (statusAwareLiveData != null) {
            return;
        }

        statusAwareLiveData = networkRepository.fetchMainData(args);
    }


    /*public int getAmountOfTotalItems() {
        List<RecyclerviewItem> recyclerviewItems = Objects.requireNonNull(Objects.requireNonNull(statusAwareLiveData.getValue()).getData());
        int c = 0;
        for (RecyclerviewItem recyclerviewItem : recyclerviewItems) {
            if (recyclerviewItem instanceof MergedItem) {
                MergedItem mergedItem = (MergedItem) recyclerviewItem;
                // If user created more subitems the total number of items should be higher ;)
                c += Math.max(mergedItem.getTimesFoundLast(), mergedItem.getTimesFoundCurrent());
            }
        }
        return c;
    }*/


    @Override
    public void reloadData(String... args) {
        if (!startedRemoving) {
            statusAwareLiveData = networkRepository.fetchMainData(args);
            validationEntries.clear();
            alreadyValidatedItems.clear();
            validatedCount = 0;
        }
    }

    public void addValidationEntry(ValidationEntry validationEntry) {
        validationEntries.add(validationEntry);
    }

    public void sendValidationEntriesToServer() {
        try {
            validateSuccessful = networkRepository.sendValidationEntriesToServer(ValidationEntry.getValidationEntriesAsJson(validationEntries));
        } catch (JSONException e) {
            statusAwareLiveData.postError(e);
        }
    }

    public LiveData<StatusAwareData<Boolean>> getValidationSuccessful() {
        return validateSuccessful;
    }


    public int getAmountOfItemsLeft() {
        List<RecyclerviewItem> recyclerviewItems = Objects.requireNonNull(Objects.requireNonNull(statusAwareLiveData.getValue()).getData());
        int c = 0;
        for (RecyclerviewItem recyclerviewItem : recyclerviewItems) {
            if (recyclerviewItem instanceof MergedItem) {
                MergedItem mergedItem = (MergedItem) recyclerviewItem;
                c += mergedItem.getTimesFoundLast() - mergedItem.getTimesFoundCurrent();
            }
        }
        return c;
    }

    public MergedItem getMergedItemFromBarcode(String barcode) {
        for (MergedItem alreadyValidatedItem : alreadyValidatedItems) {
            if (alreadyValidatedItem.equalsBarcode(barcode))
                return alreadyValidatedItem;
        }
        return null;
    }

    public List<Room> getRooms() {
        if (statusAwareLiveData.getValue() == null) return null;
        List<RecyclerviewItem> items = statusAwareLiveData.getValue().getData();
        assert items != null;

        List<Room> rooms = new ArrayList<>();
        for (RecyclerviewItem item : items) {
            if (item instanceof Room) {
                rooms.add((Room) item);
            }
        }
        return rooms;
    }

    public LiveData<String> getProgressMessage() {
        //progressMessage = new MutableLiveData<>();
        if (this.totalItemsCount == 0) {
            this.totalItemsCount = networkRepository.getTotalItemsCount();
        }

        updateProgressMessage();
        return progressMessage;
    }

    public void updateProgressMessage() {
        progressMessage.postValue(validatedCount + "/" + totalItemsCount);
    }

}
