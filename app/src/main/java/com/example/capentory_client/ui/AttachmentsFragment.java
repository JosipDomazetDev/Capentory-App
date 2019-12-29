package com.example.capentory_client.ui;


import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capentory_client.R;
import com.example.capentory_client.androidutility.FileUtility;
import com.example.capentory_client.androidutility.PreferenceUtility;
import com.example.capentory_client.androidutility.ToastUtility;
import com.example.capentory_client.models.Attachment;
import com.example.capentory_client.models.MergedItem;
import com.example.capentory_client.models.SerializerEntry;
import com.example.capentory_client.repos.AttachmentsRepository;
import com.example.capentory_client.repos.NetworkRepository;
import com.example.capentory_client.ui.errorhandling.BasicNetworkErrorHandler;
import com.example.capentory_client.viewmodels.AttachmentsViewModel;
import com.example.capentory_client.viewmodels.ViewModelProviderFactory;
import com.example.capentory_client.viewmodels.adapter.AttachmentRecyclerViewAdapter;
import com.example.capentory_client.viewmodels.sharedviewmodels.DetailXAttachmentViewModel;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Objects;

import javax.inject.Inject;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class AttachmentsFragment extends NetworkFragment<Attachment, AttachmentsRepository, AttachmentsViewModel> {
    private static final int REQUEST_PERMISSIONS = 100;
    private static final int PICK_FILE_REQUEST = 1;
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


        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);
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

    @NonNull
    private AttachmentRecyclerViewAdapter getRecyclerViewAdapter() {
        final AttachmentRecyclerViewAdapter adapter = new AttachmentRecyclerViewAdapter();
        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        return adapter;
    }


    @Override
    protected void handleSuccess(StatusAwareData<Attachment> statusAwareData) {
        super.handleSuccess(statusAwareData);
        Attachment currentAttachment = statusAwareData.getData();
        if (!detailXAttachmentViewModel.getCurrentItem()
                .getAttachments()
                .contains(currentAttachment)) {

            // This method gets called on configuraiton changes too, therefore only add the item once
            detailXAttachmentViewModel.getCurrentItem().addAttachment(currentAttachment);
        }

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
            ToastUtility.displayCenteredToastMessage(getContext(), getString(R.string.file_manager_fragment_attachments), Toast.LENGTH_LONG);
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_FILE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                if (uri == null) return;

                fetchManually(FileUtility.getPath(getContext(), uri));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
