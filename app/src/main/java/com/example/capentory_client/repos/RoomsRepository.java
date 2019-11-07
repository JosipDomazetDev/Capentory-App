package com.example.capentory_client.repos;


import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.capentory_client.models.Room;
import com.example.capentory_client.models.Stocktaking;
import com.example.capentory_client.ui.MainActivity;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

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
public class RoomsRepository extends NetworkRepository<List<Room>> {
    private final String FINISH_REQUEST_KEY = "request_finish";

    @Inject
    public RoomsRepository(Context context) {
        super(context);
    }


    @Override
    public StatusAwareLiveData<List<Room>> fetchMainData(String... args) {
        // Fetch only once for entire application, the rooms wont change
        /*  if (actualRoomsLiveData.getValue() == null || actualRoomsLiveData.getValue().fetchMainData() == null) {*/
        Map<String, String> paras = new HashMap<>();
        paras.put("stocktaking_id", String.valueOf(MainActivity.getStocktaking().getStocktakingId()));

        addMainRequest(Request.Method.GET, getUrl(context, true,
                new String[]{MainActivity.getSerializer().getRoomUrl()}, paras));
        launchMainRequest();
        return mainContentRepoData;
    }


    @Override
    protected void handleMainSuccessfulResponse(String stringPayload) {
        try {
            JSONObject payload = new JSONObject(stringPayload);
            List<Room> rooms = new ArrayList<>();
            Iterator<String> roomKeys = payload.keys();

            while (roomKeys.hasNext()) {
                rooms.add(new Room(roomKeys.next(), payload));
            }

            mainContentRepoData.postSuccess(rooms);
        } catch (JSONException error) {
            mainContentRepoData.postError(error);
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
