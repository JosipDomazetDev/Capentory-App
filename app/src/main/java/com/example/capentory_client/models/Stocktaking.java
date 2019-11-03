package com.example.capentory_client.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

public class Stocktaking {

    private int stocktakingId;
    @NonNull
    private String name;
    @Nullable
    private String comment;




    public Stocktaking(JSONObject payload) throws JSONException {
        this.stocktakingId = payload.getInt("stocktake_id");
        this.name = payload.getString("name");
        this.comment = payload.getString("comment");
    }


    public int getStocktakingId() {
        return stocktakingId;
    }

    @Nullable
    public String getComment() {
        return comment;
    }

    @NonNull
    public String getName() {
        return name;
    }
}
