package com.example.capentory_client.repos;

import android.content.Context;

import com.android.volley.Request;
import com.example.capentory_client.models.SerializerEntry;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StocktakingRepository extends JsonRepository<List<SerializerEntry>> {
    private StatusAwareLiveData<Boolean> postStocktakingSuccessful = new StatusAwareLiveData<>();
    private final String POST_STOCKTAKING_REQUEST_KEY = "request_post_stocktaking";


    @Inject
    public StocktakingRepository(Context context) {
        super(context);
    }

    @Override
    protected void handleMainSuccessfulResponse(JSONObject payload) {
        try {
            List<SerializerEntry> serializerEntries = new ArrayList<>();
            Iterator<String> keys = payload.keys();

            while (keys.hasNext()) {
                serializerEntries.add(new SerializerEntry(keys.next(), payload));
            }

            mainContentRepoData.postSuccess(serializerEntries);
        } catch (JSONException error) {
            mainContentRepoData.postError(error);
        }
    }

    @Override
    public StatusAwareLiveData<List<SerializerEntry>> fetchMainData(String... args) {
        addMainRequest(Request.Method.GET, getUrl(context, true, "api", "inventoryserializers"));
        launchMainRequest();
        return mainContentRepoData;
    }


    public StatusAwareLiveData<Boolean> postStocktaking(String name) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", name);

            addRequest(POST_STOCKTAKING_REQUEST_KEY, Request.Method.POST, getUrl(context, true, "api", "htlinventoryrooms"), jsonObject,
                    payload -> postStocktakingSuccessful.postSuccess(true));
            postStocktakingSuccessful.postFetching();
            launchRequestFromKey(POST_STOCKTAKING_REQUEST_KEY);
        } catch (JSONException e) {
            postStocktakingSuccessful.postError(e);
        }
        return postStocktakingSuccessful;
    }


}
