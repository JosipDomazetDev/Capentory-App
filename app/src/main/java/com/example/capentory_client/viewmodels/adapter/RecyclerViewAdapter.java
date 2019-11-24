package com.example.capentory_client.viewmodels.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capentory_client.R;
import com.example.capentory_client.models.MergedItem;
import com.example.capentory_client.models.RecyclerviewItem;
import com.example.capentory_client.models.Room;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<RecyclerviewItem> items = new ArrayList<>();
    private List<RecyclerviewItem> itemsFull;
    private ItemClickListener itemClickListener;
    private static final int[] SUB_HEADER_FONT_SIZES = new int[]{18, 16, 14, 12};

    public RecyclerViewAdapter(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void fill(List<RecyclerviewItem> items) {
        for (int i = 0; i < items.size(); i++) {
            // User already now top level room, no need to display him
            if (items.get(i).isTopLevelRoom()) {
                items.remove(i);
                break;
            }
        }
        this.items = items;
        this.itemsFull = new ArrayList<>(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View v = inflater.inflate(R.layout.recyclerview_room_row, parent, false);
            return new VHHeader(v);
        } else {
            View v = inflater.inflate(R.layout.recyclerview_item_row, parent, false);
            return new VHItem(v, itemClickListener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof VHItem) {
            VHItem vhItem = (VHItem) holder;
            MergedItem item = (MergedItem) items.get(position);

            vhItem.anlage_textview.setText(item.getCheckedDisplayBarcode());
            vhItem.anlage_bez_textview.setText(item.getCheckedDisplayName());

            if (item.getTimesFoundLast() > 1) {
                vhItem.counter_textview.setText(String.format("Gefunden: %1$s", (item.getTimesFoundCurrent() + "/" + item.getTimesFoundLast())));
                vhItem.optional_counter_container.setVisibility(View.VISIBLE);
            } else vhItem.optional_counter_container.setVisibility(View.GONE);
        } else if (holder instanceof VHHeader) {
            VHHeader vhHeader = (VHHeader) holder;
            Room room = (Room) items.get(position);

            vhHeader.subRoom_textview.setTextSize(
                    SUB_HEADER_FONT_SIZES[Math.min(room.getDepth() - 1, SUB_HEADER_FONT_SIZES.length - 1)]);

            vhHeader.subRoom_textview.setText(String.format(
                    vhHeader.subRoom_textview.getContext().getString(R.string.recycler_view_adapter_subroom_text),
                    room.getDisplayedNumber()));

        }
    }

    private boolean isPositionHeader(int position) {
        return items.get(position) instanceof Room;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;
        return TYPE_ITEM;
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    public RecyclerviewItem getItem(int position) {
        return items.get(position);
    }

    public void handleCollapseAndExpand(int position, View v) {
        Room room = (Room) getItem(position);
        if (room.isExpanded()) {
            int amountOfRemovedItems = 0;
            amountOfRemovedItems = getAmountOfRemovedItems(room, amountOfRemovedItems);
            notifyItemRangeRemoved(position + 1, amountOfRemovedItems);
            room.setExpanded(false);
        } else {
            int amountOfAddedItems = 0;
            amountOfAddedItems = getAmountOfAddedItems(room, amountOfAddedItems);
            notifyItemRangeInserted(position + 1, amountOfAddedItems);
            room.setExpanded(true);
        }
    }

    private int getAmountOfAddedItems(Room room, int amountOfAddedItems) {
        if (amountOfAddedItems != 0) {
            if (items.add(room)) {
                amountOfAddedItems++;
            }
        }

        for (MergedItem mergedItem : room.getMergedItems())
            if (items.add(mergedItem)) {
                amountOfAddedItems++;
            }

        for (Room subRoom : room.getSubRooms()) {
            amountOfAddedItems += getAmountOfAddedItems(subRoom, 0);
        }

        return amountOfAddedItems;
    }

    private int getAmountOfRemovedItems(Room room, int amountOfRemovedItems) {
        // Also remove other headers
        if (amountOfRemovedItems != 0) {
            if (items.remove(room)) {
                amountOfRemovedItems++;
            }
        }

        for (MergedItem mergedItem : room.getMergedItems()) {
            if (items.remove(mergedItem)) {
                amountOfRemovedItems++;
            }
        }

        for (Room subRoom : room.getSubRooms()) {
            amountOfRemovedItems += getAmountOfRemovedItems(subRoom, 0);
        }

        return amountOfRemovedItems;
    }


    class VHHeader extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView subRoom_textview;
        View room_container;


        VHHeader(View itemView) {
            super(itemView);
            this.subRoom_textview = itemView.findViewById(R.id.subroom_textview);
            this.subRoom_textview.setClickable(true);
            this.subRoom_textview.setOnClickListener(this);

            this.room_container = itemView.findViewById(R.id.room_container);
        }

        @Override
        public void onClick(View v) {
            itemClickListener.onItemClick(getAdapterPosition(), v);
        }
    }


    class VHItem extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView anlage_textview;
        TextView anlage_bez_textview;
        TextView counter_textview;
        View optional_counter_container;
        ItemClickListener itemClickListener;

        VHItem(View itemView, ItemClickListener itemClickListener) {
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
            List<RecyclerviewItem> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(itemsFull);
            } else {
                String filter = constraint.toString();

                for (RecyclerviewItem recyclerviewItem : itemsFull) {
                    if (recyclerviewItem.applySearchBarFilter(filter)) {

                        // Add all remaining items of the room as search result
                        if (recyclerviewItem instanceof Room) {
                            addItemsOfSubrooms(filteredList, (Room) recyclerviewItem);
                        } else
                            // Add the item itself a search result
                            filteredList.add(recyclerviewItem);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        private void addItemsOfSubrooms(List<RecyclerviewItem> filteredList, Room room) {
            filteredList.add(room);
            for (MergedItem mergedItem : room.getMergedItems()) {
                if (itemsFull.contains(mergedItem) && !filteredList.contains(mergedItem)) {
                    filteredList.add(mergedItem);
                }
            }
            for (Room subRoom : room.getSubRooms()) {
                addItemsOfSubrooms(filteredList, subRoom);
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            items.clear();
            if (results.values == null) return;
            items.addAll((Collection<? extends MergedItem>) results.values);
            notifyDataSetChanged();
        }
    };
}