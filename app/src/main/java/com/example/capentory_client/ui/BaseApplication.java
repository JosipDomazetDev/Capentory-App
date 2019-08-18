package com.example.capentory_client.ui;




import com.example.capentory_client.dagger.DaggerMainComponent;

import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;


import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;

public class BaseApplication extends DaggerApplication {

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        //return null;
        return DaggerMainComponent.builder().application(this).build();
    }
}
