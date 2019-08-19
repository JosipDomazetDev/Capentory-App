package com.example.capentory_client.dagger;

import com.example.capentory_client.dagger.daggerviewmodels.RoomFragmentViewModelsModule;
import com.example.capentory_client.ui.ActualRoomsFragment;
import com.example.capentory_client.ui.MainActivity;

import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBuildersModule {

    @ContributesAndroidInjector(
            modules = {RoomFragmentViewModelsModule.class})
    abstract MainActivity contributeMainActivity();

    @ContributesAndroidInjector(
            modules = {RoomFragmentViewModelsModule.class})
    abstract ActualRoomsFragment contributeRoomFragment();

    @Provides
    static String someString() {
        return "xxxxxxsdeww";
    }
}
