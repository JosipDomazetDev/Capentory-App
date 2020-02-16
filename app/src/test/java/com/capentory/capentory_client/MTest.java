package com.capentory.capentory_client;

import android.content.Context;

import com.capentory.capentory_client.models.MergedItem;
import com.capentory.capentory_client.models.MergedItemField;
import com.capentory.capentory_client.models.RecyclerViewItem;
import com.capentory.capentory_client.models.Room;
import com.capentory.capentory_client.models.ValidationEntry;
import com.capentory.capentory_client.repos.MergedItemsRepository;
import com.capentory.capentory_client.viewmodels.MergedItemViewModel;
import com.capentory.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class MTest {
    DO_NOT_COMMIT_FINALS mockedResponses = new DO_NOT_COMMIT_FINALS();

    MergedItemViewModel mergedItemViewModel;

    @Mock
    MergedItemsRepository mergedItemsRepository;

    Context context;


    @Test
    public void testInventoryModels() throws JSONException {
        List<RecyclerViewItem> longData = null;
        List<RecyclerViewItem> shortData = null;
        try {
            longData = readItems(mockedResponses.getLongRoom());
            shortData = readItems(mockedResponses.getShortRoom());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // ===== We simulate being in a big room

        ArrayList<ValidationEntry> validationEntries = new ArrayList<>();

        for (int i = 0; i < longData.size(); i++) {
            RecyclerViewItem item = longData.get(i);
            if (item instanceof MergedItem) {
                validationEntries.add(simulateChanges(new ValidationEntry((MergedItem) item), i, (MergedItem) item));
            }
        }

        for (int i = 0; i < shortData.size(); i++) {
            RecyclerViewItem item = shortData.get(i);
            if (item instanceof MergedItem) {
                validationEntries.add(simulateChanges(new ValidationEntry((MergedItem) item), i, (MergedItem) item));
            }
        }


        assertEquals(ValidationEntry.getValidationEntriesAsJson(validationEntries), "");

    }

    private ValidationEntry simulateChanges(ValidationEntry validationEntry, int i, MergedItem item) throws JSONException {
        if (i % 2 == 0) {
            validationEntry.setStaticMarkForLater(true);
            validationEntry.addChangedFieldFromFormValue(
                    new MergedItemField(mockedResponses.KEY1,
                            new JSONObject(mockedResponses.FIELD1),
                            MergedItemField.NORMAL_FIELD_CODE), "test");
        } else if (i % 3 == 0) {

            validationEntry.addChangedFieldFromFormValue(
                    new MergedItemField(mockedResponses.KEY2,
                            new JSONObject(mockedResponses.FIELD2),
                            MergedItemField.NORMAL_FIELD_CODE), true);
        } else if (i % 4 == 0) {
            validationEntry.addChangedFieldFromFormValue(
                    new MergedItemField(mockedResponses.KEY1,
                            new JSONObject(mockedResponses.FIELD1),
                            MergedItemField.NORMAL_FIELD_CODE), "test");
            validationEntry.addChangedFieldFromFormValue(
                    new MergedItemField(mockedResponses.KEY2,
                            new JSONObject(mockedResponses.FIELD2),
                            MergedItemField.NORMAL_FIELD_CODE), true);
        }
        return validationEntry;
    }

    @Test
    public void testInventoryLogic() {
        mergedItemsRepository = mock(MergedItemsRepository.class);
        context = mock(Context.class);
        mergedItemViewModel = new MergedItemViewModel(mergedItemsRepository);
        StatusAwareLiveData<List<RecyclerViewItem>> value = new StatusAwareLiveData<>();
        List<RecyclerViewItem> items = new ArrayList<>();
        items.add(MergedItem.createNewEmptyItemWithBarcode("12345", context));
        value.postSuccess(items);


        Mockito.doAnswer(new Answer<StatusAwareLiveData<List<RecyclerViewItem>>>() {
            @Override
            public StatusAwareLiveData<List<RecyclerViewItem>> answer(InvocationOnMock invocation) throws Throwable {
                //StatusAwareLiveData<List<RecyclerViewItem>> mainContentRepoData = readItems(mockedResponses.getLongRoom());
                //return mainContentRepoData;
                return null;
            }
        }).when(mergedItemsRepository).fetchMainData();

        mergedItemViewModel.fetchData("ignore");

        assertEquals(100, mergedItemViewModel.getMergedItems().size());
    }

    // Copy this from the repository, mocking a network request is not documented


    private List<RecyclerViewItem> readLongItems() throws IOException {
        return readItems(mockedResponses.getLongRoom());
    }


    private List<RecyclerViewItem> readShortItems() throws IOException {
        return readItems(mockedResponses.getShortRoom());
    }

    private List<RecyclerViewItem> readItems(String roomFinal) {

        try {
            List<RecyclerViewItem> recyclerViewItems = new ArrayList<>();
            JSONObject payloadAsJson = new JSONObject(roomFinal);
            JSONArray directItems = payloadAsJson.getJSONArray("items");
            Room superRoom = new Room(payloadAsJson, 0);
            recyclerViewItems.add(superRoom);


            for (int i = 0; i < directItems.length(); i++) {
                MergedItem mergedItem = new MergedItem(directItems.getJSONObject(i));
                //totalItemsCount += mergedItem.getTimesFoundLast();
                recyclerViewItems.add(mergedItem);
                superRoom.addItemToRoom(mergedItem);
            }

            addSubItems(recyclerViewItems, payloadAsJson, superRoom, 0);
            return recyclerViewItems;
        } catch (JSONException error) {
            error.printStackTrace();
        }
        return null;
    }


    private void addSubItems(List<RecyclerViewItem> recyclerViewItems, JSONObject payloadAsJson, Room superRoom, int depth) throws JSONException {
        JSONArray subRooms = payloadAsJson.getJSONArray("subrooms");
        depth++;

        for (int i = 0; i < subRooms.length(); i++) {
            Room subRoom = new Room(subRooms.getJSONObject(i), depth);
            recyclerViewItems.add(subRoom);
            superRoom.addSubRoomToSuperRoom(subRoom);


            JSONArray subItems = subRooms.getJSONObject(i).getJSONArray("items");
            for (int j = 0; j < subItems.length(); j++) {
                MergedItem mergedItem = new MergedItem(subItems.getJSONObject(j), subRoom);
                //totalItemsCount += mergedItem.getTimesFoundLast();
                recyclerViewItems.add(mergedItem);
                subRoom.addItemToRoom(mergedItem);
            }
            addSubItems(recyclerViewItems, subRooms.getJSONObject(i), subRoom, depth);
        }
    }
}
