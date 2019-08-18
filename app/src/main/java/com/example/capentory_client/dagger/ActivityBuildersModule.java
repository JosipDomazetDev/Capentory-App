package com.example.capentory_client.dagger;

import com.example.capentory_client.ui.MainActivity;

import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBuildersModule {

    @ContributesAndroidInjector
    abstract MainActivity contributeMainActivity();

    @Provides static String someString(){return "xxxxxxsdeww";}
}
