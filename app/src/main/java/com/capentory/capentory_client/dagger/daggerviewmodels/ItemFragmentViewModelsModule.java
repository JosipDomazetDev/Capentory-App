package com.capentory.capentory_client.dagger.daggerviewmodels;

import androidx.lifecycle.ViewModel;

import com.capentory.capentory_client.dagger.ViewModelKey;
import com.capentory.capentory_client.ui.ValidatedMergedItemsFragment;
import com.capentory.capentory_client.viewmodels.AttachmentsViewModel;
import com.capentory.capentory_client.viewmodels.DetailItemViewModel;
import com.capentory.capentory_client.viewmodels.HomeScreenViewModel;
import com.capentory.capentory_client.viewmodels.LoginViewModel;
import com.capentory.capentory_client.viewmodels.MergedItemViewModel;
import com.capentory.capentory_client.viewmodels.StocktakingViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class ItemFragmentViewModelsModule {

    @Binds
    @IntoMap
    @ViewModelKey(MergedItemViewModel.class)
    public abstract ViewModel bindItemFragmentViewModel(MergedItemViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(DetailItemViewModel.class)
    public abstract ViewModel bindDetailItemFragmentViewModel(DetailItemViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel.class)
    public abstract ViewModel bindLoginFragmentViewModel(LoginViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(StocktakingViewModel.class)
    public abstract ViewModel bindStocktakingFragmentViewModel(StocktakingViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(HomeScreenViewModel.class)
    public abstract ViewModel bindHomescreenFragmentViewModel(HomeScreenViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(AttachmentsViewModel.class)
    public abstract ViewModel bindAttachmentsFragmentViewModel(AttachmentsViewModel viewModel);

}
