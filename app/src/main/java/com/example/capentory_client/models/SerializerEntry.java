package com.example.capentory_client.models;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class SerializerEntry {
    @NonNull
    private String key, url, description;


    public SerializerEntry(@NonNull String key, JSONObject payload) throws JSONException {
        this.key = key;
        JSONObject jsonObject = payload.getJSONObject(key);
        this.url = jsonObject.getString("url");
        this.description = jsonObject.getString("description");
    }

    @NonNull
    public String getUrl() {
        return url;
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
