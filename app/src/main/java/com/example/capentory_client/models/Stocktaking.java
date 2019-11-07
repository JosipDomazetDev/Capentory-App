package com.example.capentory_client.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.capentory_client.viewmodels.adapter.GenericDropDownAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Stocktaking implements GenericDropDownAdapter.DropDownEntry {

    private int stocktakingId;
    @NonNull
    private String name;
    @Nullable
    private String comment, date;


    public Stocktaking(JSONObject payload) throws JSONException {
        this.stocktakingId = payload.getInt("stocktake_id");
        this.name = payload.getString("name");
        this.comment = payload.getString("comment");
        this.date = getGermanDate(payload.getString("date_started"));
    }

    private static String getGermanDate(String fetchedDate) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM, yy");
        Date date;
        String ret = null;

        try {
            date = inputFormat.parse(fetchedDate);
            ret = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ret;
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

    @Nullable
    public String getDate() {
        return date;
    }

    @Override
    public String displayName() {
        return getName();
    }

    @Override
    public String displayDescription() {
        return getDate();
    }
}
