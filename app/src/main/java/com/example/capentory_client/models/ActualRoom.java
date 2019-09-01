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

    public ActualRoom(JSONObject actualRoomPayload) throws JSONException {
        this.roomNumber = actualRoomPayload.getString("room_number");
        if (roomNumber == null)
            throw new JSONException("Der PK eines Raumes darf nicht null sein!");

        this.roomDescription = actualRoomPayload.optString("description", null);
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
