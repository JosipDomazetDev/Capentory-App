package com.example.capentory_client.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *  Represents an ActualRoom from ralph, only the roomNumber is directly stored to allow later changes to the server
 */
public class ActualRoom {
    private String roomNumber;
    private JSONObject actualRoomPayload;

    public ActualRoom(JSONObject actualRoomPayload) throws JSONException {
        this.actualRoomPayload = actualRoomPayload;
        this.roomNumber = actualRoomPayload.getString("room_number");
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public JSONObject getActualRoomPayload() {
        return actualRoomPayload;
    }
}