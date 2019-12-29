package com.capentory.capentory_client.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.capentory.capentory_client.viewmodels.adapter.GenericDropDownAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an Room from ralph, only the displayNumber is directly stored to allow later changes to the server
 */
public class Room implements GenericDropDownAdapter.DropDownEntry, Comparable<Room>, RecyclerviewItem {
    @NonNull
    private String displayNumber, roomId;

    @Nullable
    private String displayDescriptions, barcode;

    // These are only for subrooms
    private final List<MergedItem> mergedItems = new ArrayList<>();
    private final List<Room> subRooms = new ArrayList<>();
    private int depth;
    private boolean isExpanded = true;
    private boolean firstHeaderShouldNotBeRemoved =false;

    // only for subrooms
    public Room(JSONObject payload, int depth) throws JSONException {
        extractRoomFromJson(payload);
        this.depth = depth;
    }

    public Room(JSONObject payload) throws JSONException {
        extractRoomFromJson(payload);
    }


    public List<Room> getSubRooms() {
        return subRooms;
    }

    private void extractRoomFromJson(JSONObject payload) throws JSONException {
        this.roomId = payload.getString("roomID");
        this.displayNumber = payload.getString("displayName");
        this.displayDescriptions = payload.getString("displayDescription");
        this.barcode = payload.getString("barcode");
    }

    @NonNull
    public String getRoomId() {
        return roomId;
    }

    public List<MergedItem> getMergedItems() {
        return mergedItems;
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

        if (this.roomId.compareTo(that.roomId) < 0) {
            return -1;
        } else if (this.roomId.compareTo(that.roomId) > 0) {
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


    @Nullable
    public String getBarcode() {
        return barcode;
    }

    public boolean equalsBarcode(String scannedBarcode) {
        if (getBarcode() == null) return false;
        return getBarcode().equals(scannedBarcode);
    }


    @Override
    public boolean applySearchBarFilter(@NonNull String filter) {
        filter = filter.toLowerCase().trim();
        return displayNumber.toLowerCase().trim().contains(filter) || displayDescription().toLowerCase().trim().contains(filter);
    }

    public void addItemToRoom(MergedItem mergedItem) {
        mergedItems.add(mergedItem);
    }

    public int getDepth() {
        return depth;
    }

    public void addSubRoomToSuperRoom(Room subRoom) {
        subRooms.add(subRoom);
    }

    @Override
    public boolean isTopLevelRoom() {
        return depth == 0;
    }

    @Override
    public boolean isExpanded() {
        return isExpanded;
    }

    @Override
    public void setExpanded(boolean b) {
        isExpanded = b;
    }


    public boolean isFirstHeaderShouldNotBeRemoved() {
        return firstHeaderShouldNotBeRemoved;
    }

    public void setFirstHeaderShouldNotBeRemoved(boolean firstHeaderShouldNotBeRemoved) {
        this.firstHeaderShouldNotBeRemoved = firstHeaderShouldNotBeRemoved;
    }
}
