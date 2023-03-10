package com.capentory.capentory_client.repos.customrequest;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.capentory.capentory_client.androidutility.PreferenceUtility;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RobustRequest extends JsonRequest<String> {
    private Context context;
    private boolean sendToken = true;


    public RobustRequest(Context context, int method, String url, @Nullable String requestBody, Response.Listener<String> listener, @Nullable Response.ErrorListener errorListener) {
        super(method, url, requestBody, listener, errorListener);
        Log.e("REQ", url);
        this.context = context;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        // 204 means empty body but response was successful (this is not handled by android per default)
        int EMPTY_BODY_CODE = 204;
        if (response.statusCode == EMPTY_BODY_CODE) {
            return Response.success("[]", HttpHeaderParser.parseCacheHeaders(response));
        }

        try {
            return Response.success(
                    new String(
                            response.data,
                            HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET)), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }


    public void disableAuthentication() {
        sendToken = false;
    }

    public void enableAuthentication() {
        sendToken = true;
    }


    @Override
    public Map<String, String> getHeaders() {
        if (sendToken) {
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Token "
                    + PreferenceUtility.getToken(context));
            headers.put("Connection", "close");
            headers.put("Accept-Language", Locale.getDefault().getLanguage());
            return headers;
        }

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return headers;
    }
}
