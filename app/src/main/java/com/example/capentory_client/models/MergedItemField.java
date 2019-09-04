package com.example.capentory_client.models;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MergedItemField {
    private JSONObject field;

    @NonNull
    private String key, type, label;
    private boolean required, readOnly;

    @Nullable
    private JSONArray choices;

    public MergedItemField(@NonNull String key, JSONObject payload) throws JSONException {
        this.key = key;
        type = payload.getString("type");
        label = payload.getString("label");
        required = payload.getBoolean("required");
        readOnly = payload.getBoolean("read_only");
        choices = payload.optJSONArray("choices");

        field=payload;
    }


    public JSONObject getFieldAsJSON() {
        return field;
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
    public String getLabel() {
        return label;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    @Nullable
    public JSONArray getChoices() {
        return choices;
    }
}
