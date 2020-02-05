package com.capentory.capentory_client.ui;

import android.view.View;
import android.widget.ProgressBar;

import androidx.lifecycle.LiveData;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.capentory.capentory_client.repos.NetworkRepository;
import com.capentory.capentory_client.ui.errorhandling.ErrorHandler;
import com.capentory.capentory_client.viewmodels.NetworkViewModel;
import com.capentory.capentory_client.viewmodels.wrappers.StatusAwareData;

import dagger.android.support.DaggerFragment;

public abstract class NetworkFragment<P, R extends NetworkRepository<P>, V extends NetworkViewModel<P, R>> extends DaggerFragment {
    interface RefreshHandler {
        void handleRefresh();
    }

    V networkViewModel;
    ProgressBar progressBar;
    private View content;
    ErrorHandler errorHandler;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View[] additionalViewsToHide;

    void initWithFetch(V networkViewModel, ErrorHandler errorHandler, View view, int progressBarID, View content, int swipeRefreshLayoutID, String... args) {
        initWithIDs(networkViewModel, errorHandler, view, progressBarID, content, swipeRefreshLayoutID);

        networkViewModel.fetchData(args);
        observeMainLiveData(networkViewModel);

        swipeRefreshLayout.setOnRefreshListener(
                () -> {
                    refresh();
                    networkViewModel.reloadData(args);
                    swipeRefreshLayout.setRefreshing(false);
                }
        );
    }

    protected void refresh() {
    }

    ;


    void initWithFetch(V networkViewModel, ErrorHandler errorHandler, View view, int progressBarID, View content, int swipeRefreshLayoutID, RefreshHandler refreshHandler, String... args) {
        initWithIDs(networkViewModel, errorHandler, view, progressBarID, content, swipeRefreshLayoutID);

        networkViewModel.fetchData(args);
        observeMainLiveData(networkViewModel);


        swipeRefreshLayout.setOnRefreshListener(
                () -> {
                    refresh();
                    refreshHandler.handleRefresh();
                    swipeRefreshLayout.setRefreshing(false);
                }
        );
    }


    /**
     * This one dooesn't fetch automatically
     *
     * @param networkViewModel
     * @param errorHandler
     * @param view
     * @param progressBarID
     */
    void initWithoutFetch(V networkViewModel, ErrorHandler errorHandler, View view, int progressBarID) {
        initWithIDs(networkViewModel, errorHandler, view, progressBarID, null, -1);
    }


    <T> void observeSpecificLiveData(LiveData<StatusAwareData<T>> data, LiveDataSuccessHandler<T> liveDataSuccessHandler) {
        observeSpecificLiveData(data, liveDataSuccessHandler, this::handleError);
    }


    // Custom error handling
    <T> void observeSpecificLiveData(LiveData<StatusAwareData<T>> data, LiveDataSuccessHandler<T> liveDataSuccessHandler, LiveDataErrorHandler liveDataErrorHandler) {
        if (data == null) return;

        data.observe(getViewLifecycleOwner(), statusAwareData -> {
            switch (statusAwareData.getStatus()) {
                case SUCCESS:
                    liveDataSuccessHandler.handleSuccess(statusAwareData);
                    break;
                case ERROR:
                    liveDataErrorHandler.handleError(statusAwareData.getError());
                    break;
                case FETCHING:
                    handleFetching();
                    break;
            }
            if (statusAwareData.getStatus() != StatusAwareData.State.ERROR)
                errorHandler.reset();

        });
    }

    private void observeMainLiveData(V networkViewModel) {
        observeSpecificLiveData(networkViewModel.getLiveData(), this::handleSuccess);
    }


    private void initWithIDs(V networkViewModel, ErrorHandler errorHandler, View view, int progressBarID, View content, int swipeRefreshLayoutID) {
        this.networkViewModel = networkViewModel;
        this.errorHandler = errorHandler;
        if (progressBarID != -1)
            this.progressBar = view.findViewById(progressBarID);
        progressBar.bringToFront();
        this.content = content;
        if (swipeRefreshLayoutID != -1)
            this.swipeRefreshLayout = view.findViewById(swipeRefreshLayoutID);
    }


    protected void fetchManually(String... args) {
        networkViewModel.fetchData(args);
        observeMainLiveData(networkViewModel);
    }


    protected void handleSuccess(StatusAwareData<P> statusAwareData) {
        hideProgressBarAndShowContent();
    }


    protected void handleError(Throwable error) {
        errorHandler.displayTextViewErrorMessage(error);
        hideProgressBarAndHideContent();
    }


    protected void handleFetching() {
        displayProgressbarAndHideContent();
    }


    protected void displayProgressbarAndHideContent() {
        progressBar.setVisibility(View.VISIBLE);
        if (content != null)
            content.setVisibility(View.GONE);

        if (additionalViewsToHide == null) return;
        for (View view : additionalViewsToHide) {
            view.setVisibility(View.GONE);
        }
    }


    protected void hideProgressBarAndShowContent() {
        progressBar.setVisibility(View.GONE);

        if (content != null)
            content.setVisibility(View.VISIBLE);
        // additionalViewsToHide will not be automatically displayed
        // The reason being that content and additionalViewsToHide may not be supposed to be shown at the same time
        // e.g. The view "No Items left" should always be hidden at the start of screen and only be displayed when the fetched items are validated
        // However should not be automatically displayed along with the fetched content
    }

    protected void hideProgressBarAndHideContent() {
        progressBar.setVisibility(View.GONE);
    }

    interface LiveDataSuccessHandler<P> {
        void handleSuccess(StatusAwareData<P> liveData);
    }

    interface LiveDataErrorHandler {
        void handleError(Throwable error);
    }


    public void setAdditionalViewsToHide(View... additionalViewsToHide) {
        this.additionalViewsToHide = additionalViewsToHide;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
