package com.example.capentory_client.viewmodels.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;

public class KeyValueDropDownAdapter extends ArrayAdapter<KeyValueDropDownAdapter.DropDownEntry> {

    private final DropDownEntry[] items;
    public static int NULL_KEY_VALUE = -1;

    public KeyValueDropDownAdapter(Context context, int textViewResourceId,
                                   DropDownEntry[] dropDownEntries) {
        super(context, textViewResourceId, dropDownEntries);
        items = dropDownEntries;
        Arrays.sort(items);
    }

    public int getItemIndexFromKey(int key) {
        // The Last Entry in the list is the null representation
        if (key == NULL_KEY_VALUE) {
            for (int i = 0; i < items.length; i++) {
                if (items[i].isNullRepresentationEntry()) {
                    return i;
                }
            }
            return 0;
        }

        for (int i = 0; i < items.length; i++) {
            if (key == (items[i].getKey())) {
                return i;
            }
        }

        return 0;
    }


    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
        textView.setTextColor(Color.BLACK);
        DropDownEntry currentItem = getItem(position);


        if (currentItem == null) return textView;
        textView.setText(currentItem.getDescription());
        return textView;
    }


    public static class DropDownEntry implements Comparable<DropDownEntry> {
        int key;
        String description;
        boolean nullRepresentationEntry = false;

        public DropDownEntry(JSONObject jsonObject) throws JSONException {
            Iterator<String> keys = jsonObject.keys();
            this.key = jsonObject.getInt(keys.next());
            this.description = jsonObject.getString(keys.next());
        }

        // For manual Null entry
        public DropDownEntry(int key, String description) {
            this.key = key;
            this.description = description;
            nullRepresentationEntry = true;
        }

        public boolean isNullRepresentationEntry() {
            return nullRepresentationEntry;
        }

        public int getKey() {
            return key;
        }

        public String getDescription() {
            return description;
        }

        @NonNull
        @Override
        public String toString() {
            return getDescription();
        }


        @Override
        public int compareTo(DropDownEntry that) {
            if (this.description.toLowerCase().compareTo(that.description.toLowerCase()) < 0) {
                return -1;
            } else if (this.description.toLowerCase().compareTo(that.description.toLowerCase()) > 0) {
                return 1;
            }

            if (this.key < that.key) {
                return -1;
            } else if (this.key > that.key) {
                return 1;
            }

            return Boolean.compare(nullRepresentationEntry, that.nullRepresentationEntry);
        }
    }
}
