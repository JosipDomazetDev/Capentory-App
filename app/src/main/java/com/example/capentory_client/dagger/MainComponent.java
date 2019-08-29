package com.example.capentory_client.dagger;

import android.app.Application;


import com.example.capentory_client.dagger.daggerviewmodels.ItemFragmentViewModelsModule;
import com.example.capentory_client.dagger.daggerviewmodels.RoomFragmentViewModelsModule;
import com.example.capentory_client.ui.BaseApplication;
import com.example.capentory_client.viewmodels.ItemFragmentViewModel;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

@Singleton
@Component(
        modules = {
                AppModule.class,
                AndroidSupportInjectionModule.class,
                ActivityBuildersModule.class,
                ViewModelFactoryModule.class,
        })
public interface MainComponent extends AndroidInjector<BaseApplication> {

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);
        MainComponent build();
    }
}