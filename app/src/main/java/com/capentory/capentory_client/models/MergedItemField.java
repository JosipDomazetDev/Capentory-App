package com.capentory.capentory_client.models;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MergedItemField implements Comparable<MergedItemField> {
    public static final int NORMAL_FIELD_CODE = 0;
    public static final int EXTRA_FIELD_CODE = -1;
    public static final int CUSTOM_FIELD_CODE = -2;

    @NonNull
    private String key, type, verboseName;
    private boolean readOnly;
    private int fieldClassifier = 0;


    @NonNull
    private JSONArray choices;


    public MergedItemField(@NonNull String key, JSONObject payload, int fieldClassifier) throws JSONException {
        this.key = key;
        payload = payload.getJSONObject(key);
        readOnly = payload.optBoolean("readOnly");
        type = payload.optString("type", "");
        verboseName = payload.optString("verboseFieldName");
        choices = payload.optJSONArray("choices");
        this.fieldClassifier = fieldClassifier;
    }


    @NonNull
    public String getKey() {
        return key;
    }

    @NonNull
    public String getType() {
        return type;
    }

    @NonNull
    public String getVerboseName() {
        return verboseName;
    }


    public boolean isReadOnly() {
        return readOnly;
    }

    public boolean isExtraField() {
        return fieldClassifier == EXTRA_FIELD_CODE || isCustomField();
}

    public boolean isCustomField() {
        return fieldClassifier == CUSTOM_FIELD_CODE;
    }

    @NonNull
    public JSONArray getChoices() {
        return choices;
    }

    @Override
    public int compareTo(MergedItemField that) {

        if (Boolean.compare(this.isExtraField(), that.isExtraField()) == -1) {
            return -1;
        } else if (Boolean.compare(this.isExtraField(), that.isExtraField()) == 1) {
            return 1;
        }


        if (Boolean.compare(this.readOnly, that.readOnly) == -1) {
            return -1;
        } else if (Boolean.compare(this.readOnly, that.readOnly) == 1) {
            return 1;
        }


        if (this.type.compareTo(that.type) < 0) {
            return 1;
        } else if (this.type.compareTo(that.type) > 0) {
            return -1;
        }

        if (this.key.compareTo(that.key) < 0) {
            return -1;
        } else if (this.key.compareTo(that.key) > 0) {
            return 1;
        }


        if (this.verboseName.compareTo(that.verboseName) < 0) {
            return -1;
        } else if (this.verboseName.compareTo(that.verboseName) > 0) {
            return 1;
        }


        return 1;
    }
}
