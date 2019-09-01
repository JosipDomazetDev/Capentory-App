package com.example.capentory_client.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents an ActualRoom from ralph, only the roomNumber is directly stored to allow later changes to the server
 */
public class ActualRoom {
    private String roomNumber;
    private String roomDescription;
    private JSONObject actualRoomPayload;

    public ActualRoom(JSONObject actualRoomPayload) throws JSONException {
        this.actualRoomPayload = actualRoomPayload;
        this.roomNumber = actualRoomPayload.getString("room_number");
        this.roomDescription = actualRoomPayload.getString("description");
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getRoomDescription() {
        return roomDescription;
    }


}
