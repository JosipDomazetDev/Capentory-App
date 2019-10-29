package com.example.capentory_client.models;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MergedItemField implements Comparable<MergedItemField> {

    @NonNull
    private String key, type, label;
    private boolean required, readOnly;

    @NonNull
    private JSONArray choices;

    public MergedItemField(@NonNull String key, JSONObject payload) throws JSONException {
        this.key = key;
        type = payload.getString("type");
        label = payload.getString("label");
        required = payload.getBoolean("required");
        readOnly = payload.getBoolean("read_only");
        choices = payload.optJSONArray("choices");
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

    @NonNull
    public JSONArray getChoices() {
        return choices;
    }

    @Override
    public int compareTo(MergedItemField that) {


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


        if (this.label.compareTo(that.label) < 0) {
            return -1;
        } else if (this.label.compareTo(that.label) > 0) {
            return 1;
        }

        if (Boolean.compare(this.required, that.required) == -1) {
            return -1;
        } else if (Boolean.compare(this.required, that.required) == 1) {
            return 1;
        }

        if (Boolean.compare(this.readOnly, that.readOnly) == -1) {
            return -1;
        } else if (Boolean.compare(this.readOnly, that.readOnly) == 1) {
            return 1;
        }

        return 1;
    }
}
