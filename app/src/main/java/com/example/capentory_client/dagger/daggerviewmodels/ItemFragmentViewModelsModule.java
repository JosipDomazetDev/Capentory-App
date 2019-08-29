package com.example.capentory_client.dagger.daggerviewmodels;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.capentory_client.dagger.ViewModelKey;
import com.example.capentory_client.viewmodels.ItemFragmentViewModel;
import com.example.capentory_client.viewmodels.ViewModelProviderFactory;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class ItemFragmentViewModelsModule {

    @Binds
    @IntoMap
    @ViewModelKey(ItemFragmentViewModel.class)
    public abstract ViewModel bindItemFragmentViewModel(ItemFragmentViewModel viewModel);
}
