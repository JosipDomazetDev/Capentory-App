package com.example.capentory_client.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.capentory_client.viewmodels.adapter.GenericDropDownAdapter;

import org.json.JSONException;
import org.json.JSONObject;

public class Stocktaking implements GenericDropDownAdapter.DropDownEntry {

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

    @Override
    public String displayName() {
        return getName();
    }

    @Override
    public String displayDescription() {
        return getComment();
    }
}
