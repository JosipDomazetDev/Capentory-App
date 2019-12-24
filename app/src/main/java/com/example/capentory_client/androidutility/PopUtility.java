package com.example.capentory_client.androidutility;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;

import androidx.annotation.Nullable;

public final class PopUtility {

    // Private constructor to prevent instantiation
    private PopUtility() {
        throw new UnsupportedOperationException();
    }


    public static Spanned getHTMLFromString(int id, String string, Context context) {
        return Html.fromHtml(extractFromId(id, string, context));
    }

    private static String extractFromId(int id, @Nullable String text, Context context) {
        if (text == null || text.isEmpty()) text = "N/A";
        return context.getString(id, text);
    }
}
