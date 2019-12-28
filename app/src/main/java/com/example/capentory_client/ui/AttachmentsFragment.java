package com.example.capentory_client.ui;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.capentory_client.R;
import com.example.capentory_client.androidutility.PreferenceUtility;
import com.example.capentory_client.models.Attachment;
import com.example.capentory_client.models.MergedItem;
import com.example.capentory_client.repos.AttachmentsRepository;
import com.example.capentory_client.repos.NetworkRepository;
import com.example.capentory_client.ui.errorhandling.BasicNetworkErrorHandler;
import com.example.capentory_client.viewmodels.AttachmentsViewModel;
import com.example.capentory_client.viewmodels.ViewModelProviderFactory;
import com.example.capentory_client.viewmodels.adapter.AttachmentRecyclerViewAdapter;
import com.example.capentory_client.viewmodels.sharedviewmodels.DetailXAttachmentViewModel;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class AttachmentsFragment extends NetworkFragment<Attachment, AttachmentsRepository, AttachmentsViewModel> {
    private static final int REQUEST_PERMISSIONS = 100;
    private static final int PICK_FILE_REQUEST = 1;
    private View view;
    private RecyclerView recyclerView;
    private AttachmentRecyclerViewAdapter adapter;
    private TextView noAttachmentsTextView;


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
        recyclerView = view.findViewById(R.id.recycler_view_fragment_attachments);
        adapter = getRecyclerViewAdapter();
        noAttachmentsTextView = view.findViewById(R.id.no_attachments_textview_fragment_attachments);


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


        displayAttachments(currentItem);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                detailXAttachmentViewModel.setExitedAttachmentScreen(true);
                NavHostFragment.findNavController(AttachmentsFragment.this).popBackStack();
            }
        });


        view.findViewById(R.id.add_attachment_fragment_attachments).setOnClickListener(v -> showFileChooser());
    }

    private void displayAttachments(MergedItem currentItem) {
        recyclerView.setVisibility(View.VISIBLE);
        if (currentItem.getAttachments().size() < 1) {
            noAttachmentsTextView.setVisibility(View.VISIBLE);
            noAttachmentsTextView.setText(getString(R.string.no_attachments_fragment_attachments));
        } else {
            noAttachmentsTextView.setVisibility(View.GONE);
        }

        adapter.setAttachments(currentItem.getAttachments());
    }

    @Override
    protected void refresh() {
        displayAttachments(detailXAttachmentViewModel.getCurrentItem());
    }

    private void generateGUIforAttachments(MergedItem currentItem, View view) {
        LinearLayout linearLayout = null;
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
                textView.setText(attachment.getDisplayDescription(getContext()));
                textView.setPadding(10, 0, 0, 16);
                textView.setTextColor(Color.parseColor("#838485"));
                linearLayout.addView(textView);


                ImageView imageView = new ImageView(getContext());
                imageView.setPadding(8, 16, 8, 28);

                /*LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT );
                params.gravity = Gravity.CENTER;
                imageView.setLayoutParams(params);*/
                imageView.setMaxHeight(500);

                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);


                Glide.with(this)
                        //.setDefaultRequestOptions(requestOptions)
                        .load(attachment.getUrl(getContext()))
                        .error(R.drawable.ic_signal_wifi_off_black_24dp)
                        .into(imageView);


                linearLayout.addView(imageView);
            } else {
                TextView textView = new TextView(Objects.requireNonNull(getContext()));


                //                 textView.setText(Html.fromHtml(getString(R.string.icons_legal_notice)));
                textView.setText(Html.fromHtml(getString(R.string.url_fragment_attachment, attachment.getUrl(getContext()), attachment.getDisplayDescription(getContext()))));
                textView.setClickable(true);
                textView.setMovementMethod(LinkMovementMethod.getInstance());
                textView.setPadding(10, 0, 0, 40);
                linearLayout.addView(textView);
            }


        }


    }

    @NonNull
    private AttachmentRecyclerViewAdapter getRecyclerViewAdapter() {
        final AttachmentRecyclerViewAdapter adapter = new AttachmentRecyclerViewAdapter();
        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        return adapter;
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
        displayAttachments(detailXAttachmentViewModel.getCurrentItem());
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
        if (requestCode == PICK_FILE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                if (uri == null) return;
                /* File file = new File(getPath(uri));*/

           /*     @Override
                public Map<String, String> getHeaders() {
                    if (sendToken) {
                        HashMap<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", "Token "
                                + PreferenceUtility.getToken(context));
                        headers.put("Connection", "close");
                        return headers;
                    }

                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }*/
                try {

                    URL serverUrl = null;
                    serverUrl = new URL(NetworkRepository.getUrl(getContext(), false, MainActivity.getSerializer(getContext()).getAttachmentUrl()));
                    HttpURLConnection connection = (HttpURLConnection) serverUrl.openConnection();

                    connection.setRequestProperty("Authorization", "Token "
                            + PreferenceUtility.getToken(getContext()));

                    String boundary = UUID.randomUUID().toString();
                    connection.setRequestMethod("POST");

                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                    DataOutputStream request = new DataOutputStream(connection.getOutputStream());

                    request.writeBytes("--" + boundary + "\r\n");
                    request.writeBytes("Content-Disposition: form-data; name=\"description\"\r\n\r\n");
                    request.writeBytes("lol" + "\r\n");

                    request.writeBytes("--" + boundary + "\r\n");
                    request.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + new File(" " + uri).getName()
                            + "\"\r\n\r\n");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        request.write(Files.readAllBytes(Paths.get(uri.getPath())));
                    }
                    request.writeBytes("\r\n");

                    request.writeBytes("--" + boundary + "--\r\n");
                    request.flush();
                    int respCode = connection.getResponseCode();

                    switch (respCode) {
                        case 200:
                            //all went ok - read response
                            Log.e("XXX", connection.getResponseMessage());
                            break;
                        case 301:
                        case 302:
                        case 307:
                            //handle redirect - for example, re-post to the new location
                            break;
                        //do something sensible
                    }
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


              /*  Log.e("XXX", "File Uri: " + uri.toString());
                // Get the path
                String path = null;
                try {
                    path = getPath(getContext(), uri);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                Log.e("XXX", "File Path: " + path);*/
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = Objects.requireNonNull(getContext()).getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s = cursor.getString(column_index);
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
