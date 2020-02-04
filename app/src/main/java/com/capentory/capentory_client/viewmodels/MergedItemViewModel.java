package com.capentory.capentory_client.viewmodels;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.capentory.capentory_client.models.MergedItem;
import com.capentory.capentory_client.models.RecyclerviewItem;
import com.capentory.capentory_client.models.Room;
import com.capentory.capentory_client.models.ValidationEntry;
import com.capentory.capentory_client.repos.MergedItemsRepository;
import com.capentory.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;
import com.capentory.capentory_client.viewmodels.wrappers.StatusAwareData;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

public class MergedItemViewModel extends NetworkViewModel<List<RecyclerviewItem>, MergedItemsRepository> {
    private List<ValidationEntry> validationEntries = new ArrayList<>();
    private List<MergedItem> alreadyValidatedItems = new ArrayList<>();
    private List<Room> subRoomListForItemDetails;
    private StatusAwareLiveData<Boolean> validateSuccessful;

    private MutableLiveData<String> progressMessage = new MutableLiveData<>();
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
        if (mergedItem.getTimesFoundCurrent() >= mergedItem.getTimesFoundLast()) {
            alreadyValidatedItems.add(mergedItem);

            if (!removeItem(currentItems, mergedItem)) {
                // This is called when a additional item (subitem or new item) is added,
                // because an additional item is not part of the remaining items and can therefore not be removed
                validatedCount++;
                totalItemsCount++;
            }
        }

        updateProgressMessage();
    }

    public void removeCanceledItemDirectly(MergedItem mergedItem) {
        List<RecyclerviewItem> currentItems = Objects.requireNonNull(statusAwareLiveData.getValue()).getData();
        if (currentItems == null) return;

        // New items cannot be removed anyways, so no need to check
        removeItem(currentItems, mergedItem);
    }

    private boolean removeItem(List<RecyclerviewItem> currentItems, MergedItem mergedItem) {
        if (currentItems.remove(mergedItem)) {
            validatedCount++;
            statusAwareLiveData.postSuccess(currentItems);

            if (mergedItem.getSubroom() != null) {
                mergedItem.getSubroom().getMergedItems().remove(mergedItem);
            }
            return true;
        }

        return false;
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
        if (validatedCount == 0) {
            statusAwareLiveData = networkRepository.fetchMainData(args);
            validationEntries.clear();
            alreadyValidatedItems.clear();
            if (subRoomListForItemDetails != null)
                subRoomListForItemDetails.clear();
        }
    }

    public void addValidationEntry(ValidationEntry validationEntry) {
        validationEntries.add(validationEntry);
    }

    public void sendValidationEntriesToServer() {
        try {
            // TODO: DO NOT USE DAGGER
            validateSuccessful = networkRepository.sendValidationEntriesToServer(ValidationEntry.getValidationEntriesAsJson(validationEntries, null));
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

    @Nullable
    public List<Room> getRooms() {
        if (subRoomListForItemDetails != null) return subRoomListForItemDetails;

        // If subRooms are not known, add them once
        if (statusAwareLiveData.getValue() == null) return null;
        List<RecyclerviewItem> items = statusAwareLiveData.getValue().getData();
        if (items == null) return null;

        List<Room> rooms = new ArrayList<>();
        for (RecyclerviewItem item : items) {
            if (item instanceof Room) {
                rooms.add((Room) item);
            }
        }
        this.subRoomListForItemDetails = rooms;
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
