package com.example.capentory_client.dagger.daggerviewmodels;

import androidx.lifecycle.ViewModel;

import com.example.capentory_client.dagger.ViewModelKey;
import com.example.capentory_client.viewmodels.RoomViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class RoomFragmentViewModelsModule {

    @Binds
    @IntoMap
    @ViewModelKey(RoomViewModel.class)
    public abstract ViewModel bindRoomFragmentViewModel(RoomViewModel viewModel);
}