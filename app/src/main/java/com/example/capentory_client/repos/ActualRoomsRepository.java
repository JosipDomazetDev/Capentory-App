package com.example.capentory_client.repos;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.capentory_client.models.ActualRoom;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

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
public class ActualRoomsRepository extends Repository {
    private StatusAwareLiveData<List<ActualRoom>> actualRoomsLiveData = new StatusAwareLiveData<>();

    @Inject
    public ActualRoomsRepository(Context context) {
        super(context);
    }


    public StatusAwareLiveData<List<ActualRoom>> getRooms() {
        // Fetch only once for entire application, the rooms wont change
        /*  if (actualRoomsLiveData.getValue() == null || actualRoomsLiveData.getValue().fetchData() == null) {*/
        initRequest(Request.Method.GET, getUrl(context, true, "inventory", "actualroom"));
        setData();
        return actualRoomsLiveData;
    }

    @Override
    protected void setData() {
        actualRoomsLiveData.postFetching();
        launchRequest();
    }

    @Override
    protected void handleNetworkResponse(JSONObject payload) {
        try {
            List<ActualRoom> actualRooms = new ArrayList<>();
            Iterator<String> keys = payload.keys();

            while (keys.hasNext()) {
                actualRooms.add(new ActualRoom(keys.next(), payload));
            }

            actualRoomsLiveData.postSuccess(actualRooms);
        } catch (JSONException error) {
            actualRoomsLiveData.postError(error);
        }
    }

    @Override
    protected void handleErrorResponse(Exception error) {
        actualRoomsLiveData.postError(error);
    }


}
