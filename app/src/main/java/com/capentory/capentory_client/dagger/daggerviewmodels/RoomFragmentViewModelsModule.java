package com.capentory.capentory_client.dagger.daggerviewmodels;

import androidx.lifecycle.ViewModel;

import com.capentory.capentory_client.dagger.ViewModelKey;
import com.capentory.capentory_client.viewmodels.RoomsViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class RoomFragmentViewModelsModule {

    @Binds
    @IntoMap
    @ViewModelKey(RoomsViewModel.class)
    public abstract ViewModel bindRoomFragmentViewModel(RoomsViewModel viewModel);
}