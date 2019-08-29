package com.example.capentory_client.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.capentory_client.R;
import com.example.capentory_client.viewmodels.ItemFragmentViewModel;
import com.example.capentory_client.viewmodels.ViewModelProviderFactory;
import com.example.capentory_client.viewmodels.adapter.RecyclerViewAdapter;
import com.example.capentory_client.viewmodels.sharedviewmodels.RaISharedViewModel;

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


        final RaISharedViewModel raISharedViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(RaISharedViewModel.class);
        String currentRoomString = Objects.requireNonNull(raISharedViewModel.getCurrentRoom().getValue()).getRoomNumber();
        raISharedViewModel.getCurrentRoom().observe(this, currentRoom -> currentRoomTextView.setText(currentRoomString));


        ItemFragmentViewModel itemFragmentViewModel = ViewModelProviders.of(this, providerFactory).get(ItemFragmentViewModel.class);
        itemFragmentViewModel.fetchItems(currentRoomString);

        itemFragmentViewModel.getMergedItems().observe(getViewLifecycleOwner(), statusAwareMergedItem -> {
            switch (statusAwareMergedItem.getStatus()) {
                case SUCCESS:
                    hideProgressBar();
                    adapter.fill(statusAwareMergedItem.getData());
                    break;
                case ERROR:
                    statusAwareMergedItem.getError().printStackTrace();
                    break;
                case FETCHING:
                    displayProgressbar();
                    break;

            }
        });

        swipeRefreshLayout.setOnRefreshListener(
                () -> {
                    itemFragmentViewModel.reloadItems(currentRoomString);
                    Toast.makeText(getContext(), "Neuer Fetchversuch...", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                }
        );

        return view;

    }

    private void displayProgressbar() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }


    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

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

}
