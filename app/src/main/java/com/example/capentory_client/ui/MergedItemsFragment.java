package com.example.capentory_client.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.capentory_client.R;
import com.example.capentory_client.androidutility.ToastUtility;
import com.example.capentory_client.models.MergedItem;
import com.example.capentory_client.ui.scanactivities.ScanBarcodeActivity;
import com.example.capentory_client.viewmodels.ItemFragmentViewModel;
import com.example.capentory_client.viewmodels.ViewModelProviderFactory;
import com.example.capentory_client.viewmodels.adapter.RecyclerViewAdapter;
import com.example.capentory_client.viewmodels.sharedviewmodels.IaDSharedViewModel;
import com.example.capentory_client.viewmodels.sharedviewmodels.RaISharedViewModel;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;
import com.google.android.gms.common.api.CommonStatusCodes;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class MergedItemsFragment extends DaggerFragment implements RecyclerViewAdapter.ItemClickListener {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ItemFragmentViewModel itemFragmentViewModel;
    private IaDSharedViewModel iaDSharedViewModel;


    @Inject
    ViewModelProviderFactory providerFactory;

    public MergedItemsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mergeditems, container, false);
        recyclerView = view.findViewById(R.id.recyclerv_view);
        progressBar = view.findViewById(R.id.progress_bar_fragment_mergeditems);
        progressBar.bringToFront();
        final SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_fragment_mergeditems);
        final TextView currentRoomTextView = view.findViewById(R.id.room_number_fragment_actualrooms);
        final RecyclerViewAdapter adapter = getRecyclerViewAdapter();
        iaDSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(IaDSharedViewModel.class);


        final RaISharedViewModel raISharedViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(RaISharedViewModel.class);
        String currentRoomString = Objects.requireNonNull(raISharedViewModel.getCurrentRoom().getValue()).getRoomNumber();
        raISharedViewModel.getCurrentRoom().observe(getViewLifecycleOwner(), currentRoom -> currentRoomTextView.setText(currentRoomString));


        itemFragmentViewModel = ViewModelProviders.of(this, providerFactory).get(ItemFragmentViewModel.class);
        itemFragmentViewModel.fetchItems(currentRoomString);

        itemFragmentViewModel.getMergedItems().observe(getViewLifecycleOwner(), statusAwareMergedItem -> {
            switch (statusAwareMergedItem.getStatus()) {
                case SUCCESS:
                    hideProgressBarAndShowContent();
                    adapter.fill(statusAwareMergedItem.getData());
                    break;
                case ERROR:
                    statusAwareMergedItem.getError().printStackTrace();
                    hideProgressBarAndHideContent();
                    break;
                case FETCHING:
                    displayProgressbarAndHideContent();
                    break;

            }
        });

        swipeRefreshLayout.setOnRefreshListener(
                () -> {
                    itemFragmentViewModel.reloadItems(currentRoomString);
                    swipeRefreshLayout.setRefreshing(false);
                }
        );


        view.findViewById(R.id.scan_item_floatingbtn).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ScanBarcodeActivity.class);
            startActivityForResult(intent, 0);
        });

        return view;

    }

    //
    // After registering the broadcast receiver, the next step (below) is to define it.
    // Here it's done in the MainActivity.java, but also can be handled by a separate class.
    // The logic of extracting the scanned data and displaying it on the screen
    // is executed in its own method (later in the code). Note the use of the
    // extra keys defined in the strings.xml file.
    //
    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            //Bundle b = intent.getExtras();
            //  This is useful for debugging to verify the format of received intents from DataWedge
            //for (String key : b.keySet())
            //{
            //    Log.v(LOG_TAG, key);
            //}

            if (action.equals(getResources().getString(R.string.activity_intent_filter_action))) {
                //  Received a barcode scan
                try {
                    String barcode = intent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_data));
                    launchItemDetailFragmentFromBarcode(barcode);
                    //Log.e("xxxxx", String.valueOf(initiatingIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_label_type))));
                } catch (Exception e) {
                    //  Catch if the UI does not exist when we receive the broadcast
                }
            }
        }
    };

    @NonNull
    private RecyclerViewAdapter getRecyclerViewAdapter() {
        final RecyclerViewAdapter adapter = new RecyclerViewAdapter(this);
        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        return adapter;
    }


    @Override
    public void onItemClick(int position, View v) {
        Navigation.findNavController(v).navigate(R.id.itemDetailFragment);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 0) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    String barcode = data.getStringExtra("barcode");
                    launchItemDetailFragmentFromBarcode(barcode);
                } else {
                    Toast.makeText(getContext(), "Scan ist fehlgeschlagen!", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void launchItemDetailFragmentFromBarcode(String barcode) {
        StatusAwareData<List<MergedItem>> statusAwareData = itemFragmentViewModel.getMergedItems().getValue();
        if (statusAwareData == null) return;
        List<MergedItem> items = statusAwareData.getData();
        if (items == null) return;

        for (MergedItem item : items) {
            if (item.equalsBarcode(barcode)) {
                iaDSharedViewModel.setCurrentItem(item);
                NavHostFragment.findNavController(this).navigate(R.id.itemDetailFragment);
                return;
            }
        }

        ToastUtility.displayCenteredToastMessage(getContext(), "Scanergebnis ist nicht in der Liste!\n" + barcode, Toast.LENGTH_LONG);
    }

    @Override
    public void onPause() {
        super.onPause();
        Objects.requireNonNull(getContext()).unregisterReceiver(myBroadcastReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(getResources().getString(R.string.activity_intent_filter_action));
        Objects.requireNonNull(getContext()).registerReceiver(myBroadcastReceiver, filter);
    }


    private void displayProgressbarAndHideContent() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }


    private void hideProgressBarAndShowContent() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void hideProgressBarAndHideContent() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }


}
