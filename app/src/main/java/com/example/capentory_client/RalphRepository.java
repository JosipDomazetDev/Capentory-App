package com.example.capentory_client;


import androidx.lifecycle.MutableLiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


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

        JSONObject student1 = new JSONObject();
        try {
            student1.put("id", "3");
            student1.put("name", "NAME OF STUDENT");
            student1.put("year", "3rd");
            student1.put("curriculum", "Arts");
            student1.put("birthday", "5/5/1993");

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        actualRooms.add(student1);

    }
}
