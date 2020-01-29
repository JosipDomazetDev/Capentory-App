package com.capentory.capentory_client.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.capentory.capentory_client.R;
import com.capentory.capentory_client.androidutility.PopUtility;
import com.capentory.capentory_client.androidutility.PreferenceUtility;
import com.capentory.capentory_client.androidutility.ToastUtility;
import com.capentory.capentory_client.models.MergedItem;
import com.capentory.capentory_client.models.RecyclerviewItem;
import com.capentory.capentory_client.models.Room;
import com.capentory.capentory_client.repos.MergedItemsRepository;
import com.capentory.capentory_client.ui.errorhandling.BasicNetworkErrorHandler;
import com.capentory.capentory_client.ui.errorhandling.CustomException;
import com.capentory.capentory_client.ui.scanactivities.ScanBarcodeActivity;
import com.capentory.capentory_client.viewmodels.MergedItemViewModel;
import com.capentory.capentory_client.viewmodels.ViewModelProviderFactory;
import com.capentory.capentory_client.viewmodels.adapter.RecyclerViewAdapter;
import com.capentory.capentory_client.viewmodels.sharedviewmodels.ItemxDetailSharedViewModel;
import com.capentory.capentory_client.viewmodels.sharedviewmodels.RoomxItemSharedViewModel;
import com.capentory.capentory_client.viewmodels.wrappers.StatusAwareData;
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
public class MergedItemsFragment extends NetworkFragment<List<RecyclerviewItem>, MergedItemsRepository, MergedItemViewModel> implements RecyclerViewAdapter.ItemClickListener {
    private RecyclerView recyclerView;
    private ItemxDetailSharedViewModel itemxDetailSharedViewModel;
    private RoomxItemSharedViewModel roomxItemSharedViewModel;
    private RecyclerViewAdapter adapter;
    private TextView noItemTextView;
    private TextView currentProgressTextView;
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        currentProgressTextView = view.findViewById(R.id.progress_textview_value_mergeditems);
        setAdditionalViewsToHide(currentProgressTextView);
        noItemTextView = view.findViewById(R.id.no_items_fragment_mergeditems);
        setAdditionalViewsToHide(noItemTextView);
        recyclerView = view.findViewById(R.id.recyclerv_view);
        adapter = getRecyclerViewAdapter();
        itemxDetailSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(ItemxDetailSharedViewModel.class);

        roomxItemSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(RoomxItemSharedViewModel.class);
        currentRoomString = Objects.requireNonNull(roomxItemSharedViewModel.getCurrentRoom().getValue()).getRoomId();
        String displayRoomString = Objects.requireNonNull(roomxItemSharedViewModel.getCurrentRoom().getValue()).getDisplayedNumber();

        initWithFetch(ViewModelProviders.of(this, providerFactory).get(MergedItemViewModel.class),
                new BasicNetworkErrorHandler(getContext(), view.findViewById(R.id.room_number_fragment_mergeditems)),
                view,
                R.id.progress_bar_fragment_mergeditems,
                recyclerView,
                R.id.swipe_refresh_fragment_mergeditems,
                currentRoomString
        );


        roomxItemSharedViewModel.getCurrentRoom().observe(getViewLifecycleOwner(), currentRoom -> currentRoomTextView.setText(
                // Set the current room view
                PopUtility.getHTMLFromStringRessources(R.string.current_room_fragment_mergeditems, displayRoomString, getContext())));


