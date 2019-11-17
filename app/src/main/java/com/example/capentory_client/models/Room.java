package com.example.capentory_client.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.capentory_client.viewmodels.adapter.GenericDropDownAdapter;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents an Room from ralph, only the displayNumber is directly stored to allow later changes to the server
 */
public class Room implements GenericDropDownAdapter.DropDownEntry, Comparable<Room> {
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
    public String getDisplayedNumber() {
        if (displayNumber.equals("null")) return "N/A";

        return displayNumber;
    }

    public String getDisplayedRoomDescription() {
        if (displayDescriptions == null || displayDescriptions.equals("null")) return "N/A";
        return displayDescriptions;
    }

    @Override
    public String displayName() {
        return getDisplayedNumber();
    }

    @Override
    public String displayDescription() {
        return getDisplayedRoomDescription();
    }


    @Override
    public int compareTo(Room that) {
        if (this.displayNumber.compareTo(that.displayNumber) < 0) {
            return -1;
        } else if (this.displayNumber.compareTo(that.displayNumber) > 0) {
            return 1;
        }

        if (this.roomNumber.compareTo(that.roomNumber) < 0) {
            return -1;
        } else if (this.roomNumber.compareTo(that.roomNumber) > 0) {
            return 1;
        }
        if (displayDescriptions != null && that.displayDescriptions != null) {
            if (this.displayDescriptions.compareTo(that.displayDescriptions) < 0) {
                return -1;
            } else if (this.displayDescriptions.compareTo(that.displayDescriptions) > 0) {
                return 1;
            }
        }
        return 0;
    }
}
