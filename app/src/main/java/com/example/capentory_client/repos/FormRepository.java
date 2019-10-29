package com.example.capentory_client.repos;

import android.content.Context;

import com.android.volley.Request;
import com.example.capentory_client.models.MergedItemField;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
public class FormRepository extends JsonRepository<Map<String, MergedItemField>> {

    @Inject
    public FormRepository(Context context) {
        super(context);
    }


    @Override
    public StatusAwareLiveData<Map<String, MergedItemField>> fetchMainData(String... args) {
        // Fetch only once for entire application, the form wont change
        if (mainContentRepoData.getValue() == null || mainContentRepoData.getValue().getData() == null) {
            addMainRequest(Request.Method.OPTIONS, getUrl(context, false, "api", "actualitem/"));
            launchMainRequest();
        }

        return mainContentRepoData;
    }


    @Override
    protected void handleMainSuccessfulResponse(JSONObject payload) {
        try {
            payload = payload.getJSONObject("actions").getJSONObject("POST");
            Map<String, MergedItemField> mergedItemFieldsSet = new HashMap<>();
            Iterator<String> iterator = payload.keys();

            while (iterator.hasNext()) {
                String key = iterator.next();
                mergedItemFieldsSet.put(key, new MergedItemField(key, payload.getJSONObject(key)));
            }
            mainContentRepoData.postSuccess(sortByValue(mergedItemFieldsSet));
        } catch (JSONException error) {
            mainContentRepoData.postError(error);
        }
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
        for (Iterator<Map.Entry<K, V>> it = list.iterator(); it.hasNext();) {
            Map.Entry<K, V> entry = it.next();
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}

