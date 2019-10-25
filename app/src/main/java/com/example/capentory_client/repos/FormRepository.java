package com.example.capentory_client.repos;

import android.content.Context;

import com.android.volley.Request;
import com.example.capentory_client.models.MergedItemField;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
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
            mainContentRepoData.postSuccess(mergedItemFieldsSet);
        } catch (JSONException error) {
            mainContentRepoData.postError(error);
        }
    }


}

