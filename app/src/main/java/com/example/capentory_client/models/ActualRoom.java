package com.example.capentory_client.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents an ActualRoom from ralph, only the roomNumber is directly stored to allow later changes to the server
 */
public class ActualRoom {
    @NonNull
    private String roomNumber;
    @Nullable
    private String roomDescription;

    public ActualRoom(@NonNull String key, JSONObject actualRoomPayload) throws JSONException {
        this.roomNumber = key;
        this.roomDescription = actualRoomPayload.getString(key);
    }

    @NonNull
    public String getRoomNumber() {
        return roomNumber;
    }

    @Nullable
    public String getRoomDescription() {
        return roomDescription;
    }

    public String getDisplayedRoomDescription() {
        if (roomDescription == null) return "N/A";
        return roomDescription;
    }
}
