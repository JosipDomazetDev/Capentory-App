package com.capentory.capentory_client.models;

import android.util.Log;

import com.capentory.capentory_client.ui.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ValidationEntry {
    private static final String CANCEL_CODE = "-205";

    private static final String STOCKTAKING_JSON_KEY = "stocktaking";
    private static final String VALIDATIONS_JSON_KEY = "validations";

    private static final String PK_JSON_KEY = "itemID";
    private static final String MARK_FOR_LATER_VALIDATION_JSON_KEY = "mark_for_later_validation";
    private static final String BARCODE_JSON_KEY = "barcode";
    private static final String SUBROOM_JSON_KEY = "room";

    private static final String ATTACHMENT_JSON_KEY = "attachments";
    private static final String CUSTOM_FIELDS_JSON_KEY = "custom_fields";

    private MergedItem mergedItem;
    private String pkItem;
    private String barcode;
    private List<Attachment> attachments = new ArrayList<>();
    private boolean markForLater = false;
    private Room newSubroom = null;
    private List<Field> normalFieldChanges = new ArrayList<>();
    private List<Field> customFieldChanges = new ArrayList<>();

    private ValidationEntry(String pkItem) {
        this.pkItem = pkItem;
    }

    public ValidationEntry(MergedItem mergedItem) {
        this.mergedItem = mergedItem;
        pkItem = mergedItem.getPkItemId();
        barcode = mergedItem.getBarcode();
        attachments = mergedItem.getAttachments();
    }


    public static JSONObject getValidationEntriesAsJson(List<ValidationEntry> validationEntries) throws JSONException {
        /*{
            POST-Format:
                "stocktaking": <Stocktaking-ID>,
                "validations": [
                    {
                        "itemID": <itemID>,
                        "mark_for_later_validation": true/false,
                        "<fieldName>": <Value>,
                        ...
                        // if attachments are accepted:
                        "attachments": [
                            <id of an attachment>,
                            ...
                        ],
                        //if custom fields are accepted:
                        "custom_fields": {
                            "<Custom-Field Key>": <Custom-Field Value>,
                            ...
                        }
                    }
                ]
        }*/
        JSONObject ret = new JSONObject();
        JSONArray validationEntriesAsJson = new JSONArray();

        ret.put(STOCKTAKING_JSON_KEY, MainActivity.getStocktaking().getStocktakingId());
        for (ValidationEntry validationEntry : validationEntries) {
            validationEntriesAsJson.put(getValidationEntryAsJson(validationEntry));
        }
        ret.put(VALIDATIONS_JSON_KEY, validationEntriesAsJson);

        Log.e("XXXX", ret.toString());

        return ret;
    }

    private static JSONObject getValidationEntryAsJson(ValidationEntry validationEntry) throws JSONException {
        JSONObject validationEntryAsJson = new JSONObject();
        validationEntryAsJson.put(PK_JSON_KEY, validationEntry.pkItem);
        // If the user wants the admin to check further
        if (validationEntry.markForLater) {
            validationEntryAsJson.put(MARK_FOR_LATER_VALIDATION_JSON_KEY, true);
        }

        if (validationEntry.mergedItem.isNewItem()) {
            // New Items require a barcode
            validationEntryAsJson.put(BARCODE_JSON_KEY, validationEntry.barcode);
        }

        if (validationEntry.attachments.size() > 0) {
            // Always sent all attachments that the users wants to keep
            validationEntryAsJson.put(ATTACHMENT_JSON_KEY, Attachment.getAttachmentsAsJSON(validationEntry.attachments));
        }

        // If subrooms are involved the subRoom of the item might have been changed
        if (validationEntry.newSubroom != null) {
            validationEntryAsJson.put(SUBROOM_JSON_KEY, validationEntry.newSubroom.getRoomId());
        }

        // Normal-Fields
        addFieldChanges(validationEntryAsJson, validationEntry.normalFieldChanges);

        // Custom-Fields
        JSONObject customFieldAsJson = new JSONObject();
        addFieldChanges(customFieldAsJson, validationEntry.customFieldChanges);
        validationEntryAsJson.put(CUSTOM_FIELDS_JSON_KEY, customFieldAsJson);

        return validationEntryAsJson;
    }

    private static void addFieldChanges(JSONObject container, List<Field> fieldChanges) throws JSONException {
        for (Field fieldChange : fieldChanges) {
            if (fieldChange.fieldValue == null)
                fieldChange.fieldValue = JSONObject.NULL;
            container.put(fieldChange.fieldName, fieldChange.fieldValue);
        }
    }

    public static ValidationEntry createCanceledEntry() {
        return new ValidationEntry(CANCEL_CODE);
    }

    public boolean isCanceledItem() {
        return pkItem.equals(CANCEL_CODE);
    }

    public void addChangedFieldFromFormValue(MergedItemField field, Object valueFromForm) {
        try {
            if (mergedItem.isNewItem()) {
                // This means a new Item should be created, therefore take all the values the user provided
                normalFieldChanges.add(new ValidationEntry.Field<>(field.getKey(), valueFromForm));
            } else if (field.isCustomField() && mergedItem.getCustomFieldsWithValues() != null) {
                // Custom-Fields have another source and not every item has all Custom-Fields from the OPTIONS-Request
                if (mergedItem.getCustomFieldsWithValues().has(field.getKey())) {
                    addOnChange(field, valueFromForm, mergedItem.getCustomFieldsWithValues(), customFieldChanges);
                }
            }
            // Normal Field
            else {
                addOnChange(field, valueFromForm, mergedItem.getNormalFieldsWithValues(), normalFieldChanges);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void addOnChange(MergedItemField field, Object valueFromForm, JSONObject fieldsWithValues, List<Field> fieldChanges) throws JSONException {
        if (!fieldsWithValues.get(field.getKey()).equals(valueFromForm)) {
            // for existing items compare if something changed
            fieldChanges.add(new Field<>(field.getKey(), valueFromForm));
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
