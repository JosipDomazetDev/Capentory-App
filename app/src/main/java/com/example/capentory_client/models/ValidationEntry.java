package com.example.capentory_client.models;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ValidationEntry {
    private int pkItem;
    private boolean withoutChange;
    private List<Field> fieldChanges;

    public ValidationEntry(int pkItem) {
        this.pkItem = pkItem;
        this.fieldChanges = new ArrayList<>();
    }


    public static String getValidationEntriesAsJson(List<ValidationEntry> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    public void finishWithChanges(List<Field> changes) {
        if (changes.isEmpty()) {
            withoutChange = true;
            return;
        }

        withoutChange = false;
        fieldChanges = changes;
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
