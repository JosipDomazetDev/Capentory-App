package com.example.capentory_client.models;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class SerializerEntry {
    @NonNull
    private String key, roomUrl, itemUrl, description;


    public SerializerEntry(@NonNull String key, JSONObject payload) throws JSONException {
        this.key = key;
        JSONObject jsonObject = payload.getJSONObject(key);
        this.roomUrl = jsonObject.getString("roomUrl");
        this.itemUrl = jsonObject.getString("itemUrl");
        this.description = jsonObject.getString("description");
    }

    @NonNull
    public String getRoomUrl() {
        return roomUrl;
    }

    @NonNull
    public String getItemUrl() {
        return itemUrl;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    @NonNull
    public String getKey() {
        return key;
    }
}
