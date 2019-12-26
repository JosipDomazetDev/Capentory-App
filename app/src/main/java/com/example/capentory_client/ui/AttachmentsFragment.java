package com.example.capentory_client.ui;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.capentory_client.R;
import com.example.capentory_client.models.Attachment;
import com.example.capentory_client.models.MergedItem;
import com.example.capentory_client.viewmodels.sharedviewmodels.DetailXAttachmentViewModel;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;


/**
 * A simple {@link Fragment} subclass.
 */
public class AttachmentsFragment extends Fragment {


    public AttachmentsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_attachments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DetailXAttachmentViewModel detailXAttachmentViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(DetailXAttachmentViewModel.class);
        MergedItem currentItem = detailXAttachmentViewModel.getCurrentItem();


        ((TextView) view.findViewById(R.id.barcode_fragment_attachments))
                .setText(Html.fromHtml(
                        getString(R.string.barcode_detailitem_fragment, currentItem.getCheckedDisplayBarcode())));
        ((TextView) view.findViewById(R.id.bezeichnung_fragment_attachments))
                .setText(Html.fromHtml(
                        getString(R.string.bez_detailitem_fragment, currentItem.getCheckedDisplayName())));


        generateGUIforAttachments(currentItem, view);
    }

    private void generateGUIforAttachments(MergedItem currentItem, View view) {
        LinearLayout linearLayout = view.findViewById(R.id.content_fragment_attachments);
        if (currentItem.getAttachments().size() < 1) {
            view.findViewById(R.id.no_attachments_textview_fragment_attachments).setVisibility(View.VISIBLE);
            return;
        }

        linearLayout.removeAllViews();

        for (Attachment attachment : currentItem.getAttachments()) {
            if (attachment.isPicture()) {
                TextView textView = new TextView(Objects.requireNonNull(getContext()));
                textView.setText(attachment.getDesc());
                textView.setPadding(10, 0, 0, 10);
                linearLayout.addView(textView);


                ImageView imageView = new ImageView(getContext());
                imageView.setPadding(8, 8, 8, 48);
                imageView.setMaxWidth(500);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER;
                imageView.setLayoutParams(params);

                CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(Objects.requireNonNull(getContext()));
                circularProgressDrawable.setStrokeWidth(5);
                circularProgressDrawable.setCenterRadius(5);
                circularProgressDrawable.start();

                Glide.with(this)
                        .load(attachment.getUrl())
                        .centerCrop()
                        //.placeholder(circularProgressDrawable)
                        .into(imageView);

                linearLayout.addView(imageView);

            } else {
                TextView textView = new TextView(Objects.requireNonNull(getContext()));
                String description = attachment.getDesc();
                if (description.isEmpty()) {
                    description = getString(R.string.empty_desc_fragment_attachment);
                }

                //                 textView.setText(Html.fromHtml(getString(R.string.icons_legal_notice)));
                textView.setText(Html.fromHtml(getString(R.string.url_fragment_attachment, attachment.getUrl(), description)));
                textView.setClickable(true);
                Log.e("XXX",getString(R.string.url_fragment_attachment, attachment.getUrl(), description));
                Log.e("XXX",getString(R.string.icons_legal_notice));
                textView.setPadding(10, 0, 0, 40);
                linearLayout.addView(textView);
            }
        }
    }
}
