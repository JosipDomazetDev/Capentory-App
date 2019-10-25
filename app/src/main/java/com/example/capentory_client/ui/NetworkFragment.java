package com.example.capentory_client.ui;

import android.view.View;
import android.widget.ProgressBar;

import androidx.lifecycle.LiveData;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.capentory_client.repos.JsonRepository;
import com.example.capentory_client.ui.errorhandling.BasicNetworkErrorHandler;
import com.example.capentory_client.viewmodels.NetworkViewModel;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import dagger.android.support.DaggerFragment;

public abstract class NetworkFragment<P, R extends JsonRepository<P>, V extends NetworkViewModel<P, R>> extends DaggerFragment {

    protected V networkViewModel;
    protected ProgressBar progressBar;
    private View content;
    protected BasicNetworkErrorHandler basicNetworkErrorHandler;
    private SwipeRefreshLayout swipeRefreshLayout;

    public void init(V networkViewModel, BasicNetworkErrorHandler basicNetworkErrorHandler, View view, int progressBarID, View content, int swipeRefreshLayoutID, String... args) {
        initWithIDs(networkViewModel, basicNetworkErrorHandler, view, progressBarID, content, swipeRefreshLayoutID);

        networkViewModel.fetchData(args);
        initObserve(networkViewModel);


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
    public void init(V networkViewModel, BasicNetworkErrorHandler basicNetworkErrorHandler, View view, int progressBarID) {
        initWithIDs(networkViewModel, basicNetworkErrorHandler, view, progressBarID, content, -1);
    }


    public <T> void initSpecificObserve(LiveData<StatusAwareData<T>> data, LiveDataSuccessHandler<T> liveDataSuccessHandler) {
        data.observe(getViewLifecycleOwner(), statusAwareData -> {
            switch (statusAwareData.getStatus()) {
                case SUCCESS:
                    liveDataSuccessHandler.handleSuccess(statusAwareData);
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

    private void initObserve(V networkViewModel) {
        initSpecificObserve( networkViewModel.getData(), this::handleSuccess);
    }


    private void initWithIDs(V networkViewModel, BasicNetworkErrorHandler basicNetworkErrorHandler, View view, int progressBarID, View content, int swipeRefreshLayoutID) {
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
        initObserve(networkViewModel);
    }


    protected void handleSuccess(StatusAwareData<P> statusAwareData) {
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

    interface LiveDataSuccessHandler<P> {
        void handleSuccess(StatusAwareData<P> liveData);
    }
}
