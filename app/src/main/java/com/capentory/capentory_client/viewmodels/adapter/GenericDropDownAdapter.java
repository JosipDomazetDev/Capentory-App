package com.capentory.capentory_client.viewmodels.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.capentory.capentory_client.R;
import com.capentory.capentory_client.models.Room;

import java.util.ArrayList;
import java.util.List;

public class GenericDropDownAdapter<T extends GenericDropDownAdapter.DropDownEntry> extends ArrayAdapter<T> implements Filterable {


    public interface DropDownEntry {
        String displayName();

        String displayDescription();

        boolean applySearchBarFilter(@NonNull String filter);
    }

    private ArrayList<T> entriesFull;

    public GenericDropDownAdapter(Context context) {
        super(context, 0, new ArrayList<>());
    }

    public GenericDropDownAdapter(Context context, ArrayList<T> entries) {
        super(context, 0);
        fill(entries);
    }

    public void fill(ArrayList<T> entries) {
        clear();
        addAll(entries);

        this.entriesFull = new ArrayList<>(entries);
        notifyDataSetChanged();
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
                    R.layout.dropdown_room_row, parent, false
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


    @NonNull
    @Override
    public Filter getFilter() {
        return genericFilter;
    }

    private final Filter genericFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<DropDownEntry> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(entriesFull);
            } else {
                String filter = constraint.toString();

                for (T entry : entriesFull) {
                    if (entry.applySearchBarFilter(filter)) {
                        filteredList.add(entry);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            if (results.values == null) return;
            addAll((List<? extends T>) results.values);
            notifyDataSetChanged();
        }
    };
}
