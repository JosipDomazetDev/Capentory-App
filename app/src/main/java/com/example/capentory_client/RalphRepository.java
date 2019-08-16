package com.example.capentory_client;


import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import androidx.lifecycle.MutableLiveData;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RalphRepository {


    private static RalphRepository instance;
    private ArrayList<JSONObject> actualRooms = new ArrayList<>();

    public static RalphRepository getInstance() {
        if (instance == null) {
            instance = new RalphRepository();
        }
        return instance;
    }


    // Pretend to get data from a webservice or online source
    public MutableLiveData<List<JSONObject>> getRooms() {
        setRooms();
        MutableLiveData<List<JSONObject>> data = new MutableLiveData<>();
        data.setValue(actualRooms);
        return data;
    }

    private void setRooms() {


        String url = "http://192.168.49.123:8000/api/actualitem/1/?format=json";


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                    }

                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("xxxxxxxxxxxxxxxx", error.getMessage() + error.getLocalizedMessage());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
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
        MySingleton.getInstance(get).addToRequestQueue(jsonObjectRequest);


        actualRooms.add(student1);

    }
}
