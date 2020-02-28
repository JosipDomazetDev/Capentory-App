package com.capentory.capentory_client.repos;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.capentory.capentory_client.models.MergedItem;
import com.capentory.capentory_client.models.MergedItemField;
import com.capentory.capentory_client.ui.MainActivity;
import com.capentory.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import org.json.JSONArray;
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
    private StatusAwareLiveData<MergedItem> searchedForItem;

    @Inject
    public DetailItemRepository(Context context) {
        super(context);
    }


    @Override
    public StatusAwareLiveData<Map<String, MergedItemField>> fetchMainData(String... args) {
        // Fetch only once for entire application, the form wont change
        if (mainContentRepoData.getValue() == null || mainContentRepoData.getValue().getData() == null) {
            addMainRequest(Request.Method.OPTIONS, getUrl(context, true, MainActivity.getSerializer().getItemUrl()));
            launchMainRequest();
        }

        return mainContentRepoData;
    }


    @Override
    protected void handleMainSuccessfulResponse(String stringPayload) {
        try {
            JSONObject payload = new JSONObject(stringPayload);
            Map<String, MergedItemField> mergedItemFieldsSet = new HashMap<>();

            readFields(payload.getJSONObject("displayFields"), mergedItemFieldsSet, MergedItemField.NORMAL_FIELD_CODE);
            readFields(payload.getJSONObject("extraFields"), mergedItemFieldsSet, MergedItemField.EXTRA_FIELD_CODE);
            readFields(payload.getJSONObject("extraFields")
                    .getJSONObject("custom_fields")
                    .getJSONObject("fields"), mergedItemFieldsSet, MergedItemField.CUSTOM_FIELD_CODE);

            mainContentRepoData.postSuccess(sortByValue(mergedItemFieldsSet));
        } catch (JSONException error) {
            mainContentRepoData.postError(error);
        }
    }

    private void readFields(JSONObject payload, Map<String, MergedItemField> mergedItemFieldsSet, int fieldClassifier) throws JSONException {
        Iterator<String> iterator = payload.keys();

        while (iterator.hasNext()) {
            String key = iterator.next();
            if (key.equals("custom_fields")) continue;
            mergedItemFieldsSet.put(key, new MergedItemField(key, payload, fieldClassifier));
        }
    }


    public StatusAwareLiveData<MergedItem> fetchSearchedForItem(String barcode) {
        searchedForItem = new StatusAwareLiveData<>();

        addRequest(SEARCHED_ITEM_REQUEST_KEY, Request.Method.GET,
                getUrl(context, true, MainActivity.getSerializer().getItemUrl(), barcode),
                stringPayload -> {
                    try {
                        JSONArray payload = new JSONObject(stringPayload).getJSONArray("items");
                        if (payload.length() < 1)
                            // Either retuning new item
                            searchedForItem.postSuccess(MergedItem.createNewEmptyItemWithBarcode(barcode, context));
                        else {
                            // Or normal item from other room
                            searchedForItem.postSuccess(MergedItem.createItemFromOtherRoom(payload.getJSONObject(0)));
                        }
                    } catch (JSONException error) {
                        searchedForItem.postError(error);
                    }
                }, searchedForItem);
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

