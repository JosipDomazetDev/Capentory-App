package com.example.capentory_client.models;

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

        this.itemID = mergedItemJSONPayload.optInt("item_ID", -1);
        this.description = mergedItemJSONPayload.optString("desc", "N/A");
        JSONObject sapItem = mergedItemJSONPayload.optJSONObject("sap_item");
        if (sapItem == null) {
            this.anlageNummer = "Nicht mit SAP verlinkt!";
        } else
            this.anlageNummer = sapItem.optString("anlage", "N/A");
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

    public boolean equalsBarcode(String barcode) {
        StringBuilder barcodeBuilder = new StringBuilder(barcode);
       /* for (int i = 0; i < anlageNummer.length() - barcode.length(); i++) {
            barcodeBuilder.append("0");
        }*/

        return anlageNummer.equals(barcodeBuilder.toString());
    }
}
