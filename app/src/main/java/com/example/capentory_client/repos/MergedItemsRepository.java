package com.example.capentory_client.repos;


import android.content.Context;

import com.android.volley.Request;
import com.example.capentory_client.models.MergedItem;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MergedItemsRepository extends Repository {
    private StatusAwareLiveData<List<MergedItem>> mergedItemsLiveData = new StatusAwareLiveData<>();
    private String currentRoomString;

    @Inject
    public MergedItemsRepository(Context context) {
        super(context);
    }


    public StatusAwareLiveData<List<MergedItem>> getMergedItems(String currentRoomString) {
        this.currentRoomString = currentRoomString;
        initRequest(Request.Method.GET, getUrl(context, true, "actualroom", currentRoomString));
        setData();
        retriesCounter = 0;

        return mergedItemsLiveData;
    }

    @Override
    protected void setData() {
        mergedItemsLiveData.postFetching();
        launchRequest();
    }

    @Override
    protected void handleNetworkResponse(JSONObject payload) {
        try {
            JSONArray allItems = payload.optJSONArray("all_items");
            List<MergedItem> mergedItems = new ArrayList<>();

            for (int i = 0; i < allItems.length(); i++) {
                JSONObject jsonItem = allItems.getJSONObject(i);
                mergedItems.add(new MergedItem(currentRoomString, jsonItem));
            }

            mergedItemsLiveData.postSuccess(mergedItems);
        } catch (JSONException error) {
            mergedItemsLiveData.postError(error);
        }
    }

    @Override
    protected void handleErrorResponse(Exception error) {
        mergedItemsLiveData.postError(error);
    }


}
