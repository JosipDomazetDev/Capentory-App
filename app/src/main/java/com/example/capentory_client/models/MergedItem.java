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
        this.description = mergedItemJSONPayload.getString("desc");

        JSONObject sapItem = mergedItemJSONPayload.optJSONObject("sap_item");
        if (sapItem == null) {
            this.anlageNummer = null;
            this.assetSubnumber = null;
        } else {
            this.anlageNummer = sapItem.getString("anlage");
            this.assetSubnumber = sapItem.getString("asset_subnumber");
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
     * Compare the scannedBarcode
     *
     * @param scannedBarcode to compare
     * @return if matches
     */
    public boolean equalsBarcode(String scannedBarcode) {
        if (anlageNummer == null) return false;

        // 12340000 = 12340000
        boolean normalCond = Objects.equals(getBarcode(), scannedBarcode);
        if (normalCond) return true;

        if (anlageNummer.length() == scannedBarcode.length()) {
            // 1234 = 1234 (scannedBarcode maybe doesn't include zeros => only compare to anlage)
            return Objects.equals(anlageNummer, scannedBarcode);

        } else if (anlageNummer.length() < scannedBarcode.length()) {
            // 1234 = 1234|0000| (scannedBarcode includes zeros but local barcode doesn't (=> cut and compare to anlage))
            return Objects.equals(anlageNummer, scannedBarcode.substring(0, anlageNummer.length()));
        }

        // If the scannedBarcode however is shorther than even anlage every hope is lost
        return false;
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
