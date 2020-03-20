package com.capentory.capentory_client.ui;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.capentory.capentory_client.R;
import com.capentory.capentory_client.androidutility.PopUtility;
import com.capentory.capentory_client.androidutility.PreferenceUtility;
import com.capentory.capentory_client.androidutility.SearchBarHelperUtility;
import com.capentory.capentory_client.androidutility.ToastUtility;
import com.capentory.capentory_client.androidutility.UserUtility;
import com.capentory.capentory_client.androidutility.AlertUtility;
import com.capentory.capentory_client.models.MergedItem;
import com.capentory.capentory_client.models.RecyclerViewItem;
import com.capentory.capentory_client.ui.errorhandling.CustomException;
import com.capentory.capentory_client.ui.errorhandling.ErrorHandler;
import com.capentory.capentory_client.ui.scanactivities.ScanBarcodeActivity;
import com.capentory.capentory_client.ui.zebra.ZebraBroadcastReceiver;
import com.capentory.capentory_client.viewmodels.ViewModelProviderFactory;
import com.capentory.capentory_client.viewmodels.adapter.RecyclerViewAdapter;
import com.capentory.capentory_client.viewmodels.sharedviewmodels.ItemXValidatedSharedViewModel;
import com.capentory.capentory_client.viewmodels.sharedviewmodels.RoomxItemSharedViewModel;
import com.capentory.capentory_client.viewmodels.wrappers.StatusAwareData;
import com.google.android.gms.common.api.CommonStatusCodes;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

/**
 * A simple {@link Fragment} subclass.
 */
public class ValidatedMergedItemsFragment extends Fragment implements RecyclerViewAdapter.ItemClickListener {
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    private TextView noItemTextView;
    private TextView currentProgressTextView;
    private boolean isKeyboardShowing = false;
    private RoomxItemSharedViewModel roomxItemSharedViewModel;
    private ItemXValidatedSharedViewModel itemXValidatedSharedViewModel;
    private ErrorHandler errorHandler;
    @Inject
    ViewModelProviderFactory providerFactory;

    private ZebraBroadcastReceiver zebraBroadcastReceiver = new ZebraBroadcastReceiver(this::askForRevision);
    private AlertDialog revisionMessage;


