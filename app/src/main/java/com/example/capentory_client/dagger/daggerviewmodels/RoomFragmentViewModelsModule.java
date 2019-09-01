package com.example.capentory_client.dagger.daggerviewmodels;

import androidx.lifecycle.ViewModel;

import com.example.capentory_client.dagger.ViewModelKey;
import com.example.capentory_client.viewmodels.RoomFragmentViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class RoomFragmentViewModelsModule {

    @Binds
    @IntoMap
    @ViewModelKey(RoomFragmentViewModel.class)
    public abstract ViewModel bindRoomFragmentViewModel(RoomFragmentViewModel viewModel);
}