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
    public static final String ROOM_JSON_KEY = "room";
    @NonNull
    private String pkItemId, currentRoomNumber;
    @NonNull
    private JSONObject itemAsJson, fields;
    @Nullable
    private String barcode, displayName;


    // Standard
    public MergedItem(@NonNull String currentRoomString, @NonNull String pkItemId, @NonNull JSONObject payload) throws JSONException {
        this.currentRoomNumber = currentRoomString;
        this.pkItemId = pkItemId;
        extractFromJson(pkItemId, payload);
    }

    // For scanned known item
    public MergedItem(@NonNull JSONObject payload, @NonNull String pkItemId, @NonNull String barcode) throws JSONException {
        this.pkItemId = pkItemId;
        extractFromJson(pkItemId, payload);
    }

    private MergedItem(@NonNull String currentRoomNumber, @NonNull String pkItemId, @NonNull String displayName) {
        this.currentRoomNumber = currentRoomNumber;
        this.pkItemId = pkItemId;
        this.displayName = displayName;
        itemAsJson = new JSONObject();
        fields = new JSONObject();
    }

    private MergedItem(@NonNull String currentRoomNumber, @NonNull String pkItemId) {
        this.currentRoomNumber = currentRoomNumber;
        this.pkItemId = pkItemId;
    }

    public MergedItem(@NonNull String barcode) {
        this.pkItemId = "-1";
        this.displayName = "Item befindet sich nicht in der Datenbank!";
        this.barcode = barcode;

        itemAsJson = new JSONObject();
        fields = new JSONObject();
    }


    private void extractFromJson(@NonNull String pkItemId, @NonNull JSONObject payload) throws JSONException {
        payload = payload.getJSONObject(pkItemId);
        this.barcode = payload.getString("barcode");
        this.displayName = payload.getString("displayName");
        this.itemAsJson = payload;
        this.fields = payload.getJSONObject("fields");
    }


    public static MergedItem createNewEmptyItem(String currentRoomString) {
        return new MergedItem(currentRoomString, "-1", "Neues Item");
    }

    public static MergedItem createSearchedForItem(String currentRoomString, String barcode) {
        MergedItem mergedItem = new MergedItem(currentRoomString, "-2");
        mergedItem.barcode = barcode;

        return mergedItem;
    }


    public boolean isNewItem() {
        return pkItemId.equals("-1");
    }

    public boolean isSearchedForItem() {
        return pkItemId.equals("-2");
    }

    @NonNull
    public String getPkItemId() {
        return pkItemId;
    }

    @NonNull
    public String getCurrentRoomNumber() {
        return currentRoomNumber;
    }

    @NonNull
    public JSONObject getItemAsJson() {
        return itemAsJson;
    }

    @NonNull
    public JSONObject getFieldsWithValues() {
        return fields;
    }

    @Nullable
    public String getBarcode() {
        return barcode;
    }

    @Nullable
    public String getDisplayName() {
        return displayName;
    }


    /**
     * Compare the scannedBarcode
     *
     * @param scannedBarcode to compare
     * @return if matches
     */
    public boolean equalsBarcode(String scannedBarcode) {
        if (barcode == null) return false;

        // 12340000 = 12340000
        boolean normalCond = Objects.equals(getBarcode(), scannedBarcode);
        if (normalCond) return true;

        if (barcode.length() == scannedBarcode.length()) {
            // 1234 = 1234 (scannedBarcode maybe doesn't include zeros => only compare to anlage)
            return Objects.equals(barcode, scannedBarcode);

        } else if (barcode.length() < scannedBarcode.length()) {
            // 1234 = 1234|0000| (scannedBarcode includes zeros but local barcode doesn't (=> cut and compare to anlage))
            return Objects.equals(barcode, scannedBarcode.substring(0, barcode.length()));
        }

        // If the scannedBarcode however is shorther than even anlage every hope is lost
        return false;
    }

    public boolean applySearchBarFilter(@NonNull String filter) {
        filter = filter.toLowerCase().trim();
        return getCheckedDisplayBarcode().toLowerCase().trim().contains(filter) || getCheckedDisplayName().toLowerCase().trim().contains(filter);
    }

    public String getCheckedDisplayName() {
        if (displayName == null) return "N/A";
        return displayName;
    }

    public String getCheckedDisplayBarcode() {
        if (barcode == null) return "N/A";
        return barcode;
    }


}
