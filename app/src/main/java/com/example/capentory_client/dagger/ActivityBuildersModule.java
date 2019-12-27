package com.example.capentory_client.dagger;

import com.example.capentory_client.dagger.daggerviewmodels.ItemFragmentViewModelsModule;
import com.example.capentory_client.dagger.daggerviewmodels.RoomFragmentViewModelsModule;
import com.example.capentory_client.ui.AttachmentsFragment;
import com.example.capentory_client.ui.HomeScreenFragment;
import com.example.capentory_client.ui.RoomsFragment;
import com.example.capentory_client.ui.DetailedItemFragment;
import com.example.capentory_client.ui.LoginFragment;
import com.example.capentory_client.ui.MainActivity;
import com.example.capentory_client.ui.MergedItemsFragment;
import com.example.capentory_client.ui.StocktakingFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBuildersModule {

    @ContributesAndroidInjector
    abstract MainActivity contributeMainActivity();

    @ContributesAndroidInjector(
            modules = {RoomFragmentViewModelsModule.class})
    abstract RoomsFragment contributeRoomFragment();

    @ContributesAndroidInjector(
            modules = {ItemFragmentViewModelsModule.class})
    abstract MergedItemsFragment contributeItemFragment();


    @ContributesAndroidInjector(
            modules = {ItemFragmentViewModelsModule.class})
    abstract DetailedItemFragment contributeDetailItemFragment();

    @ContributesAndroidInjector(
            modules = {ItemFragmentViewModelsModule.class})
    abstract LoginFragment contributeLoginFragment();

    @ContributesAndroidInjector(
            modules = {ItemFragmentViewModelsModule.class})
    abstract StocktakingFragment contributeStocktakingFragment();

    @ContributesAndroidInjector(
            modules = {ItemFragmentViewModelsModule.class})
    abstract HomeScreenFragment contributeHomescreenFragment();

    @ContributesAndroidInjector(
            modules = {ItemFragmentViewModelsModule.class})
    abstract AttachmentsFragment contributeAttachementsFragment();
}
