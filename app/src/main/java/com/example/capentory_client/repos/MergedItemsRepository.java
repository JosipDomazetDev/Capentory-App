package com.example.capentory_client.repos;


import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.capentory_client.models.MergedItem;
import com.example.capentory_client.models.Room;
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
public class MergedItemsRepository extends JsonRepository<List<MergedItem>> {
    private String currentRoomString;
    private StatusAwareLiveData<Boolean> validateSuccessful = new StatusAwareLiveData<>();
    private final String VALIDATION_REQUEST_KEY = "request_validation";


    @Inject
    public MergedItemsRepository(Context context) {
        super(context);
    }


    @Override
    public StatusAwareLiveData<List<MergedItem>> fetchMainData(String... args) {
        if (args.length != 1)
            throw new IllegalArgumentException("MergedItemRepository only needs the currentRoom as argument!");

        this.currentRoomString = args[0];
        addMainRequest(Request.Method.GET, getUrl(context, true, "api", "htlinventoryrooms", currentRoomString));
        launchMainRequest();

        return mainContentRepoData;
    }


    @Override
    protected void handleMainSuccessfulResponse(JSONObject payload) {
        try {
            List<MergedItem> mergedItems = new ArrayList<>();
            payload = payload.getJSONObject("items");
            Iterator<String> itemKeys = payload.keys();


            while (itemKeys.hasNext()) {
                mergedItems.add(new MergedItem(currentRoomString,itemKeys.next(), payload));
            }

            mainContentRepoData.postSuccess(mergedItems);
        } catch (JSONException error) {
            mainContentRepoData.postError(error);
        }
    }


    public StatusAwareLiveData<Boolean> sendValidationEntriesToServer(JSONArray validationEntriesAsJson) {
        try {
            addRequest(VALIDATION_REQUEST_KEY, Request.Method.POST, getUrl(context, true, "api", "htlinventoryrooms"), validationEntriesAsJson,
                    payload -> validateSuccessful.postSuccess(true));
            validateSuccessful.postFetching();
            launchRequestFromKey(VALIDATION_REQUEST_KEY);
        } catch (JSONException e) {
            validateSuccessful.postError(e);
        }
        return validateSuccessful;
    }


}
