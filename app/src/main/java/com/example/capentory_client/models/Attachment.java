package com.example.capentory_client.models;

import org.json.JSONException;
import org.json.JSONObject;

public class Attachment implements Comparable<Attachment> {
    private String url;
    private String desc;
    private boolean isPicture;

    public Attachment(JSONObject payload) throws JSONException {
        this.url = payload.getString("url");
        this.desc = payload.optString("description");


        isPicture = url.matches("(?i)(https?://.*\\.(?:png|jpg|jpeg|tiff|gif))");
    }

    public String getUrl() {
        return url;
    }

    public String getDesc() {
        return desc;
    }

    public boolean isPicture() {
        return isPicture;
    }


    @Override
    public int compareTo(Attachment that) {
        if (this.isPicture && !that.isPicture) {
            return 1;
        } else if (!this.isPicture && that.isPicture) {
            return -1;
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

}
