package com.capentory.capentory_client.androidutility;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;

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
