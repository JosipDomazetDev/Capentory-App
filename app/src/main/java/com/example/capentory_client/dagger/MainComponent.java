package com.example.capentory_client.dagger;

import android.content.Context;

import com.example.capentory_client.repos.RalphRepository;
import com.example.capentory_client.ui.RoomFragment;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

@Singleton
@Component
interface MainComponent {
    void inject(MainActivity mainActivity);
    void inject(RoomFragment roomFragment);
    void inject(RalphRepository ralphRepository);

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder bindApplicationContext(Context context);

        MainComponent build();
    }
}