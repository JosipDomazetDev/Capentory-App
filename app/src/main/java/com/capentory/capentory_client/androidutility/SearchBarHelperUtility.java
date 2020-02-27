package com.capentory.capentory_client.androidutility;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;

import com.capentory.capentory_client.R;
import com.capentory.capentory_client.androidutility.UserUtility;
import com.capentory.capentory_client.viewmodels.adapter.GenericDropDownAdapter;

public final class SearchBarHelperUtility {

    // Private constructor to prevent instantiation
    private SearchBarHelperUtility() {
        throw new UnsupportedOperationException();
    }

    public static void bindSearchBar(@NonNull Menu menu, @NonNull MenuInflater inflater, Activity activity, SearchHandler searchHandler) {

        MenuItem searchItem = menu.findItem(R.id.search);
        searchItem.setVisible(true);

        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);



      /*  SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.getComponentName()));
        MenuItemCompat.setShowAsAction(searchItem,
                MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW |
                        MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setActionView(searchItem, searchView);
        searchView.setIconifiedByDefault(false);*/


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchHandler.onQueryTextSubmit(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchHandler.onQueryTextChange(newText);
                return false;
            }
        });
    }

    public interface SearchHandler{
        void onQueryTextSubmit(String query);
        void onQueryTextChange(String newText);
    }
}