    public ValidatedMergedItemsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);


        SearchBarHelperUtility.bindSearchBar(menu, inflater, getActivity(), new SearchBarHelperUtility.SearchHandler() {
            @Override
            public void onQueryTextSubmit(String query) {
                UserUtility.hideKeyboard(getActivity());
            }

            @Override
            public void onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
            }
        });
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_validated_merged_items, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TextView currentRoomTextView = view.findViewById(R.id.room_number_fragment_validated_mergeditems);
        currentProgressTextView = view.findViewById(R.id.progress_textview_value_fragment_validated_mergeditems);

        noItemTextView = view.findViewById(R.id.no_items_fragment_validated_mergeditems);

        recyclerView = view.findViewById(R.id.recycler_view_fragment_validated_mergeditems);

        adapter = getRecyclerViewAdapter();

        itemXValidatedSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(ItemXValidatedSharedViewModel.class);
        roomxItemSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(RoomxItemSharedViewModel.class);

        String displayRoomString = Objects.requireNonNull(roomxItemSharedViewModel.getCurrentRoom().getValue()).getDisplayedNumber();

        errorHandler = new ErrorHandler(getContext());
      /*  initWithoutFetch(ViewModelProviders.of(this, providerFactory).get(MergedItemViewModel.class),
                new ErrorHandler(getContext(), view.findViewById(R.id.room_number_fragment_mergeditems)),
                view,
                recyclerView
        );*/


        roomxItemSharedViewModel.getCurrentRoom().observe(getViewLifecycleOwner(), currentRoom ->
                // Set the current room view
                currentRoomTextView.setText(PopUtility.getHTMLFromStringRessources(R.string.current_room_fragment_mergeditems, displayRoomString, getContext())));


        // https://stackoverflow.com/questions/4745988/how-do-i-detect-if-software-keyboard-is-visible-on-android-device
        View root = view.findViewById(R.id.content_fragment_validated_mergeditems);
        root.getViewTreeObserver().addOnGlobalLayoutListener(() -> setKeyboardShowing(root));


       /* requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                        .setTitle(getString(R.string.title_onback_fragment_mergeditems))
                        .setMessage(getString(R.string.msg_onback_fragment_mergeditems))
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> NavHostFragment.findNavController(ValidatedMergedItemsFragment.this).popBackStack())
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });
*/


        view.findViewById(R.id.scan_item_floatingbtn_validated_mergeditems_fragment).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ScanBarcodeActivity.class);
            startActivityForResult(intent, 0);
        });


        itemXValidatedSharedViewModel.getAlreadyValidatedItems().observe(getViewLifecycleOwner(), statusAwareData -> {
            displayRecyclerView(adapter, statusAwareData, noItemTextView);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 0) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    String barcode = data.getStringExtra("barcode");
                    askForRevision(barcode);
                } else {
                    ToastUtility.displayCenteredToastMessage(getContext(),
                            getString(R.string.scan_failed_scan_fragments), Toast.LENGTH_SHORT);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setKeyboardShowing(View root) {
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
    }


    private void displayRecyclerView(RecyclerViewAdapter adapter, StatusAwareData<List<RecyclerViewItem>> statusAwareMergedItem, TextView textView) {
        if (adapter == null) return;
        List<RecyclerViewItem> mergedItems = statusAwareMergedItem.getData();
        if (mergedItems == null) return;

        displayRecyclerView(adapter, mergedItems, textView);
    }

    private void displayRecyclerView(RecyclerViewAdapter adapter, List<RecyclerViewItem> mergedItems, TextView textView) {
        if (mergedItems.size() < 1) {
            recyclerView.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
            textView.setText(getString(R.string.no_items_validated_fragment_validated_mergeditems));
        } else {
            textView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.fill(mergedItems);
        }
    }


    @NonNull
    private RecyclerViewAdapter getRecyclerViewAdapter() {
        final RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, false);
        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        return adapter;
    }

    @Override
    public void onItemClick(int position, View v) {
        if (adapter.getItem(position) instanceof MergedItem) {
            handleRevision((MergedItem) adapter.getItem(position));
        } else {
            try {
                adapter.handleCollapseAndExpand(position, recyclerView.getChildViewHolder(v));
            } catch (Exception e) {
                errorHandler.displayTextViewErrorMessage(
                        new CustomException(getString(R.string.expand_failure_fragment_mergeditems)));
            }
        }
    }

    private void handleRevision(MergedItem mergedItem) {
        TextView textView = new TextView(getContext());
        textView.setText(getString(R.string.title_revise_item_fragment_validated_mergeditems));
        textView.setPadding(43, 30, 20, 30);
        textView.setTextSize(20F);
        textView.setTextColor(Color.parseColor("#e8190e"));

        revisionMessage = new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                .setCustomTitle(textView)
                .setMessage(getString(R.string.msg_revise_item_fragment_validated_mergeditems))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> handleItemRevision(mergedItem))
                .setNegativeButton(android.R.string.no, null).create();

        revisionMessage.show();
    }

    private void handleItemRevision(MergedItem item) {
        itemXValidatedSharedViewModel.addItemsToRevise(item);
    }

   /* private void moveToItemDetail(MergedItem currentItem) {
        itemxDetailSharedViewModel.setCurrentItem(currentItem);
        List<Room> rooms = networkViewModel.getRooms();
        if (rooms == null) {
            errorHandler.displayTextViewMessage(getString(R.string.mergeditem_fragment));
            return;
        }
        itemxDetailSharedViewModel.setCurrentRooms(rooms);
        NavHostFragment.findNavController(this).navigate(R.id.itemDetailFragment);
    }*/


    @Override
    public void onResume() {
        super.onResume();
        ZebraBroadcastReceiver.registerZebraReceiver(getContext(), zebraBroadcastReceiver, errorHandler);

        synchronized (adapter) {
            List<RecyclerViewItem> recyclerViewItems = itemXValidatedSharedViewModel.getAlreadyValidatedItems().getValue();
            if (recyclerViewItems == null) return;
            adapter.fill(recyclerViewItems);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        ZebraBroadcastReceiver.unregisterZebraReceiver(getContext(), zebraBroadcastReceiver);

        synchronized (adapter) {
            List<RecyclerViewItem> recyclerViewItems = itemXValidatedSharedViewModel.getAlreadyValidatedItems().getValue();
            if (recyclerViewItems == null) return;
            adapter.fill(recyclerViewItems);
        }
    }

    private void askForRevision(String barcode) {
        if (isKeyboardShowing && !PreferenceUtility.getBoolean(getContext(), SettingsFragment.ENFORCE_ZEBRA_KEY, true))
            return;

        if (revisionMessage != null && revisionMessage.isShowing()) {
            ToastUtility.displayCenteredToastMessage(getContext(), getString(R.string.warning_duplicate_fragment_mergeditems), Toast.LENGTH_LONG);
            AlertUtility.makeNormalVibration(getContext());
            return;
        }

        if (itemXValidatedSharedViewModel.getAlreadyValidatedItems() == null
                || itemXValidatedSharedViewModel.getAlreadyValidatedItems().getValue() == null)
            return;

        for (RecyclerViewItem recyclerViewItem : itemXValidatedSharedViewModel.getAlreadyValidatedItems().getValue()) {
            if (recyclerViewItem instanceof MergedItem) {
                if (((MergedItem) recyclerViewItem).equalsBarcode(barcode)) {
                    handleRevision((MergedItem) recyclerViewItem);
                    return;
                }
            }
        }

        ToastUtility.displayCenteredToastMessage(getContext(), getString(R.string.item_not_done_fragment_validated_mergeditems), Toast.LENGTH_LONG);
    }


}
