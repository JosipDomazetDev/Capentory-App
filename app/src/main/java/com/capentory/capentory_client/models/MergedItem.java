package com.capentory.capentory_client.models;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.capentory.capentory_client.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Represents an MergedItem from ralph, only the desc and sap_item_number (scancode) is directly stored to allow later changes to the server
 */
public class MergedItem implements RecyclerViewItem {
    private static final String NEW_ITEM_CODE = "-1", SEARCHED_FOR_ITEM_CODE = "-2";
    private static final int WAS_FOUND = 1, NOT_DECIDED = 0, WAS_NOT_FOUND = -1;
    @NonNull
    private String itemID;
    @NonNull
    private JSONObject fields, customFields;
    @Nullable
    private String barcode, displayName, displayDescription, descriptionaryRoom;
    private int timesFoundLast = 0, timesFoundCurrent = 0;
    private Room subroom;
    // For Items this control whether a item is already validated and  therefore whether a item should be included into the expand/collapse behaviour


    private boolean isExpanded = true;
    // Only used for the DONE screen
    private int wasFound = NOT_DECIDED;
    // Optional attachments
    private ArrayList<Attachment> attachments = new ArrayList<>();


    @Nullable
    public Room getSubroom() {
        return subroom;
    }

    public int getAmountOfTotalItemsForSubroom(int c) {
        if (subroom == null) return 0;
        for (MergedItem mergedItem : subroom.getMergedItems()) {
            c += mergedItem.getAmountOfTotalItemsForSubroom(c);
        }

        return subroom.getMergedItems().size();
    }

    // Standard
    public MergedItem(@NonNull JSONObject payload) throws JSONException {
        extractFromJson(payload);
    }

    // For not fully fledged item just for the code
    private MergedItem(@NonNull String itemID) {
        this.itemID = itemID;
    }

    public MergedItem(JSONObject payload, Room subroom) throws JSONException {
        extractFromJson(payload);
        this.subroom = subroom;
    }

    private void extractFromJson(@NonNull JSONObject payload) throws JSONException {
        this.itemID = payload.getString("itemID");
        this.barcode = payload.getString("barcode");
        this.displayName = payload.getString("displayName");
        this.displayDescription = payload.getString("displayDescription");
        this.timesFoundLast = payload.optInt("times_found_last", 0);
        this.descriptionaryRoom = payload.getString("room");
        this.fields = payload.getJSONObject("fields");
        this.customFields = this.fields.getJSONObject("custom_fields");

        // If user doesn't have rights no attachments are sent
        JSONArray attachmentsAsJSON = payload.optJSONArray("attachments");
        if (attachmentsAsJSON == null) return;

        for (int i = 0; i < attachmentsAsJSON.length(); i++) {
            attachments.add(new Attachment(attachmentsAsJSON.getJSONObject(i)));
        }
    }

    public static MergedItem createNewEmptyItem(Context context) {
        MergedItem mergedItem = new MergedItem(NEW_ITEM_CODE);
        mergedItem.displayName = context.getString(R.string.new_item_merged_item);
        mergedItem.fields = new JSONObject();
        mergedItem.customFields = new JSONObject();
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
        return itemID.equals(NEW_ITEM_CODE);
    }/**/

    public boolean isSearchedForItem() {
        return itemID.equals(SEARCHED_FOR_ITEM_CODE);
    }

    @NonNull
    public String getItemID() {
        return itemID;
    }


    @Nullable
    public JSONObject getCustomFieldsWithValues() {
        return customFields;
    }

    @NonNull
    public JSONObject getNormalFieldsWithValues() {
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
        return barcode.equals(scannedBarcode);
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

    public RecyclerViewItem finish(boolean wasFound) {
        if (wasFound) {
            this.wasFound = WAS_FOUND;
        } else {
            this.wasFound = WAS_NOT_FOUND;
        }
        return this;
    }


    public boolean wasFound() {
        return wasFound == WAS_FOUND;
    }

    public boolean wasNotFound() {
        return wasFound == WAS_NOT_FOUND;
    }

    public boolean notDecided() {
        return wasFound == NOT_DECIDED;
    }


}
