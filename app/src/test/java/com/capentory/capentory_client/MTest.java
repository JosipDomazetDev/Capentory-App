package com.capentory.capentory_client;

import android.content.Context;

import com.capentory.capentory_client.models.MergedItem;
import com.capentory.capentory_client.models.ValidationEntry;
import com.capentory.capentory_client.viewmodels.MergedItemViewModel;

import org.junit.Test;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class MTest {

    @Mock
    MergedItemViewModel mergedItemViewModel;
    Context context = mock(Context.class);

    @Test
    public void testInventoryLogic() {
        mergedItemViewModel = mock(MergedItemViewModel.class);
        mergedItemViewModel.getMergedItems();


        mergedItemViewModel.addValidationEntry(new ValidationEntry(MergedItem.createNewEmptyItem(context)));

        assertEquals(0,mergedItemViewModel.getValidationEntries().size());
    }
}
