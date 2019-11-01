package com.example.capentory_client.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents an Room from ralph, only the displayNumber is directly stored to allow later changes to the server
 */
public class Room {
    @NonNull
    private String displayNumber, roomNumber;

    @Nullable
    private String displayDescriptions;

    public Room(@NonNull String key, JSONObject payload) throws JSONException {
        this.roomNumber = key;
        payload = payload.getJSONObject(key);
        this.displayNumber = payload.getString("displayName");
        this.displayDescriptions = payload.getString("displayDescription");
    }


    @NonNull
    public String getRoomNumber() {
        return roomNumber;
    }

    @NonNull
    public String getDisplayNumber() {
        return displayNumber;
    }

    @Nullable
    public String getDisplayDescriptions() {
        return displayDescriptions;
    }

    public String getDisplayedRoomDescription() {
        if (displayDescriptions == null) return "N/A";
        return displayDescriptions;
    }
}
