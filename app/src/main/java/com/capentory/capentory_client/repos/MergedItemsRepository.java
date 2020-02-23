package com.capentory.capentory_client.repos;


import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.capentory.capentory_client.models.MergedItem;
import com.capentory.capentory_client.models.RecyclerViewItem;
import com.capentory.capentory_client.models.Room;
import com.capentory.capentory_client.ui.MainActivity;
import com.capentory.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MergedItemsRepository extends NetworkRepository<List<RecyclerViewItem>> {
    private String currentRoomString;
    private final String VALIDATION_REQUEST_KEY = "request_validation";
    private int totalItemsCount;

    // StatusAwareLiveData<Boolean> validateSuccessful = new StatusAwareLiveData<>();

    @Inject
    public MergedItemsRepository(Context context) {
        super(context);
    }


    @Override
    public StatusAwareLiveData<List<RecyclerViewItem>> fetchMainData(String... args) {
        if (args.length != 1)
            throw new IllegalArgumentException("MergedItemRepository only needs the currentRoom as argument!");

        this.currentRoomString = args[0];
        Map<String, String> paras = new HashMap<>();
        paras.put("stocktaking_id", String.valueOf(MainActivity.getStocktaking().getStocktakingId()));

        totalItemsCount=0;
        addMainRequest(Request.Method.GET, getUrl(context, true, new String[]{MainActivity.getSerializer().getRoomUrl(), currentRoomString + "/"}, paras));
        launchMainRequest();

        return mainContentRepoData;
    }

    public int getAmountOfItemsLeft(List<RecyclerViewItem> recyclerViewItems) {
        int c = 0;
        for (RecyclerViewItem recyclerviewItem : recyclerViewItems) {
            if (recyclerviewItem instanceof MergedItem) {
                MergedItem mergedItem = (MergedItem) recyclerviewItem;
                c += mergedItem.getTimesFoundLast() - mergedItem.getTimesFoundCurrent();
            }
        }
        return c;
    }

    @Override
    protected void handleMainSuccessfulResponse(String stringPayload) {
        try {
            List<RecyclerViewItem> recyclerViewItems = new ArrayList<>();
            JSONObject payloadAsJson = new JSONObject(stringPayload);
            JSONArray directItems = payloadAsJson.getJSONArray("items");
            Room superRoom = new Room(payloadAsJson, 0);
            recyclerViewItems.add(superRoom);


            for (int i = 0; i < directItems.length(); i++) {
                MergedItem mergedItem = new MergedItem(directItems.getJSONObject(i));
                totalItemsCount += mergedItem.getTimesFoundLast();
                recyclerViewItems.add(mergedItem);
                superRoom.addItemToRoom(mergedItem);
            }

            addSubItems(recyclerViewItems, payloadAsJson, superRoom, 0);
            mainContentRepoData.postSuccess(recyclerViewItems);
        } catch (JSONException error) {
            mainContentRepoData.postError(error);
        }
    }

    public int getTotalItemsCount() {
        return totalItemsCount;
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
                totalItemsCount += mergedItem.getTimesFoundLast();
                recyclerViewItems.add(mergedItem);
                subRoom.addItemToRoom(mergedItem);
            }
            addSubItems(recyclerViewItems, subRooms.getJSONObject(i), subRoom, depth);
        }
    }


    public StatusAwareLiveData<Boolean> sendValidationEntriesToServer(JSONObject validationEntriesAsJson) {
        StatusAwareLiveData<Boolean> validateSuccessful = new StatusAwareLiveData<>();

        addRequestWithContent(VALIDATION_REQUEST_KEY, Request.Method.POST, getUrl(context, true, MainActivity.getSerializer().getRoomUrl(), currentRoomString + "/"), validationEntriesAsJson,
                payload -> {

                    Log.e("XXX", payload);
                    validateSuccessful.postSuccess(true);
                }, validateSuccessful);
        launchRequestFromKey(VALIDATION_REQUEST_KEY, validateSuccessful);

        return validateSuccessful;
    }


}
