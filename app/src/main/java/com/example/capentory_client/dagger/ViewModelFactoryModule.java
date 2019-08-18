package com.example.capentory_client.dagger;

import androidx.lifecycle.ViewModelProvider;

import com.example.capentory_client.viewmodels.ViewModelProviderFactory;

import dagger.Binds;
import dagger.Module;


@Module
public abstract class ViewModelFactoryModule {

    @Binds
    public abstract ViewModelProvider.Factory bindViewModelFactory(ViewModelProviderFactory viewModelFactory);

}