package com.capentory.capentory_client.repos;

import android.content.Context;

import com.android.volley.Request;
import com.capentory.capentory_client.models.MergedItem;
import com.capentory.capentory_client.repos.customrequest.NetworkErrorHandler;
import com.capentory.capentory_client.repos.customrequest.NetworkSuccessHandler;
import com.capentory.capentory_client.repos.customrequest.RobustJsonRequestExecutioner;
import com.capentory.capentory_client.ui.MainActivity;
import com.capentory.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HomeScreenRepository extends NetworkRepository<MergedItem> {
    private String barcode;

    @Inject
    HomeScreenRepository(Context context) {
        super(context);
    }

    @Override
    public StatusAwareLiveData<MergedItem> fetchMainData(String... args) {
        mainContentRepoData = new StatusAwareLiveData<>();
        if (args.length != 1) {
            throw new IllegalArgumentException("Nur ein Barcode aus Argument erlaubt!");
        }
        this.barcode = args[0];

        addMainRequest(Request.Method.GET, getUrl(context, true, MainActivity.getSerializer().getItemUrl(), barcode));
        launchMainRequest();

        RobustJsonRequestExecutioner robustJsonRequestExecutioner = new RobustJsonRequestExecutioner(context, Request.Method.GET, "https://www.beispiel.org/", null,
                new NetworkSuccessHandler() {
                    @Override
                    public void handleSuccess(String payload) {
                        // TODO: Antwort verarbeiten

                    }
                },
                new NetworkErrorHandler() {
                    @Override
                    public void handleError(Exception error) {
                        // TODO: Fehler verarbeiten
                    }
                });


        robustJsonRequestExecutioner.launchRequest();

        return mainContentRepoData;
    }

    @Override
    protected void handleMainSuccessfulResponse(String stringPayload) {
        return;
    }


}
