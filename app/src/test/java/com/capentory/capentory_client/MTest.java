package com.capentory.capentory_client;

import android.content.Context;

import com.capentory.capentory_client.models.MergedItem;
import com.capentory.capentory_client.models.RecyclerViewItem;
import com.capentory.capentory_client.models.Room;
import com.capentory.capentory_client.models.ValidationEntry;
import com.capentory.capentory_client.repos.MergedItemsRepository;
import com.capentory.capentory_client.repos.customrequest.RobustJsonRequestExecutioner;
import com.capentory.capentory_client.viewmodels.MergedItemViewModel;
import com.capentory.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class MTest {
    MergedItemViewModel mergedItemViewModel;

    @Mock
    MergedItemsRepository mergedItemsRepository;

    Context context = mock(Context.class);

    @Test
    public void testInventoryLogic() {
        mergedItemsRepository = mock(MergedItemsRepository.class);
        mergedItemViewModel = new MergedItemViewModel(mergedItemsRepository);
        StatusAwareLiveData<List<RecyclerViewItem>> value = new StatusAwareLiveData<>();
        List<RecyclerViewItem> items = new ArrayList<>();
        items.add(MergedItem.createNewEmptyItemWithBarcode("12345", context));
        value.postSuccess(items);

        when(mergedItemsRepository.fetchMainData()).thenReturn(value);
        mergedItemViewModel.fetchData("ignore");


        assertEquals(100, mergedItemViewModel.getMergedItems().size());
    }

    // Copy this from the repository, mocking a network request is not documented
    private StatusAwareLiveData<List<RecyclerViewItem>> readItems() {
        StatusAwareLiveData<List<RecyclerViewItem>> mainContentRepoData = new StatusAwareLiveData<>();

        try {
            List<RecyclerViewItem> recyclerViewItems = new ArrayList<>();
            JSONObject payloadAsJson = new JSONObject(DO_NOT_COMMIT_FINALS.getLongRoom());
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
            mainContentRepoData.postSuccess(recyclerViewItems);
        } catch (JSONException | IOException error) {
            mainContentRepoData.postError(error);
        }
        return mainContentRepoData;
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
