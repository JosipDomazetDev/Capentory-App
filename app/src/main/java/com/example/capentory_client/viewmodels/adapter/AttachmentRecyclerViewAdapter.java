package com.example.capentory_client.viewmodels.adapter;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.capentory_client.R;
import com.example.capentory_client.models.Attachment;

import java.util.ArrayList;
import java.util.List;

public class AttachmentRecyclerViewAdapter extends RecyclerView.Adapter<AttachmentRecyclerViewAdapter.ViewHolder> {
    private List<Attachment> attachments = new ArrayList<>();

    @NonNull
    @Override
    public AttachmentRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_attachment_row, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AttachmentRecyclerViewAdapter.ViewHolder holder, int position) {
        Attachment currentAttachment = attachments.get(position);

        holder.header.setText(
                holder.header.getContext().
                        getString(R.string.attachment_count_fragment_attachments, position + 1));


        if (currentAttachment.isPicture()) {
            holder.desc.setText(currentAttachment.getDisplayDescription(holder.desc.getContext()));

            Glide.with(holder.imageView.getContext())
                    //.setDefaultRequestOptions(requestOptions)
                    .load(currentAttachment.getUrl(holder.imageView.getContext()))
                    .error(R.drawable.ic_signal_wifi_off_black_24dp)
                    .into(holder.imageView);
        } else {
            Context ctx = holder.desc.getContext();
            holder.desc.setText
                    (Html.fromHtml(
                            ctx.getString(R.string.url_fragment_attachment,
                                    currentAttachment.getUrl(ctx),
                                    currentAttachment.getDisplayDescription(ctx))));
            holder.desc.setClickable(true);
            holder.desc.setMovementMethod(LinkMovementMethod.getInstance());

            holder.imageView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return attachments.size();
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView header;
        private TextView desc;
        private ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.header_recyclerview_attachment);
            desc = itemView.findViewById(R.id.desc_recyclerview_attachment);
            imageView = itemView.findViewById(R.id.imageView_recyclerview_attachment);
        }
    }
}