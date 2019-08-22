package com.example.capentory_client.repos;


import android.content.Context;
import android.util.Base64;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.capentory_client.models.ActualRoom;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RalphRepository {

    private MutableLiveData<List<ActualRoom>> actualRoomsLiveData = new MutableLiveData<>();
    private MutableLiveData<Exception> exception = new MutableLiveData<>();

    private Context context;


    @Inject
    public RalphRepository(Context context) {
        this.context = context;
    }

    public MutableLiveData<Exception> getException() {
        return exception;
    }

    // Pretend to get data from a webservice or online source
    public MutableLiveData<List<ActualRoom>> getRooms() {
        setRooms();
        return actualRoomsLiveData;
    }

    public void setRooms() {


        String url = "http://192.168.1.3:8000/api/actualroom?format=json";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.e("network", "SUCCESSS");

                            List<ActualRoom> actualRooms = new ArrayList<>();

                            JSONArray payload = response.getJSONArray("results");
                            for (int i = 0; i < payload.length(); i++) {
                                actualRooms.add(new ActualRoom(payload.getJSONObject(i)));
                            }
                            actualRoomsLiveData.setValue(actualRooms);

                            Log.e("network", "Filled");

                        } catch (JSONException error) {
                            exception.setValue(error);
                        }
                    }

                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        exception.setValue(error);
                        Log.e("network", error.getMessage() + error.getLocalizedMessage());
                    }
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

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);

    }

    public void resetExceptionState() {
        exception.setValue(null);
    }
}
