package com.example.capentory_client.viewmodels;

import androidx.lifecycle.LiveData;

import com.example.capentory_client.models.MergedItem;
import com.example.capentory_client.models.RecyclerviewItem;
import com.example.capentory_client.models.Room;
import com.example.capentory_client.models.ValidationEntry;
import com.example.capentory_client.repos.MergedItemsRepository;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

public class MergedItemViewModel extends NetworkViewModel<List<RecyclerviewItem>, MergedItemsRepository> {
    private List<ValidationEntry> validationEntries = new ArrayList<>();
    private List<MergedItem> alreadyValidatedItems = new ArrayList<>();
    private StatusAwareLiveData<Boolean> validateSuccessful;
    private boolean startedRemoving;

    @Inject
    public MergedItemViewModel(MergedItemsRepository mergedItemsRepository) {
        super(mergedItemsRepository);
    }


    public void removeItemByFoundCounterIncrease(MergedItem mergedItem) {
        List<RecyclerviewItem> currentItems = Objects.requireNonNull(statusAwareLiveData.getValue()).getData();
        if (currentItems == null) return;

        mergedItem.increaseTimesFoundCurrent();
        startedRemoving = true;

        if (mergedItem.getTimesFoundCurrent() >= mergedItem.getTimesFoundLast()) {
            // We are not adding a NewItem to alreadyValidatedItems because we dont want subitems for any kind of NewItem
            if (!mergedItem.isNewItem()) alreadyValidatedItems.add(mergedItem);

            if (currentItems.remove(mergedItem)) {
                statusAwareLiveData.postSuccess(currentItems);
            }
        }
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
        if (!startedRemoving) {
            statusAwareLiveData = networkRepository.fetchMainData(args);
            validationEntries.clear();
            alreadyValidatedItems.clear();
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

    public void removeItemDirectly(MergedItem mergedItem) {
        List<RecyclerviewItem> currentItems = Objects.requireNonNull(statusAwareLiveData.getValue()).getData();
        if (currentItems == null) return;

        if (currentItems.remove(mergedItem)) {
            statusAwareLiveData.postSuccess(currentItems);
            startedRemoving = true;
        }
    }


    public MergedItem getMergedItemFromBarcode(String barcode) {
        for (MergedItem alreadyValidatedItem : alreadyValidatedItems) {
            if (alreadyValidatedItem.equalsBarcode(barcode))
                return alreadyValidatedItem;
        }
        return null;
    }

    public Room getSuperRoom() {
        if (statusAwareLiveData.getValue() == null) return null;
        List<RecyclerviewItem> items = statusAwareLiveData.getValue().getData();
        assert items != null;
        for (RecyclerviewItem item : items) {
            if (item instanceof Room) {
                Room room = (Room) item;
                if (room.isTopLevelRoom())
                    return room;
            }
        }
        return null;
    }
}
