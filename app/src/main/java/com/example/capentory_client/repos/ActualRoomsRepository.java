package com.example.capentory_client.repos;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.capentory_client.models.ActualRoom;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
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
        if (actualRoomsLiveData.getValue() == null) {
            setRooms();
        }

        return actualRoomsLiveData;
    }

    public void setRooms() {
        actualRoomsLiveData.postFetching();
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest
                (Request.Method.GET, getUrl(true, "inventory", "actualroom"), null, payload -> {
                    try {
                        List<ActualRoom> actualRooms = new ArrayList<>();
                        for (int i = 0; i < payload.length(); i++) {
                            actualRooms.add(new ActualRoom(payload.getJSONObject(i)));
                        }
                        actualRoomsLiveData.postSuccess(actualRooms);
                    } catch (JSONException error) {
                        actualRoomsLiveData.postError(error);
                    }
                }, error -> {
                    actualRoomsLiveData.postError(error);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                String credentials = "ralph" + ":" + "ralph";
                String auth = "Basic "
                        + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", auth);
                return headers;
            }
        };

        NetworkSingleton.getInstance(context).
                addToRequestQueue(jsonObjectRequest);


        actualRoomsLiveData.postFetching();
    }
}
