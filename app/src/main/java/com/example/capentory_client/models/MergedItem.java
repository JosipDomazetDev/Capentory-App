package com.example.capentory_client.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Represents an MergedItem from ralph, only the desc and sap_item_number (scancode) is directly stored to allow later changes to the server
 */
public class MergedItem {
    private static final String NEW_ITEM_CODE = "-1", SEARCHED_FOR_ITEM_CODE = "-2";
    @NonNull
    private String pkItemId;
    @NonNull
    private JSONObject itemAsJson, fields;
    @Nullable
    private String barcode, displayName;
    private int timesFoundLast, timesFoundCurrent = 0;


    // Standard
    public MergedItem(@NonNull String pkItemId, @NonNull JSONObject payload) throws JSONException {
        this.pkItemId = pkItemId;
        extractFromJson(pkItemId, payload);
    }

    // For not fully fledged item just for the code
    private MergedItem(@NonNull String pkItemId) {
        this.pkItemId = pkItemId;
    }

    private void extractFromJson(@NonNull String pkItemId, @NonNull JSONObject payload) throws JSONException {
        payload = payload.getJSONObject(pkItemId);
        this.barcode = payload.getString("barcode");
        this.displayName = payload.getString("displayName");
        this.timesFoundLast = payload.getInt("times_found_last");
        this.itemAsJson = payload;
        this.fields = payload.getJSONObject("fields");
    }

    public static MergedItem createNewEmptyItem() {
        MergedItem mergedItem = new MergedItem(NEW_ITEM_CODE);
        mergedItem.displayName = "Neues Item";
        mergedItem.fields = new JSONObject();
        mergedItem.itemAsJson = new JSONObject();
        return mergedItem;
    }

    public static MergedItem createNewEmptyItemWithBarcode(String barcode) {
        MergedItem mergedItem = createNewEmptyItem();
        mergedItem.barcode = barcode;
        mergedItem.displayName = "Item befindet sich nicht in der Datenbank!";
        return mergedItem;
    }

    //Temporary Item, will either be transformed to a EmptyItemWithBarcode or a normal Item
    public static MergedItem createSearchedForItem(String barcode) {
        MergedItem mergedItem = new MergedItem(SEARCHED_FOR_ITEM_CODE);
        mergedItem.barcode = barcode;

        return mergedItem;
    }


    public boolean isNewItem() {
        return pkItemId.equals(NEW_ITEM_CODE);
    }/**/

    public boolean isSearchedForItem() {
        return pkItemId.equals(SEARCHED_FOR_ITEM_CODE);
    }

    @NonNull
    public String getPkItemId() {
        return pkItemId;
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


    public int getTimesFoundLast() {
        return timesFoundLast;
    }


    public int getTimesFoundCurrent() {
        return timesFoundCurrent;
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


    public void increaseTimesFoundCurrent() {
        timesFoundCurrent++;
    }
}
