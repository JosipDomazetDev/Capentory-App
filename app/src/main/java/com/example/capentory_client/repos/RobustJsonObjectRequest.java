package com.example.capentory_client.repos;

import android.content.Context;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.capentory_client.androidutility.Cryptography;
import com.example.capentory_client.androidutility.PreferenceUtility;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RobustJsonObjectRequest extends JsonObjectRequest {

    private Context context;

    public RobustJsonObjectRequest(Context context, int method, String url, @Nullable JSONObject jsonRequest, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        this.context = context;
    }


    /**
     * Create a  POST JsonObjectRequest that will fetch data from the specified url with a body (e.g. for user/password)
     *
     * @param url Specify the url
     * @return JsonObjectRequest
     */
    protected RobustJsonObjectRequest(String url, Map<String, String> params, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener) {
        super(Request.Method.POST, url, new JSONObject(params), listener, errorListener);
    }


    /**
     * create JsonObjectRequest with empty body that will fetch data from the specified url
     *
     * @param method Specify the type of the request i.e. GET, POST ...
     * @param url    Specify the url
     * @return JsonObjectRequest
     */
    protected RobustJsonObjectRequest(int method, String url, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener) {
        super(method, url, null, listener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Token "
                + PreferenceUtility.getToken(context));
        headers.put("Connection", "close");
        return headers;
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        // 204 means empty body but response was successful (this is not handled by android per default)
        int EMPTY_BODY_CODE = 204;
        if (response.statusCode == EMPTY_BODY_CODE) {
            return Response.success(new JSONObject(), HttpHeaderParser.parseCacheHeaders(response));
        }

        return super.parseNetworkResponse(response);
    }
}
