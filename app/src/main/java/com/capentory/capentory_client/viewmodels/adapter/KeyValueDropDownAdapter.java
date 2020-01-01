package com.capentory.capentory_client.viewmodels.adapter;

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

import java.util.Iterator;

public class KeyValueDropDownAdapter extends ArrayAdapter<KeyValueDropDownAdapter.DropDownEntry> {

    private final DropDownEntry[] items;

    public KeyValueDropDownAdapter(Context context, int textViewResourceId,
                                   DropDownEntry[] dropDownEntries) {
        super(context, textViewResourceId, dropDownEntries);
        items = dropDownEntries;
    }

    public int getItemIndexFromKey(Object key) {
        if (key == null) return 0;

        for (int i = 0; i < items.length; i++) {
            if (key.equals(items[i].getKey())) {
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


    public static class DropDownEntry {
        Object key;
        String description;

        public DropDownEntry(JSONObject jsonObject) throws JSONException {
            Iterator<String> keys = jsonObject.keys();
            this.key = jsonObject.get(keys.next());
            this.description = jsonObject.getString(keys.next());
        }

        // For manual Null entry
        public DropDownEntry(int key, String description) {
            this.key = key;
            this.description = description;
        }


        public Object getKey() {
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


    }
}
