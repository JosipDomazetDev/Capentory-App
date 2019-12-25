package com.example.capentory_client.repos;


import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.capentory_client.models.Room;
import com.example.capentory_client.models.Stocktaking;
import com.example.capentory_client.ui.MainActivity;
import com.example.capentory_client.ui.errorhandling.CustomException;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RoomsRepository extends NetworkRepository<List<Room>> {

    @Inject
    public RoomsRepository(Context context) {
        super(context);
    }


    @Override
    public StatusAwareLiveData<List<Room>> fetchMainData(String... args) {
        // Fetch only once for entire application, the rooms wont change
        /*  if (actualRoomsLiveData.getValue() == null || actualRoomsLiveData.getValue().fetchMainData() == null) {*/
        Map<String, String> paras = new HashMap<>();
        paras.put("stocktaking_id", String.valueOf(MainActivity.getStocktaking(context).getStocktakingId()));

        addMainRequest(Request.Method.GET, getUrl(context, true,
                new String[]{MainActivity.getSerializer(context).getRoomUrl()}, paras));
        launchMainRequest();
        return mainContentRepoData;
    }


    @Override
    protected void handleMainSuccessfulResponse(String stringPayload) {
        try {

            JSONArray payload = new JSONObject(stringPayload).getJSONArray("rooms");
            List<Room> rooms = new ArrayList<>();

            for (int i = 0; i < payload.length(); i++) {
                rooms.add(new Room(payload.getJSONObject(i)));
            }

            Collections.sort(rooms);
            mainContentRepoData.postSuccess(rooms);
        } catch (JSONException error) {
            try {
                String errors = new JSONObject(stringPayload).getString("errors");
                mainContentRepoData.postError(new CustomException(errors));
            } catch (JSONException e) {
                mainContentRepoData.postError(error);
            }
        }
    }


    /*
    If for some reason you want to completely end a Inventory you could do it with this method
    public MutableLiveData<StatusAwareData<Boolean>> finishInventory() {
        StatusAwareLiveData<Boolean> finishSuccessful = new StatusAwareLiveData<>();

        addRequest(FINISH_REQUEST_KEY, Request.Method.POST, getUrl(context, false,
                "api", "stocktaking", String.valueOf(MainActivity.getStocktaking().getStocktakingId()), "finish/"),
                stringPayload -> {
                    try {
                        JSONObject payload = new JSONObject(stringPayload);
                        finishSuccessful.postSuccess(payload.getBoolean("success"));
                    } catch (JSONException e) {
                        finishSuccessful.postError(e);
                    }
                }, finishSuccessful);
        launchRequestFromKey(FINISH_REQUEST_KEY, finishSuccessful);

        return finishSuccessful;
    }*/
}
