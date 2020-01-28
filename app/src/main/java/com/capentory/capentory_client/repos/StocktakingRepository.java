package com.capentory.capentory_client.repos;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.capentory.capentory_client.models.MergedItem;
import com.capentory.capentory_client.models.SerializerEntry;
import com.capentory.capentory_client.models.Stocktaking;
import com.capentory.capentory_client.ui.MainActivity;
import com.capentory.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StocktakingRepository extends NetworkRepository<List<SerializerEntry>> {
    private final String GET_STOCKTAKINGS_REQUEST_KEY = "request_get_stocktakings";
    private final String GET_SEARCHED_FOR_ITEM_REQUEST_KEY = "request_get_specific_item";
    // This one musn't be reset
    private StatusAwareLiveData<List<Stocktaking>> activeStocktakingsLiveData = new StatusAwareLiveData<>();


    @Inject
    public StocktakingRepository(Context context) {
        super(context);
        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Display the first 500 characters of the response string.
            }
        };
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "",
                listener, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });


        stringRequest.cancel();
    }

    @Override
    protected void handleMainSuccessfulResponse(String stringPayload) {
        try {
            JSONObject payload = new JSONObject(stringPayload);
            List<SerializerEntry> serializerEntries = new ArrayList<>();
            Iterator<String> keys = payload.keys();

            while (keys.hasNext()) {
                serializerEntries.add(new SerializerEntry(keys.next(), payload));
            }

            mainContentRepoData.postSuccess(serializerEntries);
        } catch (JSONException error) {
            mainContentRepoData.postError(error);
        }
    }

    @Override
    public StatusAwareLiveData<List<SerializerEntry>> fetchMainData(String... args) {
        addMainRequest(Request.Method.GET, getUrl(context, false, "api", "inventoryserializers"));
        launchMainRequest();
        return mainContentRepoData;
    }


    public StatusAwareLiveData<List<Stocktaking>> fetchStocktakings() {
        // In this case we want to create a new LiveDataObject each time because the Stocktaking will be saved statically later
        // and we cannot overwrite it since we are leaving the screen at the same time
        // (this isn't an issue with other fragments e.g. old data being displayed shortly before new data is displayed)

        Map<String, String> paras = new HashMap<>();
        paras.put("date_finished__isnull", "True");
        paras.put("time_finish_isnull", "True");

        addRequest(GET_STOCKTAKINGS_REQUEST_KEY, Request.Method.GET,
                getUrl(context, false, new String[]{"api", "stocktaking/"}, paras),
                stringPayload -> {
                    try {
                        JSONArray payload = new JSONArray(stringPayload);
                        List<Stocktaking> activeStocktakings = new ArrayList<>();

                        for (int i = 0; i < payload.length(); i++) {
                            activeStocktakings.add(new Stocktaking(payload.getJSONObject(i),context));
                        }

                        activeStocktakingsLiveData.postSuccess(activeStocktakings);
                    } catch (JSONException e) {
                        activeStocktakingsLiveData.postError(e);
                    }
                }, activeStocktakingsLiveData);

        launchRequestFromKey(GET_STOCKTAKINGS_REQUEST_KEY, activeStocktakingsLiveData);

        return activeStocktakingsLiveData;
    }


    public StatusAwareLiveData<MergedItem> fetchSpecificallySearchedForItem(String barcode) {
        StatusAwareLiveData<MergedItem> specificallySearchedForItem = new StatusAwareLiveData<>();

        addRequest(GET_SEARCHED_FOR_ITEM_REQUEST_KEY, Request.Method.GET,
                getUrl(context, true, MainActivity.getSerializer(context).getItemUrl(), barcode),
                stringPayload -> {
                    try {
                        JSONArray payload = new JSONObject(stringPayload).getJSONArray("items");
                        if (payload.length() < 1)
                            specificallySearchedForItem.postSuccess(MergedItem.createNewEmptyItemWithBarcode(barcode, context));
                        else {
                            specificallySearchedForItem.postSuccess(new MergedItem(payload.getJSONObject(0)));
                        }
                    } catch (JSONException error) {
                        specificallySearchedForItem.postError(error);
                    }
                }, specificallySearchedForItem);
        launchRequestFromKey(GET_SEARCHED_FOR_ITEM_REQUEST_KEY, specificallySearchedForItem);

        return specificallySearchedForItem;
    }
}
