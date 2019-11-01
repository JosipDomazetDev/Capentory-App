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
import com.example.capentory_client.models.SerializerEntry;

import java.util.ArrayList;

public class DropDownSerializerAdapter  extends ArrayAdapter<SerializerEntry> {


    public DropDownSerializerAdapter(Context context, ArrayList<SerializerEntry> actualRooms) {
        super(context, 0, actualRooms);
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

        TextView textViewRoomNumber = convertView.findViewById(R.id.room_tv);
        TextView textViewDescription = convertView.findViewById(R.id.room_desc_tv);
        SerializerEntry currentSer = getItem(position);

        if (currentSer != null) {
            textViewRoomNumber.setText(currentSer.getKey());
            textViewDescription.setText(currentSer.getDescription());
        }

        return convertView;
    }
}
