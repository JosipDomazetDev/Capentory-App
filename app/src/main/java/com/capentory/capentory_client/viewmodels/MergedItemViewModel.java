package com.capentory.capentory_client.viewmodels;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.capentory.capentory_client.models.MergedItem;
import com.capentory.capentory_client.models.RecyclerViewItem;
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

public class MergedItemViewModel extends NetworkViewModel<List<RecyclerViewItem>, MergedItemsRepository> {
    private List<ValidationEntry> validationEntries = new ArrayList<>();
    private List<RecyclerViewItem> alreadyValidatedItems = new ArrayList<>();

    private List<Room> subRoomListForItemDetail;
    private StatusAwareLiveData<Boolean> validateSuccessful;

    private MutableLiveData<String> progressMessage = new MutableLiveData<>();
    private int validatedCount = 0;
    // Indicates not counted yet
    private static final int TOTAL_COUNT_NOT_SET_YET = -1;
    private int totalItemsCount = TOTAL_COUNT_NOT_SET_YET;


    @Inject
    public MergedItemViewModel(MergedItemsRepository mergedItemsRepository) {
        super(mergedItemsRepository);
    }

    // ========================= RecyclerView logic =========================

    public void removeItemByFoundCounterIncrease(MergedItem mergedItem) {
        List<RecyclerViewItem> currentItems = Objects.requireNonNull(statusAwareLiveData.getValue()).getData();
        if (currentItems == null) return;
        mergedItem.increaseTimesFoundCurrent();
        if (mergedItem.getTimesFoundCurrent() >= mergedItem.getTimesFoundLast()) {
            if (!alreadyValidatedItems.contains(mergedItem)) {
                alreadyValidatedItems.add(mergedItem.finish(true));
            }

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
        List<RecyclerViewItem> currentItems = Objects.requireNonNull(statusAwareLiveData.getValue()).getData();
        if (currentItems == null) return;

        // New items cannot be removed anyways, so no need to check
        if (removeItem(currentItems, mergedItem)) {
            if (!alreadyValidatedItems.contains(mergedItem)) {
                alreadyValidatedItems.add(mergedItem.finish(false));
            }
        }
    }

    private boolean removeItem(List<RecyclerViewItem> currentItems, MergedItem mergedItem) {
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

        resetAndFetch(args);
    }


    @Override
    public void reloadData(String... args) {
        if (validatedCount == 0) {
            resetAndFetch(args);
        }
    }

    private void resetAndFetch(String[] args) {
        validationEntries.clear();
        alreadyValidatedItems.clear();
        if (subRoomListForItemDetail != null)
            subRoomListForItemDetail.clear();
        statusAwareLiveData = networkRepository.fetchMainData(args);
    }


    @Nullable
    public List<Room> getRooms() {
        if (subRoomListForItemDetail != null) return subRoomListForItemDetail;

        if (getLiveData().getValue() == null || getLiveData().getValue().getData() == null)
            return null;

        // If subRooms are not known, set them once
        List<Room> rooms = new ArrayList<>();

        for (RecyclerViewItem item : getLiveData().getValue().getData()) {
            if (item instanceof Room) {
                rooms.add((Room) item);
            }
        }

        this.subRoomListForItemDetail = rooms;
        return rooms;
    }

    @NonNull
    public List<MergedItem> getMergedItems() {
        List<MergedItem> mergedItems = new ArrayList<>();

        if (getLiveData().getValue() == null || getLiveData().getValue().getData() == null)
            return mergedItems;


        for (RecyclerViewItem recyclerviewItem : getLiveData().getValue().getData()) {
            if (recyclerviewItem instanceof MergedItem) {
                mergedItems.add((MergedItem) recyclerviewItem);
            }
        }

        return mergedItems;
    }


    // ========================= Progress message logic =========================


    public int getAmountOfItemsLeft() {
     /*   List<RecyclerViewItem> recyclerviewItems = Objects.requireNonNull(Objects.requireNonNull(statusAwareLiveData.getValue()).getLiveData());
        int c = 0;
        for (RecyclerViewItem recyclerviewItem : recyclerviewItems) {
            if (recyclerviewItem instanceof MergedItem) {
                MergedItem mergedItem = (MergedItem) recyclerviewItem;
                c += mergedItem.getTimesFoundLast() - mergedItem.getTimesFoundCurrent();
            }
        }*/

        return getTotalItemsCount() - validatedCount;
    }

    public boolean totalItemCountUnkown() {
        return totalItemsCount == TOTAL_COUNT_NOT_SET_YET;
    }

    private int getTotalItemsCount() {
        if (totalItemCountUnkown()) {
            this.totalItemsCount = networkRepository.getTotalItemsCount();
        }

        return totalItemsCount;
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

    // ========================= Validation logic =========================

    public void addValidationEntry(ValidationEntry validationEntry) {
        validationEntries.add(validationEntry);
    }

    public List<ValidationEntry> getValidationEntries() {
        return validationEntries;
    }

    public void sendValidationEntriesToServer() {
        try {
            // TODO: DO NOT USE DAGGER
            validateSuccessful = networkRepository.sendValidationEntriesToServer(ValidationEntry.getValidationEntriesAsJson(validationEntries));
        } catch (JSONException e) {
            statusAwareLiveData.postError(e);
        }
    }

    public LiveData<StatusAwareData<Boolean>> getValidationSuccessful() {
        return validateSuccessful;
    }

    public MergedItem getAlreadyValidatedItemFromBarcode(String barcode) {
        for (RecyclerViewItem alreadyValidatedItem : alreadyValidatedItems) {
            if (alreadyValidatedItem instanceof MergedItem) {
                if (((MergedItem) alreadyValidatedItem).equalsBarcode(barcode))
                    return (MergedItem) alreadyValidatedItem;
            }
        }
        return null;
    }


    public List<RecyclerViewItem> getAlreadyValidatedItems() {
        return alreadyValidatedItems;
    }
}
