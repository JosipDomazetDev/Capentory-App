package com.example.capentory_client.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.capentory_client.R;
import com.example.capentory_client.androidutility.ToastUtility;
import com.example.capentory_client.models.MergedItem;
import com.example.capentory_client.ui.errorhandling.BasicNetworkErrorHandler;
import com.example.capentory_client.ui.scanactivities.ScanBarcodeActivity;
import com.example.capentory_client.viewmodels.MergedItemFragmentViewModel;
import com.example.capentory_client.viewmodels.ViewModelProviderFactory;
import com.example.capentory_client.viewmodels.adapter.RecyclerViewAdapter;
import com.example.capentory_client.viewmodels.sharedviewmodels.ItemxDetailSharedViewModel;
import com.example.capentory_client.viewmodels.sharedviewmodels.RoomxItemSharedViewModel;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
    private MergedItemFragmentViewModel mergedItemFragmentViewModel;
    private ItemxDetailSharedViewModel itemxDetailSharedViewModel;
    private RoomxItemSharedViewModel roomxItemSharedViewModel;
    private RecyclerViewAdapter adapter;


    @Inject
    ViewModelProviderFactory providerFactory;

    public MergedItemsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem searchItem = menu.findItem(R.id.search);
        searchItem.setVisible(true);

        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mergeditems, container, false);
        final SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_fragment_mergeditems);
        final FloatingActionButton finishRoom = view.findViewById(R.id.finish_room_floatingbtn);
        final FloatingActionButton addItem = view.findViewById(R.id.add_item_floatingbtn);
        final TextView currentRoomTextView = view.findViewById(R.id.room_number_fragment_mergeditems);
        final TextView noItemTextView = view.findViewById(R.id.no_items_fragment_mergeditems);
        recyclerView = view.findViewById(R.id.recyclerv_view);
        progressBar = view.findViewById(R.id.progress_bar_fragment_mergeditems);
        progressBar.bringToFront();
        adapter = getRecyclerViewAdapter();
        itemxDetailSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(ItemxDetailSharedViewModel.class);
        BasicNetworkErrorHandler basicNetworkErrorHandler = new BasicNetworkErrorHandler(getContext(), view.findViewById(R.id.room_number_label_fragment_mergeditems));


        roomxItemSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(RoomxItemSharedViewModel.class);
        String currentRoomString = Objects.requireNonNull(roomxItemSharedViewModel.getCurrentRoom().getValue()).getRoomNumber();
        roomxItemSharedViewModel.getCurrentRoom().observe(getViewLifecycleOwner(), currentRoom -> currentRoomTextView.setText(currentRoomString));


        mergedItemFragmentViewModel = ViewModelProviders.of(this, providerFactory).get(MergedItemFragmentViewModel.class);
        mergedItemFragmentViewModel.fetchData(currentRoomString);


        mergedItemFragmentViewModel.getMergedItems().observe(getViewLifecycleOwner(), statusAwareMergedItem -> {
            noItemTextView.setVisibility(View.GONE);
            switch (statusAwareMergedItem.getStatus()) {
                case SUCCESS:
                    handleDisplayOfData(adapter, statusAwareMergedItem, noItemTextView);
                    break;
                case ERROR:
                    basicNetworkErrorHandler.displayTextViewMessage(statusAwareMergedItem.getError());
                    statusAwareMergedItem.getError().printStackTrace();
                    hideProgressBarAndHideContent();
                    break;
                case FETCHING:
                    displayProgressbarAndHideContent();
                    break;

            }
            if (statusAwareMergedItem.getStatus() != StatusAwareData.State.ERROR)
                basicNetworkErrorHandler.reset();

        });

        swipeRefreshLayout.setOnRefreshListener(
                () -> {
                    mergedItemFragmentViewModel.reloadItems(currentRoomString);
                    swipeRefreshLayout.setRefreshing(false);
                }
        );


        view.findViewById(R.id.scan_item_floatingbtn).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ScanBarcodeActivity.class);
            startActivityForResult(intent, 0);
        });


        finishRoom.setOnClickListener(v -> {
            roomxItemSharedViewModel.setCurrentRoomValidated(true);
            new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                    .setTitle("Alles erledigt?")
                    .setMessage("Wollen Sie die Validierung für diesen Raum beenden?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> handleFinishRoom())
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        });

        addItem.setOnClickListener(v -> {
            itemxDetailSharedViewModel.setCurrentItem(null);
            NavHostFragment.findNavController(this).navigate(R.id.action_itemsFragment_to_itemDetailFragment);
        });

        itemxDetailSharedViewModel.getCurrentItemValidated().observe(getViewLifecycleOwner(), b -> {
            if (b) {
                mergedItemFragmentViewModel.removeItem(itemxDetailSharedViewModel.getCurrentItem().getValue());
                itemxDetailSharedViewModel.setCurrentItemValidated(false);
            }
        });

        return view;
    }

    private void handleFinishRoom() {
        roomxItemSharedViewModel.setCurrentRoomValidated(true);
        NavHostFragment.findNavController(this).popBackStack();
    }

    private void handleDisplayOfData(RecyclerViewAdapter adapter, StatusAwareData<List<MergedItem>> statusAwareMergedItem, TextView textView) {
        if (adapter == null) return;
        hideProgressBarAndShowContent();
        List<MergedItem> mergedItems = statusAwareMergedItem.getData();
        if (mergedItems == null) return;

        if (mergedItems.isEmpty()) {
            textView.setVisibility(View.VISIBLE);
            textView.setText("In diesem Raum befinden sich keine Items!");
        }
        adapter.fill(mergedItems);
    }

    //
    // After registering the broadcast receiver, the next step (below) is to define it.
    // Here it'statusAwareLiveData done in the MainActivity.java, but also can be handled by a separate class.
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
        itemxDetailSharedViewModel.setCurrentItem(adapter.getItem(position));
        Navigation.findNavController(v).navigate(R.id.action_itemsFragment_to_itemDetailFragment);
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
        StatusAwareData<List<MergedItem>> statusAwareData = mergedItemFragmentViewModel.getMergedItems().getValue();
        if (statusAwareData == null) return;
        List<MergedItem> items = statusAwareData.getData();
        if (items == null) return;

        for (MergedItem item : items) {
            if (item.equalsBarcode(barcode)) {
                itemxDetailSharedViewModel.setCurrentItem(item);
                NavHostFragment.findNavController(this).navigate(R.id.action_itemsFragment_to_itemDetailFragment);
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


    @Override
    public void onDetach() {
        super.onDetach();
    }

}
