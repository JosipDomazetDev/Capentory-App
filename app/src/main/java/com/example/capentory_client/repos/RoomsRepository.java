package com.example.capentory_client.repos;


import android.content.Context;

import com.android.volley.Request;
import com.example.capentory_client.models.Room;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        addMainRequest(Request.Method.GET, getUrl(context, true, "api", "htlinventory"));
        launchMainRequest();
        return mainContentRepoData;
    }


    @Override
    protected void handleMainSuccessfulResponse(JSONObject payload) {
        try {
            List<Room> rooms = new ArrayList<>();
            payload = payload.getJSONObject("rooms");
            Iterator<String> roomKeys = payload.keys();

            while (roomKeys.hasNext()) {
                rooms.add(new Room(roomKeys.next(), payload));
            }

            mainContentRepoData.postSuccess(rooms);
        } catch (JSONException error) {
            mainContentRepoData.postError(error);
        }
    }


}
