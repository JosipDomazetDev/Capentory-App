package com.example.capentory_client.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *  Represents an MergedItem from ralph, only the desc and sap_item_number (scancode) is directly stored to allow later changes to the server
 */
public class MergedItem {
    private String roomNumber;
    private JSONObject actualRoomPayload;

    public MergedItem(JSONObject actualRoomPayload) throws JSONException {
        this.actualRoomPayload = actualRoomPayload;
        this.roomNumber = actualRoomPayload.getString("room_number");
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getRoomDescription() throws JSONException {
        return (String) actualRoomPayload.get("description");
    }

    public JSONObject getActualRoomPayload() {
        return actualRoomPayload;
    }



}
