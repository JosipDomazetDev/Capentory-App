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
    private static final String BARCODE_JSON_KEY = "barcode";
    private MergedItem mergedItem;
    private String pkItem;
    private String barcode;
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
        if (validationEntry.mergedItem.isNewItem()) {
            // New Items require a barcode
            validationEntryAsJson.put(BARCODE_JSON_KEY, validationEntry.barcode);
        }

        for (Field fieldChange : validationEntry.fieldChanges) {
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

    public void addChangedFieldFromFormValue(String fieldName, Object valueFromForm) throws JSONException {
        if (mergedItem.isNewItem()) {
            // this means a new Item should be created, therefore take all values
            fieldChanges.add(new ValidationEntry.Field<>(fieldName, valueFromForm));
        } else if (!mergedItem.getFieldsWithValues().get(fieldName).equals(valueFromForm)) {
            // for existing items compare if something changed
            fieldChanges.add(new ValidationEntry.Field<>(fieldName, valueFromForm));
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
