package com.capentory.capentory_client.models;

import androidx.annotation.NonNull;

import com.capentory.capentory_client.viewmodels.adapter.GenericDropDownAdapter;

import org.json.JSONException;
import org.json.JSONObject;

public class SerializerEntry implements GenericDropDownAdapter.DropDownEntry {
    @NonNull
    private String key, roomUrl, itemUrl, description;
    public final static String attachmentUrl="/api/addattachment/";

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

    @Override
    public String displayName() {
        return getKey();
    }

    @Override
    public String displayDescription() {
        return getDescription();
    }


}
