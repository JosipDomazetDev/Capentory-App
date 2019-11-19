package com.example.capentory_client.viewmodels.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.capentory_client.R;

import java.util.ArrayList;

public class GenericDropDownAdapter<T extends GenericDropDownAdapter.DropDownEntry> extends ArrayAdapter<T> {


    public interface DropDownEntry {
        String displayName();

        String displayDescription();
    }


    public GenericDropDownAdapter(Context context, ArrayList<T> entries) {
        super(context, 0, entries);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.room_dropdown_row, parent, false
            );
        }

        TextView name = convertView.findViewById(R.id.name_tv);
        TextView description = convertView.findViewById(R.id.desc_tv);

        T entry;
        try {
            entry = getItem(position);
            if (entry != null) {
                name.setText(entry.displayName());
                description.setText(entry.displayDescription());
            }
        } catch (Exception ignored) {
        }
        return convertView;
    }


}