        view.findViewById(R.id.scan_item_floatingbtn).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ScanBarcodeActivity.class);
            startActivityForResult(intent, 0);
        });


        finishRoom.setOnClickListener(v -> {
            if (networkViewModel.getData() == null || networkViewModel.getData().getValue() == null)
                return;

            String title;
            String message;
            if (MainActivity.getStocktaking(getContext()).isNeverEndingStocktkaking()) {
                title = getString(R.string.title_never_ending_finishroom_fragment_mergeditems);
                message = getString(R.string.msg_never_ending_finishroom_fragment_mergeditems);
            } else if (networkViewModel.getAmountOfItemsLeft() == 1) {
                title = getString(R.string.title_one_left_finishroom_fragment_mergeditems);
                message = getString(R.string.msg_one_left_finishroom_fragment_mergeditems);
            } else if (networkViewModel.getAmountOfItemsLeft() > 1) {
                title = getString(R.string.title_X_left_finishroom_fragment_mergeditems, networkViewModel.getAmountOfItemsLeft());
                message = getString(R.string.msg_X_left_finishroom_fragment_mergeditems, networkViewModel.getAmountOfItemsLeft());
            } else {
                title = getString(R.string.title_0_left_finishroom_fragment_mergeditems);
                message = getString(R.string.msg_0_left_finishroom_fragment_mergeditems);
            }

            new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> handleFinishRoom())
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        });

        addItem.setOnClickListener(v -> {
            moveToItemDetail(MergedItem.createNewEmptyItem(v.getContext()));
        });

        itemxDetailSharedViewModel.getValidationEntryForCurrentItem().observe(getViewLifecycleOwner(), validationEntry -> {
            // If it's null this means that user aborted the DetailItemScreen altogether and doesn't want to create a ValidationEntry
            if (validationEntry != null) {
                itemxDetailSharedViewModel.setValidationEntryForCurrentItem(null);
                // This means he pressed the Red Button and wants to remove the Item from the List and mark it as missing
                if (validationEntry.isCanceledItem()) {
                    networkViewModel.removeCanceledItemDirectly(itemxDetailSharedViewModel.getCurrentItem());
                } else {
                    // This means he pressed the Green Button and wants to add a ValidationEntry
                    networkViewModel.addValidationEntry(validationEntry);
                    networkViewModel.removeItemByFoundCounterIncrease(itemxDetailSharedViewModel.getCurrentItem());
                }
            }
        });


        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                        .setTitle(getString(R.string.title_onback_fragment_mergeditems))
                        .setMessage(getString(R.string.msg_onback_fragment_mergeditems))
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
    protected void handleSuccess(StatusAwareData<List<RecyclerviewItem>> statusAwareData) {
        super.handleSuccess(statusAwareData);
        displayRecyclerView(adapter, statusAwareData, noItemTextView);
        networkViewModel.getProgressMessage().observe(getViewLifecycleOwner(), progressAsText -> {
            // Set progress text view
            currentProgressTextView.setText(PopUtility.getHTMLFromStringRessources(R.string.progress_fragment_mergeditems, progressAsText, getContext()));
            currentProgressTextView.setVisibility(View.VISIBLE);
        });

    }

    private void handleFinishRoom() {
        networkViewModel.sendValidationEntriesToServer();

        observeSpecificLiveData(networkViewModel.getValidationSuccessful(), liveData -> {
            if (liveData == null || liveData.getData() == null) return;

            if (liveData.getData()) {
                roomxItemSharedViewModel.setCurrentRoomValidated(true);
                //roomxItemSharedViewModel.setCurrentRooms(networkViewModel.getSuperRoom());
                NavHostFragment.findNavController(this).popBackStack();
            }
        }, error -> {
            basicNetworkErrorHandler.displayTextViewErrorMessage(error);
            hideProgressBarAndShowContent();
        });
    }

    protected void handleError(Throwable error) {
        basicNetworkErrorHandler.displayTextViewErrorMessage(error);
        hideProgressBarAndShowContent();
    }


    private void displayRecyclerView(RecyclerViewAdapter adapter, StatusAwareData<List<RecyclerviewItem>> statusAwareMergedItem, TextView textView) {
        if (adapter == null) return;
        List<RecyclerviewItem> mergedItems = statusAwareMergedItem.getData();
        if (mergedItems == null) return;

        if (networkViewModel.getAmountOfItemsLeft() < 1) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(getString(R.string.no_items_left_fragment_mergeditems));
        } else {
            textView.setVisibility(View.GONE);
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
            if (isKeyboardShowing && !PreferenceUtility.getBoolean(getContext(), SettingsFragment.ENFORCE_ZEBRA_KEY, true))
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
                    basicNetworkErrorHandler.displayTextViewMessage(getString(R.string.wait_till_scan_ready_error));
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
        if (adapter.getItem(position) instanceof MergedItem) {
            moveToItemDetail((MergedItem) adapter.getItem(position));
        } else {
            try {
                adapter.handleCollapseAndExpand(position, recyclerView.getChildViewHolder(v));
            } catch (Exception e) {
                basicNetworkErrorHandler.displayTextViewErrorMessage(
                        new CustomException(getString(R.string.expand_failure_fragment_mergeditems)));
            }
        }
    }

    private void moveToItemDetail(MergedItem currentItem) {
        itemxDetailSharedViewModel.setCurrentItem(currentItem);
        List<Room> rooms = networkViewModel.getRooms();
        if (rooms == null) {
            basicNetworkErrorHandler.displayTextViewMessage(getString(R.string.mergeditem_fragment));
            return;
        }
        itemxDetailSharedViewModel.setCurrentRooms(rooms);
        NavHostFragment.findNavController(this).navigate(R.id.itemDetailFragment);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 0) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    String barcode = data.getStringExtra("barcode");
                    launchItemDetailFragmentFromBarcode(barcode);
                } else {
                    ToastUtility.displayCenteredToastMessage(getContext(),
                            getString(R.string.expand_failure_fragment_mergeditems), Toast.LENGTH_SHORT);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void launchItemDetailFragmentFromBarcode(String barcode) {
        StatusAwareData<List<RecyclerviewItem>> statusAwareData = networkViewModel.getData().getValue();
        if (statusAwareData == null) return;
        List<RecyclerviewItem> items = statusAwareData.getData();
        if (items == null) return;

        // Item already scanned, create subItem (if user wants to)
        MergedItem mergedItemFromBarcode = networkViewModel.getMergedItemFromBarcode(barcode);
        if (mergedItemFromBarcode != null) {
            new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                    .setTitle(getString(R.string.title_duplicate_fragment_mergeditems))
                    .setMessage(getString(R.string.msg_duplicate_fragment_mergeditems))
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        moveToItemDetail(mergedItemFromBarcode);
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
            return;
        }

        // Item scanned first time and it's in the list
        for (RecyclerviewItem recyclerviewItem : items) {
            if (recyclerviewItem instanceof MergedItem) {
                MergedItem mergedItem = (MergedItem) recyclerviewItem;
                if (mergedItem.equalsBarcode(barcode)) {
                    moveToItemDetail(mergedItem);
                    return;
                }
            }
        }

        // Scanned item is not in the list ==> query the barcode
        moveToItemDetail(MergedItem.createSearchedForItem(barcode));
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
