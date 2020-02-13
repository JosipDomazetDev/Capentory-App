package com.capentory.capentory_client;

import android.content.Context;

import androidx.lifecycle.ViewModelProvider;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.capentory.capentory_client.viewmodels.MergedItemViewModel;
import com.capentory.capentory_client.viewmodels.ViewModelProviderFactory;

import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Inject
    ViewModelProviderFactory providerFactory;

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.example.capentory_client", appContext.getPackageName());

        //MergedItemViewModel mergedItemViewModel = new ViewModelProvider(appContext.ge, providerFactory).get(MergedItemViewModel.class);
    }
}
