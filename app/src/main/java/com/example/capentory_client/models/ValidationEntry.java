package com.example.capentory_client.models;

import android.util.Log;

import com.example.capentory_client.ui.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ValidationEntry {
    private static final String CANCEL_CODE = "-205";
    private static final String PK_JSON_KEY = "itemID";
    private static final String MARK_FOR_LATER_VALIDATION_JSON_KEY = "mark_for_later_validation";
    private static final String BARCODE_JSON_KEY = "barcode";
    private static final String SUBROOM_JSON_KEY = "room";
    private MergedItem mergedItem;
    private String pkItem;
    private String barcode;
    private boolean markForLater = false;
    private Room newSubroom = null;
    private List<Field> fieldChanges = new ArrayList<>();

    private ValidationEntry(String pkItem) {
        this.pkItem = pkItem;
    }

    public ValidationEntry(MergedItem mergedItem) {
        this.mergedItem = mergedItem;
        pkItem = mergedItem.getPkItemId();
        barcode = mergedItem.getBarcode();
    }


    public static JSONObject getValidationEntriesAsJson(List<ValidationEntry> validationEntries) throws JSONException {
       /* POST (for each room):
        POST (for each room):
            {
                stocktaking: <Stocktaking-ID>,
                validations: [
                    {
                        "itemID": <itemID>,
                        "barcode": <Scanned Barcode> | null,
                        <fieldName>: <Value>,
                        ...
                    }
                ]
            }
        }*/
        JSONObject ret = new JSONObject();
        JSONArray validationEntriesAsJson = new JSONArray();

        ret.put("stocktaking", MainActivity.getStocktaking().getStocktakingId());
        for (ValidationEntry validationEntry : validationEntries) {
            validationEntriesAsJson.put(getValidationEntryAsJson(validationEntry));
        }
        ret.put("validations", validationEntriesAsJson);

        Log.e("XXXX", validationEntriesAsJson.toString());

        return ret;
    }

    private static JSONObject getValidationEntryAsJson(ValidationEntry validationEntry) throws JSONException {
        JSONObject validationEntryAsJson = new JSONObject();
        validationEntryAsJson.put(PK_JSON_KEY, validationEntry.pkItem);
        // If the user wants the admin to check further
        if (validationEntry.markForLater)
            validationEntryAsJson.put(MARK_FOR_LATER_VALIDATION_JSON_KEY, true);

        if (validationEntry.mergedItem.isNewItem()) {
            // New Items require a barcode
            validationEntryAsJson.put(BARCODE_JSON_KEY, validationEntry.barcode);
        }

        // If subrooms are involved the subRoom of the item might have been changed
        if (validationEntry.newSubroom != null) {
            validationEntryAsJson.put(SUBROOM_JSON_KEY, validationEntry.newSubroom.getRoomId());
        }


        for (Field fieldChange : validationEntry.fieldChanges) {
            if (fieldChange.fieldValue == null)
                fieldChange.fieldValue = JSONObject.NULL;
            validationEntryAsJson.put(fieldChange.fieldName, fieldChange.fieldValue);
        }

        return validationEntryAsJson;
    }

    public static ValidationEntry createCanceledEntry() {
        return new ValidationEntry(CANCEL_CODE);
    }

    public boolean isCanceledItem() {
        return pkItem.equals(CANCEL_CODE);
    }

    public void addChangedFieldFromFormValue(String fieldName, Object valueFromForm) {
        try {
            if (mergedItem.isNewItem()) {
                // this means a new Item should be created, therefore take all values
                fieldChanges.add(new ValidationEntry.Field<>(fieldName, valueFromForm));
            } else if (!mergedItem.getFieldsWithValues().get(fieldName).equals(valueFromForm)) {
                // for existing items compare if something changed
                fieldChanges.add(new ValidationEntry.Field<>(fieldName, valueFromForm));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void setStaticMarkForLater(boolean b) {
        markForLater = b;
    }

    public void setStaticRoomChange(Room selectedRoom, Room currentRoom) {
        if (!selectedRoom.equals(currentRoom)) {
            newSubroom = selectedRoom;
        }
    }


    public static class Field<T> {
        private String fieldName;
        private T fieldValue;

        public Field(String fieldName, T fieldValue) {
            this.fieldName = fieldName;
            this.fieldValue = fieldValue;
        }
    }
}
