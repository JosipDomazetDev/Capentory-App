package com.example.capentory_client.models;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Represents an MergedItem from ralph, only the desc and sap_item_number (scancode) is directly stored to allow later changes to the server
 */
public class MergedItem {
    @NonNull
    private String currentRoomNumber;
    @NonNull
    private JSONObject mergedItemJSONPayload;
    @Nullable
    private String anlageNummer, assetSubnumber, description;

    public MergedItem(@NonNull String currentRoomNumber, JSONObject mergedItemJSONPayload) throws JSONException {
        this.currentRoomNumber = currentRoomNumber;
        this.mergedItemJSONPayload = mergedItemJSONPayload;
        this.description = mergedItemJSONPayload.optString("desc", null);

        JSONObject sapItem = mergedItemJSONPayload.optJSONObject("sap_item");
        if (sapItem == null) {
            this.anlageNummer = null;
            this.assetSubnumber = null;
        } else {
            this.anlageNummer = sapItem.optString("anlage", null);
            this.assetSubnumber = sapItem.optString("asset_subnumber", null);
        }
    }

    @NonNull
    public String getCurrentRoomNumber() {
        return currentRoomNumber;
    }

    @NonNull
    public JSONObject getMergedItemJSONPayload() {
        return mergedItemJSONPayload;
    }

    @Nullable
    public String getAnlageNummer() {
        return anlageNummer;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    @Nullable
    public String getBarcode() {
        if (anlageNummer == null) return null;
        StringBuilder barcodeBuilder = new StringBuilder(anlageNummer);

        if (assetSubnumber == null || Objects.equals(assetSubnumber, "null"))
            barcodeBuilder.append("");
        else barcodeBuilder.append(assetSubnumber);

        return barcodeBuilder.toString();
    }

    /**
     * Compare the barcode (consists out of two parts), if no match, just check the anlage itself
     *
     * @param barcode to compare
     * @return if matches
     */
    public boolean equalsBarcode(String barcode) {
        return Objects.equals(getBarcode(), barcode) || Objects.equals(getAnlageNummer(), barcode);
    }

    public boolean applySearchBarFilter(@NonNull String filter) {
        filter = filter.toLowerCase().trim();
        return getDisplayedAnlageNummer().toLowerCase().trim().contains(filter) || getDisplayedDescription().toLowerCase().trim().contains(filter);
    }

    public String getDisplayedAnlageNummer() {
        if (anlageNummer == null) return "N/A";
        return anlageNummer;
    }

    public String getDisplayedDescription() {
        if (description == null) return "N/A";
        return description;
    }
}
