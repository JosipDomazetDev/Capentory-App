package com.example.capentory_client.models;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.capentory_client.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents an MergedItem from ralph, only the desc and sap_item_number (scancode) is directly stored to allow later changes to the server
 */
public class MergedItem implements RecyclerviewItem {
    private static final String NEW_ITEM_CODE = "-1", SEARCHED_FOR_ITEM_CODE = "-2";
    @NonNull
    private String pkItemId;
    @NonNull
    private JSONObject itemAsJson, fields;
    @Nullable
    private String barcode, displayName, displayDescription, descriptionaryRoom;
    private int timesFoundLast = 0, timesFoundCurrent = 0;
    private Room subroom;
    // For Items this control whether a item is already validated and  therefore whether a item should be included into the expand/collapse behaviour
    private boolean isExpanded = true;
    // Optional attachments
    private ArrayList<Attachment> attachments = new ArrayList<>();


    @Nullable
    public Room getSubroom() {
        return subroom;
    }

    // Standard
    public MergedItem(@NonNull JSONObject payload) throws JSONException {
        extractFromJson(payload);
    }

    // For not fully fledged item just for the code
    private MergedItem(@NonNull String pkItemId) {
        this.pkItemId = pkItemId;
    }

    public MergedItem(JSONObject payload, Room subroom) throws JSONException {
        extractFromJson(payload);
        this.subroom = subroom;
    }

    private void extractFromJson(@NonNull JSONObject payload) throws JSONException {
        this.pkItemId = payload.getString("itemID");
        this.barcode = payload.getString("barcode");
        this.displayName = payload.getString("displayName");
        this.displayDescription = payload.getString("displayDescription");
        this.timesFoundLast = payload.optInt("times_found_last", 0);
        this.descriptionaryRoom = payload.getString("room");
        this.itemAsJson = payload;
        this.fields = payload.getJSONObject("fields");

        JSONArray attachmentsAsJSON = payload.getJSONArray("attachments");
        if (attachmentsAsJSON == null) return;

        for (int i = 0; i < attachmentsAsJSON.length(); i++) {
            attachments.add(new Attachment(attachmentsAsJSON.getJSONObject(i)));
        }
    }

    public static MergedItem createNewEmptyItem(Context context) {
        MergedItem mergedItem = new MergedItem(NEW_ITEM_CODE);
        mergedItem.displayName = context.getString(R.string.new_item_merged_item);
        mergedItem.fields = new JSONObject();
        mergedItem.itemAsJson = new JSONObject();
        return mergedItem;
    }

    public static MergedItem createNewEmptyItemWithBarcode(String barcode, Context context) {
        MergedItem mergedItem = createNewEmptyItem(context);
        mergedItem.barcode = barcode;
        mergedItem.displayName = context.getString(R.string.item_not_in_db_merged_item);
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

    public boolean isParentItem() {
        return timesFoundLast > 1;
    }


    public int getTimesFoundCurrent() {
        return timesFoundCurrent;
    }

    @Nullable
    public String getDisplayDescription() {
        return displayDescription;
    }

    public ArrayList<Attachment> getAttachments() {
        return attachments;
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
        return Objects.equals(getBarcode(), scannedBarcode);

       /* if (barcode.length() == scannedBarcode.length()) {
            // 1234 = 1234 (scannedBarcode maybe doesn't include zeros => only compare to anlage)
            return Objects.equals(barcode, scannedBarcode);

        } else if (barcode.length() < scannedBarcode.length()) {
            // 1234 = 1234|0000| (scannedBarcode includes zeros but local barcode doesn't (=> cut and compare to anlage))
            return Objects.equals(barcode, scannedBarcode.substring(0, barcode.length()));
        }

        // If the scannedBarcode however is shorther than even anlage every hope is lost
        return false;*/
    }

    @Override
    public boolean applySearchBarFilter(@NonNull String filter) {
        filter = filter.toLowerCase().trim();
        return getCheckedDisplayBarcode().toLowerCase().trim().contains(filter) || getCheckedDisplayName().toLowerCase().trim().contains(filter);
    }

    @Override
    public boolean isTopLevelRoom() {
        return false;
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

    @Nullable
    public String getDescriptionaryRoom() {
        return descriptionaryRoom;
    }

    public int getRemainingTimes() {
        return getTimesFoundLast() - getTimesFoundCurrent();
    }


    @Override
    public boolean isExpanded() {
        return isExpanded;
    }

    @Override
    public void setExpanded(boolean b) {
        isExpanded = b;
    }

    public void addAttachment(Attachment attachment) {
        attachments.add(attachment);
    }
}
