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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

public class MergedItemViewModel extends NetworkViewModel<List<RecyclerViewItem>, MergedItemsRepository> {
    //private List<ValidationEntry> validationEntries = new ArrayList<>();
    private List<RecyclerViewItem> alreadyValidatedItems = new ArrayList<>();
    private Map<RecyclerViewItem, List<ValidationEntry>> validationEntries = new HashMap<>();

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
            mergedItem.finish(true);
            // Also add items from other rooms to alreadyValidatedItems
            updateAlreadyValidatedItems(mergedItem);

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
        mergedItem.finish(false);

        if (removeItem(currentItems, mergedItem)) {
            // Add removed item to alreadyValidatedItems.
            // Items that couldn't be removed (i.e. items from other rooms) should not be added to alreadyValidatedItems,
            // because they were not supposed to be in this room and the user confirmed that (this is logically impossible in the real world,
            // because in order to scan them in this room they need to be in this room)
            updateAlreadyValidatedItems(mergedItem);
        }
    }

    private void updateAlreadyValidatedItems(MergedItem finishedItem) {
        if (!alreadyValidatedItems.contains(finishedItem)) {

            Room subRoom = finishedItem.getSubroom();
            if (subRoom != null) {
                if (!alreadyValidatedItems.contains(subRoom)) {
                    alreadyValidatedItems.add(subRoom);
                }
                // Insert the item into the subRoom (to the top)
                alreadyValidatedItems.add(
                        alreadyValidatedItems.indexOf(subRoom) + 1,
                        finishedItem);

            } else {
                // Insert to top
                alreadyValidatedItems.add(0, finishedItem);
            }
        }
    }


    private boolean removeItem(List<RecyclerViewItem> currentItems, MergedItem mergedItem) {
        if (currentItems.remove(mergedItem)) {
            validatedCount++;
            statusAwareLiveData.postSuccess(currentItems);
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
        return setTotalItemsCount() - validatedCount;
    }

    private boolean totalItemCountUnknown() {
        return totalItemsCount == TOTAL_COUNT_NOT_SET_YET;
    }

    public int setTotalItemsCount() {
        if (totalItemCountUnknown()) {
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

    public void addValidationEntry(MergedItem currentItem, ValidationEntry validationEntry) {
        List<ValidationEntry> validationEntriesForItem = validationEntries.get(currentItem);
        if (validationEntriesForItem == null) {
            validationEntriesForItem = new ArrayList<>();
        }
        validationEntriesForItem.add(validationEntry);
        this.validationEntries.put(currentItem, validationEntriesForItem);
    }

    public List<ValidationEntry> getValidationEntries() {
        List<ValidationEntry> ret = new ArrayList<>();
        for (List<ValidationEntry> value : validationEntries.values()) {
            ret.addAll(value);
        }

        return ret;
    }

    public void sendValidationEntriesToServer() {
        try {
            // TODO: DO NOT USE DAGGER
            validateSuccessful = networkRepository.sendValidationEntriesToServer(ValidationEntry.getValidationEntriesAsJson(getValidationEntries()));
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

    public boolean returnItems(List<MergedItem> itemsToRevise) {
        if (itemsToRevise.isEmpty()) return true;
        if (getLiveData().getValue() == null || getLiveData().getValue().getData() == null)
            return false;

        List<RecyclerViewItem> currentItems = getLiveData().getValue().getData();


        for (MergedItem mergedItem : itemsToRevise) {
            alreadyValidatedItems.remove(mergedItem);
            validationEntries.remove(mergedItem);
            validatedCount -= mergedItem.getTimesFoundCurrent();
            mergedItem.unfinish();

            if (mergedItem.getSubroom() != null) {
                // Insert the item into the subRoom (to the top)
                currentItems.add(
                        alreadyValidatedItems.indexOf(mergedItem.getSubroom()) + 1, mergedItem);
            } else {
                // Insert to top
                currentItems.add(0, mergedItem);
            }
        }

        statusAwareLiveData.postSuccess(currentItems);
        updateProgressMessage();

        return true;
    }
}
