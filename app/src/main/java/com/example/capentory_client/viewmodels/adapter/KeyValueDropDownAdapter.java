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

import java.util.Iterator;

public class KeyValueDropDownAdapter extends ArrayAdapter<KeyValueDropDownAdapter.DropDownEntry> {

    private final DropDownEntry[] items;

    public KeyValueDropDownAdapter(Context context, int textViewResourceId,
                                   DropDownEntry[] dropDownEntries) {
        super(context, textViewResourceId, dropDownEntries);
        items = dropDownEntries;
    }

    public int getItemIndexFromKey(int key) {
        if (key == -1) return 0;

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

    public static class DropDownEntry {
        int key;
        String description;

        public DropDownEntry(JSONObject jsonObject) throws JSONException {
            Iterator<String> keys = jsonObject.keys();
            this.key = jsonObject.getInt(keys.next());
            this.description = jsonObject.getString(keys.next());
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
    }
}
