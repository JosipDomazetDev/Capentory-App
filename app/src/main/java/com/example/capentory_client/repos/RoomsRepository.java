package com.example.capentory_client.repos;


import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.example.capentory_client.models.Room;
import com.example.capentory_client.models.Stocktaking;
import com.example.capentory_client.ui.MainActivity;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        addMainRequest(Request.Method.GET, getUrl(context, true, MainActivity.getSerializer().getRoomUrl()));
        launchMainRequest();
        return mainContentRepoData;
    }


    @Override
    protected void handleMainSuccessfulResponse(JSONObject payload) {
        try {
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


    public MutableLiveData<StatusAwareData<Boolean>> finishInventory() {
        StatusAwareLiveData<Boolean> finishSuccessful = new StatusAwareLiveData<>();

        addRequest(FINISH_REQUEST_KEY, Request.Method.POST, getUrl(context, false,
                "api", "stocktaking", String.valueOf(MainActivity.getStocktaking().getStocktakingId()), "finish/"),
                payload -> {
                    try {
                        finishSuccessful.postSuccess(payload.getBoolean("success"));
                    } catch (JSONException e) {
                        finishSuccessful.postError(e);
                    }
                }, finishSuccessful);
        launchRequestFromKey(FINISH_REQUEST_KEY, finishSuccessful);

        return finishSuccessful;
    }
}
