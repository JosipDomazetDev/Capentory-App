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
public class MergedItemsRepository extends Repository<List<MergedItem>> {
    private String currentRoomString;

    @Inject
    public MergedItemsRepository(Context context) {
        super(context);
    }


    @Override
    public StatusAwareLiveData<List<MergedItem>> getData(String... args) {
        if (args.length != 1)
            throw new IllegalArgumentException("MergedItemRepository only needs the currentRoom as argument!");

        this.currentRoomString = args[0];
        initRequest(Request.Method.GET, getUrl(context, true, "actualroom", currentRoomString));
        fetchData();
        return statusAwareRepoLiveData;
    }


    @Override
    protected void handleSuccessfulNetworkResponse(JSONObject payload) {
        try {
            JSONArray allItems = payload.optJSONArray("all_items");
            List<MergedItem> mergedItems = new ArrayList<>();

            for (int i = 0; i < allItems.length(); i++) {
                JSONObject jsonItem = allItems.getJSONObject(i);
                mergedItems.add(new MergedItem(currentRoomString, jsonItem));
            }

            statusAwareRepoLiveData.postSuccess(mergedItems);
        } catch (JSONException error) {
            statusAwareRepoLiveData.postError(error);
        }
    }


}
