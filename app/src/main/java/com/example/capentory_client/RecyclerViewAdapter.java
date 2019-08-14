package com.example.capentory_client;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private ArrayList<String> anlage;
    private ArrayList<String> anlage_bez;
    private Context context;

    public RecyclerViewAdapter(ArrayList<String> anlage, ArrayList<String> anlage_bez, Context context) {
        this.anlage = anlage;
        this.anlage_bez = anlage_bez;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_actualitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.anlage_textview.setText(anlage.get(position));
        holder.anlage_bez_textview.setText(anlage_bez.get(position));

        holder.actualitem_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, anlage.get(position), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return anlage.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView anlage_textview;
        TextView anlage_bez_textview;
        RelativeLayout actualitem_container;

        public ViewHolder(View itemView) {
            super(itemView);
            anlage_textview = itemView.findViewById(R.id.anlage_bez_textview);
            anlage_bez_textview = itemView.findViewById(R.id.anlage_textview);
            actualitem_container = itemView.findViewById(R.id.actualitem_container);
        }
    }
}