package com.example.capentory_client.repos;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.capentory_client.models.ActualRoom;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ActualRoomsRepository {
    private StatusAwareLiveData<List<ActualRoom>> actualRoomsLiveData = new StatusAwareLiveData<>();
    private Context context;

    @Inject
    public ActualRoomsRepository(Context context) {
        this.context = context;
    }


    // Pretend to get data from a webservice or online source
    public StatusAwareLiveData<List<ActualRoom>> getRooms() {
        setRooms();
        return actualRoomsLiveData;
    }

    public void setRooms() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        String server_ip = sharedPreferences.getString("server_ip", "capentory.hostname") + ":";
        String server_port = sharedPreferences.getString("server_port", "80");
        String url = "http://" + server_ip + server_port + "/api/inventory/actualroom/?format=json";

        //url = "http://192.168.1.2:8000/api/actualroom/171/?format=json";
        //url = "http://192.168.1.2:8000/api/inventory/actualroom/LAN%209/?format=json";
        //url = "http://192.168.1.2:8000/api/saproom/?format=json";


        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, payload -> {
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

        MySingleton.getInstance(context).
                addToRequestQueue(jsonObjectRequest);


        actualRoomsLiveData.postFetching();
    }

    public void resetExceptionState() {
    }
}
