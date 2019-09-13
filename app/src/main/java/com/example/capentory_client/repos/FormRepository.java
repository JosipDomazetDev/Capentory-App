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
public class FormRepository extends Repository {
    private StatusAwareLiveData<Map<String, MergedItemField>> mergedItemFieldsLiveData = new StatusAwareLiveData<>();

    @Inject
    public FormRepository(Context context) {
        super(context);
    }


    public StatusAwareLiveData<Map<String, MergedItemField>> getData(String... args) {
        // Fetch only once for entire application, the form wont change
        if (mergedItemFieldsLiveData.getValue() == null || mergedItemFieldsLiveData.getValue().getData() == null) {
            initRequest(Request.Method.OPTIONS, getUrl(context, false, "actualitem/"));
            setData();
        }

        return mergedItemFieldsLiveData;
    }



    @Override
    protected void handleNetworkResponse(JSONObject payload) {
        try {
            payload = payload.getJSONObject("actions").getJSONObject("POST");
            Map<String, MergedItemField> mergedItemFieldsSet = new HashMap<>();
            Iterator<String> iterator = payload.keys();

            while (iterator.hasNext()) {
                String key = iterator.next();
                mergedItemFieldsSet.put(key, new MergedItemField(key, payload.getJSONObject(key)));
            }
            mergedItemFieldsLiveData.postSuccess(mergedItemFieldsSet);
        } catch (JSONException error) {
            mergedItemFieldsLiveData.postError(error);
        }
    }

    @Override
    protected void handleErrorResponse(Exception error) {
        mergedItemFieldsLiveData.postError(error);
    }

    @Override
    protected void setData() {
        mergedItemFieldsLiveData.postFetching();
        launchRequest();
    }
}

