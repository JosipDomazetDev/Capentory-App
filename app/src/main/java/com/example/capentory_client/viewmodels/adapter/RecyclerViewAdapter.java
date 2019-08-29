package com.example.capentory_client.viewmodels.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capentory_client.R;
import com.example.capentory_client.models.MergedItem;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {


    private List<MergedItem> mergedItems = new ArrayList<>();
    private ItemClickListener itemClickListener;

    public RecyclerViewAdapter(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void fill(List<MergedItem> mergedItems) {
        this.mergedItems = mergedItems;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_actualitem, parent, false);
        return new ViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.anlage_textview.setText(mergedItems.get(position).getAnlageNummer());
        holder.anlage_bez_textview.setText(mergedItems.get(position).getDescription());
    }

    @Override
    public int getItemCount() {
        return mergedItems.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView anlage_textview;
        TextView anlage_bez_textview;
        RelativeLayout actualitem_container;
        ItemClickListener itemClickListener;

        public ViewHolder(View itemView, ItemClickListener itemClickListener) {
            super(itemView);
            anlage_textview = itemView.findViewById(R.id.anlage_textview);
            anlage_bez_textview = itemView.findViewById(R.id.anlage_bez_textview);
            actualitem_container = itemView.findViewById(R.id.actualitem_container);

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
}