package com.example.capentory_client.repos;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.capentory_client.models.ActualRoom;
import com.example.capentory_client.repos.MySingleton;

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

    private ArrayList<ActualRoom> actualRooms=new ArrayList<>();
    private Context context;

    @Inject
    public RalphRepository(Context context) {
        this.context = context;
    }

    // Pretend to get data from a webservice or online source
    public MutableLiveData<List<ActualRoom>> getRooms() {
        setRooms();
        MutableLiveData<List<ActualRoom>> data = new MutableLiveData<>();
        data.setValue(actualRooms);
        return data;
}

    private void setRooms() {

        String url = "http://192.168.1.3:8000/api/actualroom?format=json";

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                JSONObject jsonObject=new JSONObject();
                jsonObject.put("room_number","11112432");

                    actualRooms.add(new ActualRoom(jsonObject));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected Void doInBackground(Void... voids) {

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray payload = response.getJSONArray("results");
                            for (int i = 0; i < payload.length(); i++) {
                                actualRooms.add(new ActualRoom(payload.getJSONObject(i)));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("network", error.getMessage() + error.getLocalizedMessage());
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
        MySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);



    }
}
