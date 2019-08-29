package com.example.capentory_client.dagger;

import com.example.capentory_client.dagger.daggerviewmodels.ItemFragmentViewModelsModule;
import com.example.capentory_client.dagger.daggerviewmodels.RoomFragmentViewModelsModule;
import com.example.capentory_client.ui.ActualRoomsFragment;
import com.example.capentory_client.ui.MainActivity;
import com.example.capentory_client.ui.MergedItemsFragment;

import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBuildersModule {

    @ContributesAndroidInjector
    abstract MainActivity contributeMainActivity();

    @ContributesAndroidInjector(
            modules = {RoomFragmentViewModelsModule.class})
    abstract ActualRoomsFragment contributeRoomFragment();

    @ContributesAndroidInjector(
            modules = {ItemFragmentViewModelsModule.class})
    abstract MergedItemsFragment contributeItemFragment();



}
