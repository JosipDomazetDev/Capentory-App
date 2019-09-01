package com.example.capentory_client.dagger.daggerviewmodels;

import androidx.lifecycle.ViewModel;

import com.example.capentory_client.dagger.ViewModelKey;
import com.example.capentory_client.viewmodels.MergedItemFragmentViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class ItemFragmentViewModelsModule {

    @Binds
    @IntoMap
    @ViewModelKey(MergedItemFragmentViewModel.class)
    public abstract ViewModel bindItemFragmentViewModel(MergedItemFragmentViewModel viewModel);
}
