package com.example.capentory_client.dagger;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private String currentString;

    public AppModule(String currentString) {
        this.currentString = currentString;
    }

    @Singleton
    @Provides
    static Context getContext(Application application) {
        return application.getApplicationContext();
    }
}
