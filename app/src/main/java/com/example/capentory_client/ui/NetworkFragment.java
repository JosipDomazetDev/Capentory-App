package com.example.capentory_client.ui;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TabHost;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.capentory_client.ui.errorhandling.BasicNetworkErrorHandler;
import com.example.capentory_client.viewmodels.NetworkViewModel;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import dagger.android.support.DaggerFragment;

public abstract class NetworkFragment<L> extends DaggerFragment {


    protected NetworkViewModel<L> networkViewModel;
    protected ProgressBar progressBar;
    private View content;
    protected BasicNetworkErrorHandler basicNetworkErrorHandler;
    private SwipeRefreshLayout swipeRefreshLayout;

    public void init(NetworkViewModel<L> networkViewModel, BasicNetworkErrorHandler basicNetworkErrorHandler, View view, int progressBarID, View content, int swipeRefreshLayoutID, String... args) {
        initWithIDs(networkViewModel, basicNetworkErrorHandler, view, progressBarID, content, swipeRefreshLayoutID);

        networkViewModel.fetchData(args);
        initObserve(networkViewModel, basicNetworkErrorHandler);


        swipeRefreshLayout.setOnRefreshListener(
                () -> {
                    networkViewModel.reloadData(args);
                    swipeRefreshLayout.setRefreshing(false);
                }
        );

    }

    /**
     * This one dooesn't fetch automatically
     *
     * @param networkViewModel
     * @param basicNetworkErrorHandler
     * @param view
     * @param progressBarID
     */
    public void init(NetworkViewModel<L> networkViewModel, BasicNetworkErrorHandler basicNetworkErrorHandler, View view, int progressBarID) {
        initWithIDs(networkViewModel, basicNetworkErrorHandler, view, progressBarID, content, -1);
    }

    private void initObserve(NetworkViewModel<L> networkViewModel, BasicNetworkErrorHandler basicNetworkErrorHandler) {
        networkViewModel.getData().observe(getViewLifecycleOwner(), statusAwareData -> {
            switch (statusAwareData.getStatus()) {
                case SUCCESS:
                    handleSuccess(statusAwareData);
                    break;
                case ERROR:
                    handleError(statusAwareData.getError());
                    break;
                case FETCHING:
                    handleFetching();
                    break;
            }
            if (statusAwareData.getStatus() != StatusAwareData.State.ERROR)
                basicNetworkErrorHandler.reset();

        });
    }


    private void initWithIDs(NetworkViewModel<L> networkViewModel, BasicNetworkErrorHandler basicNetworkErrorHandler, View view, int progressBarID, View content, int swipeRefreshLayoutID) {
        this.networkViewModel = networkViewModel;
        this.basicNetworkErrorHandler = basicNetworkErrorHandler;
        if (progressBarID != -1)
            this.progressBar = view.findViewById(progressBarID);
        progressBar.bringToFront();
        this.content = content;
        if (swipeRefreshLayoutID != -1)
            this.swipeRefreshLayout = view.findViewById(swipeRefreshLayoutID);
    }


    protected void fetchManually(String... args) {
        networkViewModel.fetchData(args);
        initObserve(networkViewModel, basicNetworkErrorHandler);
    }


    protected void handleSuccess(StatusAwareData<L> statusAwareData) {
        hideProgressBarAndShowContent();
    }


    protected void handleError(Throwable error) {
        basicNetworkErrorHandler.displayTextViewMessage(error);
        hideProgressBarAndHideContent();
    }


    protected void handleFetching() {
        displayProgressbarAndHideContent();
    }


    protected void displayProgressbarAndHideContent() {
        progressBar.setVisibility(View.VISIBLE);
        content.setVisibility(View.GONE);
    }


    protected void hideProgressBarAndShowContent() {
        progressBar.setVisibility(View.GONE);
        content.setVisibility(View.VISIBLE);
    }

    protected void hideProgressBarAndHideContent() {
        progressBar.setVisibility(View.GONE);
        content.setVisibility(View.GONE);

    }

}
