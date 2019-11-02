package com.example.capentory_client.models;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ValidationEntry {
    public static final String NOT_FOUND = "-404";
    private static final String PK_JSON_KEY = "itemId";
    private String pkItem;
    private List<Field> fieldChanges;

    public ValidationEntry(String pkItem) {
        this.pkItem = pkItem;
        this.fieldChanges = new ArrayList<>();
    }


    public static JSONArray getValidationEntriesAsJson(List<ValidationEntry> list) {
        JSONArray ret = new JSONArray();
        try {
            for (ValidationEntry validationEntry : list) {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put(PK_JSON_KEY, validationEntry.pkItem);

                for (Field fieldChange : validationEntry.fieldChanges) {
                    jsonObject.put(fieldChange.fieldName, fieldChange.fieldValue);
                }
                ret.put(jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }

 /*   @SuppressWarnings("unchecked")
    public static ValidationEntry createMissingEntry(MergedItem item) {
        ValidationEntry validationEntry = new ValidationEntry(item.getPkItemId());
        validationEntry.withoutChange = false;
        List<Field> changes = new ArrayList<>();
        changes.add(new Field(MergedItem.ROOM_JSON_KEY, -1));
        validationEntry.finishWithChanges(changes);
        return validationEntry;
    }*/

    public void finishWithChanges(List<Field> changes) {
        if (changes.isEmpty()) {
            return;
        }
        fieldChanges = changes;
    }

    public boolean isMissingItem() {
        return pkItem.equals(NOT_FOUND);
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
