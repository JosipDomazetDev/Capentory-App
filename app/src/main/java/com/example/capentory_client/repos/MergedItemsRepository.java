package com.example.capentory_client.repos;


import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.example.capentory_client.models.MergedItem;
import com.example.capentory_client.ui.MainActivity;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MergedItemsRepository extends NetworkRepository<List<MergedItem>> {
    private String currentRoomString;
    private final String VALIDATION_REQUEST_KEY = "request_validation";

    // StatusAwareLiveData<Boolean> validateSuccessful = new StatusAwareLiveData<>();

    @Inject
    public MergedItemsRepository(Context context) {
        super(context);
    }


    @Override
    public StatusAwareLiveData<List<MergedItem>> fetchMainData(String... args) {
        if (args.length != 1)
            throw new IllegalArgumentException("MergedItemRepository only needs the currentRoom as argument!");

        this.currentRoomString = args[0];
        Map<String, String> paras = new HashMap<>();
        paras.put("stocktaking_id", String.valueOf(MainActivity.getStocktaking().getStocktakingId()));
        addMainRequest(Request.Method.GET, getUrl(context, true, new String[]{MainActivity.getSerializer().getRoomUrl(), currentRoomString + "/"}, paras));
        launchMainRequest();

        return mainContentRepoData;
    }


    @Override
    protected void handleMainSuccessfulResponse(String stringPayload) {
        try {
            List<MergedItem> mergedItems = new ArrayList<>();
            JSONArray payload = new JSONObject(stringPayload).getJSONArray("items");

            for (int i = 0; i < payload.length(); i++) {
                mergedItems.add(new MergedItem(payload.getJSONObject(i)));
            }

            mainContentRepoData.postSuccess(mergedItems);
        } catch (JSONException error) {
            mainContentRepoData.postError(error);
        }
    }


    public StatusAwareLiveData<Boolean> sendValidationEntriesToServer(JSONObject validationEntriesAsJson) {
        StatusAwareLiveData<Boolean> validateSuccessful = new StatusAwareLiveData<>();

        addRequestWithContent(VALIDATION_REQUEST_KEY, Request.Method.POST, getUrl(context, true, MainActivity.getSerializer().getRoomUrl(), currentRoomString + "/"), validationEntriesAsJson,
                payload -> validateSuccessful.postSuccess(true), validateSuccessful);
        launchRequestFromKey(VALIDATION_REQUEST_KEY, validateSuccessful);

        return validateSuccessful;
    }


}
