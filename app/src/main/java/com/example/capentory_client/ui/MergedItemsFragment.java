package com.example.capentory_client.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
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

import com.example.capentory_client.R;
import com.example.capentory_client.androidutility.PreferenceUtility;
import com.example.capentory_client.models.MergedItem;
import com.example.capentory_client.repos.MergedItemsRepository;
import com.example.capentory_client.ui.errorhandling.BasicNetworkErrorHandler;
import com.example.capentory_client.ui.scanactivities.ScanBarcodeActivity;
import com.example.capentory_client.viewmodels.MergedItemViewModel;
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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class MergedItemsFragment extends NetworkFragment<List<MergedItem>, MergedItemsRepository, MergedItemViewModel> implements RecyclerViewAdapter.ItemClickListener {
    private RecyclerView recyclerView;
    private ItemxDetailSharedViewModel itemxDetailSharedViewModel;
    private RoomxItemSharedViewModel roomxItemSharedViewModel;
    private RecyclerViewAdapter adapter;
    private TextView noItemTextView;
    private String currentRoomString;
    private boolean isKeyboardShowing = false;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mergeditems, container, false);
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final FloatingActionButton finishRoom = view.findViewById(R.id.finish_room_floatingbtn);
        final FloatingActionButton addItem = view.findViewById(R.id.add_item_floatingbtn);
        final TextView currentRoomTextView = view.findViewById(R.id.room_number_fragment_mergeditems);
        noItemTextView = view.findViewById(R.id.no_items_fragment_mergeditems);
        recyclerView = view.findViewById(R.id.recyclerv_view);
        adapter = getRecyclerViewAdapter();
        itemxDetailSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(ItemxDetailSharedViewModel.class);


        roomxItemSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(RoomxItemSharedViewModel.class);
        currentRoomString = Objects.requireNonNull(roomxItemSharedViewModel.getCurrentRoom().getValue()).getRoomNumber();
        String displayRoomString = Objects.requireNonNull(roomxItemSharedViewModel.getCurrentRoom().getValue()).getDisplayedNumber();
        roomxItemSharedViewModel.getCurrentRoom().observe(getViewLifecycleOwner(), currentRoom -> currentRoomTextView.setText(displayRoomString));


        initWithFetch(ViewModelProviders.of(this, providerFactory).get(MergedItemViewModel.class),
                new BasicNetworkErrorHandler(getContext(), view.findViewById(R.id.room_number_label_fragment_mergeditems)),
                view,
                R.id.progress_bar_fragment_mergeditems,
                recyclerView,
                R.id.swipe_refresh_fragment_mergeditems,
                currentRoomString
        );


        view.findViewById(R.id.scan_item_floatingbtn).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ScanBarcodeActivity.class);
            startActivityForResult(intent, 0);
        });


        finishRoom.setOnClickListener(v -> {

            String title;
            String message;
            if (MainActivity.getStocktaking().isEndingStocktaking()) {
                title = "Alles im Raum erledigt?";
                message = "Wollen Sie die Validierung für diesen Raum beenden und die Daten an den Server senden?";
            } else if (networkViewModel.getAmountOfItemsLeft() == 1) {
                title = "Fehlt ein Gegenstand?";
                message = "Wollen Sie die Validierung für diesen Raum beenden und die Daten an den Server senden? Ein Gegenstand wird als fehlend markiert!";
            } else if (networkViewModel.getAmountOfItemsLeft() > 1) {
                title = "Fehlen " + networkViewModel.getAmountOfItemsLeft() + " Gegenstände?";
                message = "Wollen Sie die Validierung für diesen Raum beenden und die Daten an den Server senden? " + networkViewModel.getAmountOfItemsLeft() + " Gegenstände werden als fehlend markiert!";
            } else {
                title = "Alle Items im Raum gescannt?";
                message = "Wollen Sie die Validierung für diesen Raum beenden und die Daten an den Server senden?";
            }

            new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> handleFinishRoom())
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        });

        addItem.setOnClickListener(v -> {
            itemxDetailSharedViewModel.setCurrentItem(MergedItem.createNewEmptyItem());
            NavHostFragment.findNavController(this).navigate(R.id.action_itemsFragment_to_itemDetailFragment);
        });

        itemxDetailSharedViewModel.getValidationEntryForCurrentItem().observe(getViewLifecycleOwner(), validationEntry -> {
            // If it's null this means that user aborted the DetailItemScreen altogether and doesn't want to create a ValidationEntry
            if (validationEntry != null) {
                itemxDetailSharedViewModel.setValidationEntryForCurrentItem(null);
                // This means he pressed the Red Button and wants to remove the Item from the List and mark it as invalid
                if (validationEntry.isCanceledItem()) {
                    networkViewModel.removeItemDirectly(itemxDetailSharedViewModel.getCurrentItem().getValue());
                } else {
                    // This means he pressed the Green Button and wants to add a ValidationEntry
                    networkViewModel.addValidationEntry(validationEntry);
                    networkViewModel.removeItemByFoundCounterIncrease(itemxDetailSharedViewModel.getCurrentItem().getValue());
                }
            }
        });


        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                        .setTitle("Raumeinträge")
                        .setMessage("Wollen Sie die Änderungen für diesen Raum wirklich verwerfen und zum vorherigen Bildschirm zurückkehren?")
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> NavHostFragment.findNavController(MergedItemsFragment.this).popBackStack())
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });


        // https://stackoverflow.com/questions/4745988/how-do-i-detect-if-software-keyboard-is-visible-on-android-device
        View root = view.findViewById(R.id.swipe_refresh_fragment_mergeditems);
        root.getViewTreeObserver().addOnGlobalLayoutListener(
                () -> {

                    Rect r = new Rect();
                    root.getWindowVisibleDisplayFrame(r);
                    int screenHeight = root.getRootView().getHeight();

                    // r.bottom is the position above soft keypad or device button.
                    // if keypad is shown, the r.bottom is smaller than that before.
                    int keypadHeight = screenHeight - r.bottom;


                    if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                        // keyboard is opened
                        if (!isKeyboardShowing) {
                            isKeyboardShowing = true;
                        }
                    } else {
                        // keyboard is closed
                        if (isKeyboardShowing) {
                            isKeyboardShowing = false;
                        }
                    }
                });
    }

    @Override
    protected void handleSuccess(StatusAwareData<List<MergedItem>> statusAwareData) {
        super.handleSuccess(statusAwareData);
        displayRecyclerView(adapter, statusAwareData, noItemTextView);
    }

    private void handleFinishRoom() {
        networkViewModel.sendValidationEntriesToServer();

        observeSpecificLiveData(networkViewModel.getValidationSuccessful(), liveData -> {
            if (liveData == null || liveData.getData() == null) return;

            if (liveData.getData()) {
                roomxItemSharedViewModel.setCurrentRoomValidated(true);
                NavHostFragment.findNavController(this).popBackStack();
            }
        });
    }


    private void displayRecyclerView(RecyclerViewAdapter adapter, StatusAwareData<List<MergedItem>> statusAwareMergedItem, TextView textView) {
        if (adapter == null) return;
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
            // User has the option to disable this behaviour in case the keyboard detection bugs
            if (isKeyboardShowing && !PreferenceUtility.getBoolean(getContext(), "switch_enforece_zebra", true))
                return;

            String action = intent.getAction();

            //Bundle b = intent.getExtras();
            //  This is useful for debugging to verify the format of received intents from DataWedge
            //for (String key : b.keySet())
            //{
            //    Log.v(LOG_TAG, key);
            //}

            assert action != null;
            if (action.equals(getResources().getString(R.string.activity_intent_filter_action))) {
                //  Received a barcode scan
                try {
                    String barcode = intent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_data));
                    launchItemDetailFragmentFromBarcode(barcode);
                } catch (Exception e) {
                    //  Catch if the UI does not exist when we receive the broadcast
                    basicNetworkErrorHandler.displayTextViewMessage("Bitte warten Sie bis der Scan bereit ist!");
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
        StatusAwareData<List<MergedItem>> statusAwareData = networkViewModel.getData().getValue();
        if (statusAwareData == null) return;
        List<MergedItem> items = statusAwareData.getData();
        if (items == null) return;

        // Item already scanned, create subItem (if user wants to)
        MergedItem mergedItemFromBarcode = networkViewModel.getMergedItemFromBarcode(barcode);
        if (mergedItemFromBarcode != null) {
            new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                    .setTitle("Doppelter Eintrag")
                    .setMessage("Sie haben diesen Gegenstand bereits validiert, wollen Sie ein Subitem anlegen?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        itemxDetailSharedViewModel.setCurrentItem(mergedItemFromBarcode);
                        NavHostFragment.findNavController(this).navigate(R.id.action_itemsFragment_to_itemDetailFragment);
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
            return;
        }

        // Item scanned first time and it's in the list
        for (MergedItem item : items) {
            if (item.equalsBarcode(barcode)) {
                itemxDetailSharedViewModel.setCurrentItem(item);
                NavHostFragment.findNavController(this).navigate(R.id.action_itemsFragment_to_itemDetailFragment);
                return;
            }
        }

        // Scanned item is not in the list ==> query the barcode
        itemxDetailSharedViewModel.setCurrentItem(MergedItem.createSearchedForItem(barcode));
        NavHostFragment.findNavController(this).navigate(R.id.action_itemsFragment_to_itemDetailFragment);

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


}
