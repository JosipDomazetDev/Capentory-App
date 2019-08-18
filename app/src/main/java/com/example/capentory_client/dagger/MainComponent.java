package com.example.capentory_client.dagger;

import android.app.Application;


import com.example.capentory_client.ui.BaseApplication;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;


@Component(
        modules = {
                AndroidSupportInjectionModule.class,
                ActivityBuildersModule.class
        })
public interface MainComponent extends AndroidInjector<BaseApplication> {

    @Component.Builder
    interface Builder{

        @BindsInstance
        Builder application(Application application);

       MainComponent build();
    }
}