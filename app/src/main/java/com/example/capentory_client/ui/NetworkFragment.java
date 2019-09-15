package com.example.capentory_client.ui;

import android.view.View;
import android.widget.ProgressBar;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.capentory_client.ui.errorhandling.BasicNetworkErrorHandler;
import com.example.capentory_client.viewmodels.NetworkViewModel;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import dagger.android.support.DaggerFragment;

public abstract class NetworkFragment<L> extends DaggerFragment {

    protected NetworkViewModel<L> networkViewModel;
    private ProgressBar progressBar;
    private View content;
    private BasicNetworkErrorHandler basicNetworkErrorHandler;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String[] args;


    public void init(NetworkViewModel<L> networkViewModel, BasicNetworkErrorHandler basicNetworkErrorHandler, View view, int progressBarID, View content, int swipeRefreshLayoutID, String... args) {
        initWithIDs(networkViewModel, basicNetworkErrorHandler, view, progressBarID, content, swipeRefreshLayoutID, args);

        networkViewModel.fetchData(args);
        networkViewModel.getData().observe(getViewLifecycleOwner(), statusAwareData -> {
            switch (statusAwareData.getStatus()) {
                case SUCCESS:
                    handleSuccess(statusAwareData);
                    break;
                case ERROR:
                    handleError(statusAwareData);
                    break;
                case FETCHING:
                    handleFetching(statusAwareData);
                    break;
            }
            if (statusAwareData.getStatus() != StatusAwareData.State.ERROR)
                basicNetworkErrorHandler.reset();

        });


        swipeRefreshLayout.setOnRefreshListener(
                () -> {
                    networkViewModel.reloadData(args);
                    swipeRefreshLayout.setRefreshing(false);
                }
        );

    }



    private void initWithIDs(NetworkViewModel<L> networkViewModel, BasicNetworkErrorHandler basicNetworkErrorHandler, View view, int progressBarID, View content, int swipeRefreshLayoutID, String[] args) {
        this.networkViewModel = networkViewModel;
        this.basicNetworkErrorHandler = basicNetworkErrorHandler;
        this.progressBar = view.findViewById(progressBarID);
        progressBar.bringToFront();
        this.content = content;
        this.swipeRefreshLayout = view.findViewById(swipeRefreshLayoutID);
        this.args = args;
    }

    protected void handleSuccess(StatusAwareData<L> statusAwareData) {
        hideProgressBarAndShowContent();
    }


    protected void handleError(StatusAwareData<L> statusAwareData) {
        basicNetworkErrorHandler.displayTextViewMessage(statusAwareData.getError());
        hideProgressBarAndHideContent();
    }


    protected void handleFetching(StatusAwareData<L> statusAwareData) {
        displayProgressbarAndHideContent();
    }



    private void displayProgressbarAndHideContent() {
        progressBar.setVisibility(View.VISIBLE);
        content.setVisibility(View.GONE);
    }


    private void hideProgressBarAndShowContent() {
        progressBar.setVisibility(View.GONE);
        content.setVisibility(View.VISIBLE);
    }

    private void hideProgressBarAndHideContent() {
        progressBar.setVisibility(View.GONE);
        content.setVisibility(View.GONE);
    }

}
