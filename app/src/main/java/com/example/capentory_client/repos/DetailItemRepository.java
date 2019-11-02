package com.example.capentory_client.repos;

import android.content.Context;

import com.android.volley.ClientError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.capentory_client.models.MergedItem;
import com.example.capentory_client.models.MergedItemField;
import com.example.capentory_client.repos.customrequest.NetworkErrorHandler;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DetailItemRepository extends NetworkRepository<Map<String, MergedItemField>> {
    private final String SEARCHED_ITEM_REQUEST_KEY = "request_searched_item";
    private StatusAwareLiveData<MergedItem> searchedForItem = new StatusAwareLiveData<>();

    @Inject
    public DetailItemRepository(Context context) {
        super(context);
    }


    @Override
    public StatusAwareLiveData<Map<String, MergedItemField>> fetchMainData(String... args) {
        // Fetch only once for entire application, the form wont change
        if (mainContentRepoData.getValue() == null || mainContentRepoData.getValue().getData() == null) {
            addMainRequest(Request.Method.OPTIONS, getUrl(context, false, "api", "htlinventoryitems/"));
            launchMainRequest();
        }

        return mainContentRepoData;
    }


    @Override
    protected void handleMainSuccessfulResponse(JSONObject payload) {
        try {
            Map<String, MergedItemField> mergedItemFieldsSet = new HashMap<>();
            payload = payload.getJSONObject("displayFields");
            Iterator<String> iterator = payload.keys();

            while (iterator.hasNext()) {
                String key = iterator.next();
                mergedItemFieldsSet.put(key, new MergedItemField(key, payload));
            }
            mainContentRepoData.postSuccess(sortByValue(mergedItemFieldsSet));
        } catch (JSONException error) {
            mainContentRepoData.postError(error);
        }
    }


    public StatusAwareLiveData<MergedItem> fetchSearchedForItem(String barcode) {
        addRequest(SEARCHED_ITEM_REQUEST_KEY, Request.Method.GET,
                getUrl(context, false, "api", "htlinventoryitems", barcode),
                payload -> {
                    try {
                        searchedForItem.postSuccess(new MergedItem(payload, payload.keys().next(), barcode));
                    } catch (JSONException error) {
                        searchedForItem.postError(error);
                    }
                }, error -> {
                    if (error instanceof ClientError && ((ClientError) error).networkResponse.statusCode == 404) {
                        searchedForItem.postSuccess(new MergedItem(barcode));
                    } else super.handleErrorResponse(error, searchedForItem);

                });
        launchRequestFromKey(SEARCHED_ITEM_REQUEST_KEY, searchedForItem);

        return searchedForItem;
    }


    private static <K, V> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Object>() {
            @SuppressWarnings("unchecked")
            public int compare(Object o1, Object o2) {
                return ((Comparable<V>) ((Map.Entry<K, V>) (o1)).getValue()).compareTo(((Map.Entry<K, V>) (o2)).getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Iterator<Map.Entry<K, V>> it = list.iterator(); it.hasNext(); ) {
            Map.Entry<K, V> entry = it.next();
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }


}

