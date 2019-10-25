package com.example.capentory_client.repos;


import android.content.Context;

import com.android.volley.Request;
import com.example.capentory_client.models.ActualRoom;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ActualRoomsRepository extends JsonRepository<List<ActualRoom>> {

    @Inject
    public ActualRoomsRepository(Context context) {
        super(context);
    }


    @Override
    public StatusAwareLiveData<List<ActualRoom>> fetchMainData(String... args) {
        // Fetch only once for entire application, the rooms wont change
        /*  if (actualRoomsLiveData.getValue() == null || actualRoomsLiveData.getValue().fetchMainData() == null) {*/
        addMainRequest(Request.Method.GET, getUrl(context, true, "api","inventory", "actualroom"));
        launchMainRequest();
        return mainContentRepoData;
    }


    @Override
    protected void handleMainSuccessfulResponse(JSONObject payload) {
        try {
            List<ActualRoom> actualRooms = new ArrayList<>();
            Iterator<String> keys = payload.keys();

            while (keys.hasNext()) {
                actualRooms.add(new ActualRoom(keys.next(), payload));
            }

            mainContentRepoData.postSuccess(actualRooms);
        } catch (JSONException error) {
            mainContentRepoData.postError(error);
        }
    }


}
