package com.example.capentory_client.ui;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import android.provider.MediaStore;
import android.text.Html;
import android.text.method.LinkMovementMethod;
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
import com.example.capentory_client.androidutility.ToastUtility;
import com.example.capentory_client.models.Attachment;
import com.example.capentory_client.models.MergedItem;
import com.example.capentory_client.repos.AttachmentsRepository;
import com.example.capentory_client.ui.errorhandling.BasicNetworkErrorHandler;
import com.example.capentory_client.viewmodels.AttachmentsViewModel;
import com.example.capentory_client.viewmodels.LoginViewModel;
import com.example.capentory_client.viewmodels.ViewModelProviderFactory;
import com.example.capentory_client.viewmodels.sharedviewmodels.DetailXAttachmentViewModel;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;
import com.google.android.gms.common.api.CommonStatusCodes;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Objects;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class AttachmentsFragment extends NetworkFragment<Attachment, AttachmentsRepository, AttachmentsViewModel> {
    private static final int REQUEST_PERMISSIONS = 100;
    private static final int PICK_FILE_REQUEST = 1;
    private View view;


    public AttachmentsFragment() {
        // Required empty public constructor
    }

    private DetailXAttachmentViewModel detailXAttachmentViewModel;
    @Inject
    ViewModelProviderFactory providerFactory;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_attachments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        detailXAttachmentViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(DetailXAttachmentViewModel.class);
        MergedItem currentItem = detailXAttachmentViewModel.getCurrentItem();


        initWithoutFetch(ViewModelProviders.of(this, providerFactory).get(AttachmentsViewModel.class),
                new BasicNetworkErrorHandler(getContext(), view.findViewById(R.id.item_textview_fragment_attachments)),
                view,
                R.id.progress_bar_fragment_attachment);


        ((TextView) view.findViewById(R.id.barcode_fragment_attachments))
                .setText(Html.fromHtml(
                        getString(R.string.barcode_detailitem_fragment, currentItem.getCheckedDisplayBarcode())));
        ((TextView) view.findViewById(R.id.bezeichnung_fragment_attachments))
                .setText(Html.fromHtml(
                        getString(R.string.bez_detailitem_fragment, currentItem.getCheckedDisplayName())));


        generateGUIforAttachments(currentItem, view);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                detailXAttachmentViewModel.setExitedAttachmentScreen(true);
                NavHostFragment.findNavController(AttachmentsFragment.this).popBackStack();
            }
        });


        view.findViewById(R.id.add_attachment_fragment_attachments).setOnClickListener(v -> showFileChooser());
    }

    private void generateGUIforAttachments(MergedItem currentItem, View view) {
        LinearLayout linearLayout = view.findViewById(R.id.content_fragment_attachments);
        if (currentItem.getAttachments().size() < 1) {
            view.findViewById(R.id.no_attachments_textview_fragment_attachments).setVisibility(View.VISIBLE);
            return;
        }

        linearLayout.removeAllViews();

        int c = 1;
        for (Attachment attachment : currentItem.getAttachments()) {
            c = addHeader(linearLayout, c);

            if (attachment.isPicture()) {

                TextView textView = new TextView(Objects.requireNonNull(getContext()));
                textView.setText(attachment.getDesc());
                textView.setPadding(10, 0, 0, 10);
                textView.setTextColor(Color.parseColor("#838485"));
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
                textView.setMovementMethod(LinkMovementMethod.getInstance());
                textView.setPadding(10, 0, 0, 40);
                linearLayout.addView(textView);
            }


        }
    }

    private int addHeader(LinearLayout linearLayout, int c) {
        View hr = new View(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
        );
        params.setMargins(4, 10, 4, 0);
        hr.setLayoutParams(params);
        hr.setBackgroundColor(Color.parseColor("#D8D8D8"));
        linearLayout.addView(hr);

        TextView header = new TextView(Objects.requireNonNull(getContext()));
        header.setText(getString(R.string.attachment_count_fragment_attachments, c++));
        header.setPadding(10, 50, 0, 10);
        linearLayout.addView(header);
        return c;
    }


    @Override
    protected void handleSuccess(StatusAwareData<Attachment> statusAwareData) {
        super.handleSuccess(statusAwareData);
        detailXAttachmentViewModel.getCurrentItem().addAttachment(statusAwareData.getData());
        generateGUIforAttachments(detailXAttachmentViewModel.getCurrentItem(), view);
    }


    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    PICK_FILE_REQUEST);
        } catch (android.content.ActivityNotFoundException ex) {
          /*  Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();*/
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_FILE_REQUEST:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    if (uri == null) return;
                    File file = new File(getPath(uri));


                    Log.e("XXX", "File Uri: " + uri.toString());
                    // Get the path
                    String path = null;
                    try {
                        path = getPath(getContext(), uri);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    Log.e("XXX", "File Path: " + path);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public String getPath(Uri uri)
    {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = Objects.requireNonNull(getContext()).getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index =             cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s=cursor.getString(column_index);
        cursor.close();
        return s;
    }

    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
}
