package com.capentory.capentory_client.viewmodels.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.capentory.capentory_client.R;
import com.capentory.capentory_client.models.MergedItem;
import com.capentory.capentory_client.models.RecyclerviewItem;
import com.capentory.capentory_client.models.Room;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<RecyclerviewItem> items = new ArrayList<>();
    private List<RecyclerviewItem> itemsFull;
    private List<RecyclerviewItem> collapsedItems = new ArrayList<>();
    private ItemClickListener itemClickListener;
    private static final int[] SUB_HEADER_FONT_SIZES = new int[]{18, 16, 14, 12};

    public RecyclerViewAdapter(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void fill(List<RecyclerviewItem> items) {
        items = new ArrayList<>(items);

        for (int i = 0; i < items.size(); i++) {
            // User already now top level room, no need to display him
            if (items.get(i).isTopLevelRoom()) {
                items.remove(i--);

            } else {
                // Remove everything that the user already collapsed before leaving the screen
                if (!items.get(i).isExpanded()) {
                    if (items.get(i) instanceof Room) {
                        Room room = (Room) items.get(i);

                        // However, First Header should not be removed
                        if (!room.isFirstHeaderShouldNotBeRemoved()) {
                            items.remove(i--);
                        }
                    } else items.remove(i--);

                }
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
                vhItem.counter_textview.setText(vhItem.counter_textview.getContext().getString(R.string.found_times_recyclerview_adapter
                        , item.getTimesFoundCurrent(), item.getTimesFoundLast()));
                vhItem.optional_counter_container.setVisibility(View.VISIBLE);
            } else vhItem.optional_counter_container.setVisibility(View.GONE);
        } else if (holder instanceof VHHeader) {
            VHHeader vhHeader = (VHHeader) holder;
            Room room = (Room) items.get(position);

            vhHeader.subRoom_textview.setTextSize(
                    SUB_HEADER_FONT_SIZES[Math.min(room.getDepth() - 1, SUB_HEADER_FONT_SIZES.length - 1)]);

            vhHeader.subRoom_textview.setText(
                    vhHeader.subRoom_textview.getContext().getString(R.string.recycler_view_adapter_subroom_text,
                            room.getDisplayedRoomDescription()));

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

    public void handleCollapseAndExpand(int position) {

        Room room = (Room) getItem(position);
        if (room.isExpanded()) {
            ArrayList<RecyclerviewItem> itemsToRemove = new ArrayList<>();
            itemsToRemove = getItemsToRemove(room, itemsToRemove, 0);

            if (itemsToRemove.size() > 0) {
                collapsedItems.addAll(itemsToRemove);
                items.removeAll(itemsToRemove);
                notifyItemRangeRemoved(position + 1, itemsToRemove.size());
            }
        } else {
            ArrayList<RecyclerviewItem> itemsToInsert = new ArrayList<>();
            itemsToInsert = getItemsToAdd(room, itemsToInsert, 0);

            if (itemsToInsert.size() > 0) {
                collapsedItems.removeAll(itemsToInsert);
                items.addAll(position + 1, itemsToInsert);
                notifyItemRangeInserted(position + 1, itemsToInsert.size());
            }
        }

    }

    private ArrayList<RecyclerviewItem> getItemsToRemove(Room room, ArrayList<RecyclerviewItem> itemsToRemove, int depth) {

        if (depth > 0) {
            // Always remove the header
            itemsToRemove.add(room);
            room.setFirstHeaderShouldNotBeRemoved(false);
        } else {
            //(but not the first one)
            room.setFirstHeaderShouldNotBeRemoved(true);
        }

        for (MergedItem mergedItem : room.getMergedItems()) {
            // Remove the item if its room is expanded
            itemsToRemove.add(mergedItem);
            mergedItem.setExpanded(false);
        }

        for (Room subRoom : room.getSubRooms()) {
            itemsToRemove = (getItemsToRemove(subRoom, itemsToRemove, ++depth));
        }

        // Mark the room as collapsed
        room.setExpanded(false);

        return itemsToRemove;
    }


    private ArrayList<RecyclerviewItem> getItemsToAdd(Room room, ArrayList<RecyclerviewItem> itemsToInserts, int depth) {

        // Dont add a already expanded room
        if (depth > 0 /*&& !room.isExpanded()*/) {
            itemsToInserts.add(room);
        }

        for (MergedItem mergedItem : room.getMergedItems()) {
            // Add the item if its room is collapsed
            itemsToInserts.add(mergedItem);
            mergedItem.setExpanded(true);
        }

        for (Room subRoom : room.getSubRooms()) {
            itemsToInserts = (getItemsToAdd(subRoom, itemsToInserts, ++depth));
        }

        // Mark the room as expanded
        room.setExpanded(true);

        return itemsToInserts;
    }


    class VHHeader extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView subRoom_textview;
        View room_container;


        VHHeader(View itemView) {
            super(itemView);
            this.subRoom_textview = itemView.findViewById(R.id.subroom_textview);
            this.room_container = itemView.findViewById(R.id.room_container);
            this.room_container.setClickable(true);
            this.room_container.setOnClickListener(this);
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
                        if (recyclerviewItem instanceof Room && recyclerviewItem.isExpanded()) {
                            addItemsOfSubrooms(filteredList, (Room) recyclerviewItem);
                        } else
                            // Add the item itself a search result
                            if (!filteredList.contains(recyclerviewItem))
                                filteredList.add(recyclerviewItem);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        private void addItemsOfSubrooms(List<RecyclerviewItem> filteredList, Room room) {
            if (!room.isExpanded()) return;

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

            List<RecyclerviewItem> itemsToPush = new ArrayList<>();

            if (results.values == null) return;
            for (RecyclerviewItem filterItem : (Collection<? extends RecyclerviewItem>) results.values) {
                // Filter results may include item that has already been collapsed,
                // we dont want to include a item which may appear twice when the user expands the subroom again
                if (!collapsedItems.contains(filterItem)) {
                    itemsToPush.add(filterItem);
                }

            }

            items.clear();
            items.addAll(itemsToPush);
            notifyDataSetChanged();

/*
            items.clear();
            if (results.values == null) return;
            items.addAll((Collection<? extends MergedItem>) results.values);
            notifyDataSetChanged();*/
        }
    };
}