package com.example.capentory_client.repos;


import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.capentory_client.models.Attachment;
import com.example.capentory_client.models.Room;
import com.example.capentory_client.models.Stocktaking;
import com.example.capentory_client.ui.MainActivity;
import com.example.capentory_client.ui.errorhandling.CustomException;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AttachmentsRepository extends NetworkRepository<Attachment> {

    @Inject
    public AttachmentsRepository(Context context) {
        super(context);
    }


    @Override
    public StatusAwareLiveData<Attachment> fetchMainData(String... args) {
        addMainRequest(Request.Method.GET, getUrl(context, true, MainActivity.getSerializer(context).getItemUrl()));
        launchMainRequest();

        return mainContentRepoData;
    }


    @Override
    protected void handleMainSuccessfulResponse(String stringPayload) {
        try {
            mainContentRepoData.postSuccess(new Attachment(new JSONObject(stringPayload)));
        } catch (JSONException error) {
            mainContentRepoData.postError(error);
        }
    }

}
