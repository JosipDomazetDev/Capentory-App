package com.example.capentory_client.viewmodels.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capentory_client.R;
import com.example.capentory_client.models.MergedItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> implements Filterable {


    private List<MergedItem> mergedItems = new ArrayList<>();
    private List<MergedItem> mergedItemsFull;
    private ItemClickListener itemClickListener;

    public RecyclerViewAdapter(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void fill(List<MergedItem> mergedItems) {
        this.mergedItems = mergedItems;
        if (mergedItems != null)
            this.mergedItemsFull = new ArrayList<>(mergedItems);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview_row, parent, false);
        return new ViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        MergedItem mergedItem = mergedItems.get(position);
        holder.anlage_textview.setText(mergedItem.getCheckedDisplayBarcode());
        holder.anlage_bez_textview.setText(mergedItem.getCheckedDisplayName());

        if (mergedItem.getTimesFoundCurrent() > 1) {
            holder.counter_textview.setText(String.format("Gefunden: %1$s", (mergedItem.getTimesFoundCurrent() + "/" + mergedItem.getTimesFoundLast())));
            holder.optional_counter_container.setVisibility(View.VISIBLE);
        } else holder.optional_counter_container.setVisibility(View.GONE);

    }

    @Override
    public int getItemCount() {
        return mergedItems.size();
    }

    public MergedItem getItem(int position) {
        return mergedItems.get(position);
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView anlage_textview;
        TextView anlage_bez_textview;
        TextView counter_textview;
        View optional_counter_container;
        ItemClickListener itemClickListener;

        public ViewHolder(View itemView, ItemClickListener itemClickListener) {
            super(itemView);
            anlage_textview = itemView.findViewById(R.id.anlage_textview);
            anlage_bez_textview = itemView.findViewById(R.id.anlage_bez_textview);
            counter_textview = itemView.findViewById(R.id.counter_textview);
            optional_counter_container = itemView.findViewById(R.id.optional_counter_container);

            this.itemClickListener = itemClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            itemClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public interface ItemClickListener {
        void onItemClick(int position, View v);
    }


    @Override
    public Filter getFilter() {
        return mergedItemFilter;
    }

    private Filter mergedItemFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<MergedItem> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(mergedItemsFull);
            } else {
                String filter = constraint.toString();

                for (MergedItem mergedItem : mergedItemsFull) {
                    if (mergedItem.applySearchBarFilter(filter)) {
                        filteredList.add(mergedItem);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mergedItems.clear();
            mergedItems.addAll((Collection<? extends MergedItem>) results.values);
            notifyDataSetChanged();
        }
    };
}