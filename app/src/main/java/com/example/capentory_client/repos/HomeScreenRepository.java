package com.example.capentory_client.repos;

import android.content.Context;

import com.android.volley.Request;
import com.example.capentory_client.models.MergedItem;
import com.example.capentory_client.ui.MainActivity;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import org.json.JSONException;
import org.json.JSONObject;

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

        addMainRequest(Request.Method.GET, getUrl(context, true, MainActivity.getSerializer(context).getItemUrl(), barcode));
        launchMainRequest();
        return mainContentRepoData;
    }

    @Override
    protected void handleMainSuccessfulResponse(String stringPayload) {
        return;
    }


}
