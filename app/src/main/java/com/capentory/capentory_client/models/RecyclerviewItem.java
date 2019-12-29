package com.capentory.capentory_client.models;

import androidx.annotation.NonNull;

public interface RecyclerviewItem {
    boolean applySearchBarFilter(@NonNull String filter);

    boolean isTopLevelRoom();

    boolean isExpanded();

    void setExpanded(boolean b);


}
