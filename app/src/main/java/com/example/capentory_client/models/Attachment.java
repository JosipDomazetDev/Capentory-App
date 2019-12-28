package com.example.capentory_client.models;

import android.content.Context;
import android.util.Log;

import com.example.capentory_client.R;
import com.example.capentory_client.repos.NetworkRepository;
import com.example.capentory_client.ui.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class Attachment implements Comparable<Attachment> {
    private String url;
    private String desc;
    private boolean isPicture;
    private int attachmentId;

    public Attachment(JSONObject payload) throws JSONException {
        this.url = payload.getString("url");
        this.desc = payload.optString("description");
        this.attachmentId = payload.optInt("id");

        isPicture = url.matches("(?i)(.*\\.(?:png|jpg|jpeg|jfif|tiff|gif))");
    }

    public int getAttachmentId() {
        return attachmentId;
    }

    public String getUrl(Context context) {
        Log.e("XXX", NetworkRepository.getUrl(context, false, url));
        return NetworkRepository.getNonJsonUrl(context, true, url);
    }

    public String getDescription() {
        return desc;
    }

    public boolean isPicture() {
        return isPicture;
    }


    @Override
    public int compareTo(Attachment that) {
        if (this.isPicture != that.isPicture) {
            return Boolean.compare(this.isPicture, that.isPicture);
        } else {
            if (this.url.compareTo(that.url) < 0) {
                return -1;
            } else if (this.url.compareTo(that.url) > 0) {
                return 1;
            }

            if (this.desc.compareTo(that.desc) < 0) {
                return -1;
            } else if (this.desc.compareTo(that.desc) > 0) {
                return 1;
            }
        }
        return 0;
    }

    public String getDisplayDescription(Context context) {
        if (desc.isEmpty()) {
            return context.getString(R.string.empty_desc_fragment_attachment);
        } else return desc;
    }
}