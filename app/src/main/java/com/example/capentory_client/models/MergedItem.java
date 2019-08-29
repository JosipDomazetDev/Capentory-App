package com.example.capentory_client.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents an MergedItem from ralph, only the desc and sap_item_number (scancode) is directly stored to allow later changes to the server
 */
public class MergedItem {
    private String currentRoomNumber;


    private JSONObject mergedItemJSONPayload;

    private int itemID;
    private String anlageNummer;
    private String description;


    public MergedItem(String currentRoomNumber, JSONObject mergedItemJSONPayload) throws JSONException {
        this.currentRoomNumber = currentRoomNumber;
        this.mergedItemJSONPayload = mergedItemJSONPayload;

        this.itemID = mergedItemJSONPayload.getInt("item_id");
        JSONObject sapItem = mergedItemJSONPayload.getJSONObject("sap_item");
        this.anlageNummer = sapItem.getString("anlage");
        this.description = sapItem.getString("desc");
    }

    public String getCurrentRoomNumber() {
        return currentRoomNumber;
    }

    public JSONObject getMergedItemJSONPayload() {
        return mergedItemJSONPayload;
    }

    public String getAnlageNummer() {
        return anlageNummer;
    }

    public String getDescription() {
        return description;
    }

}
