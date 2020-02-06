package com.capentory.capentory_client.ui;


import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.capentory.capentory_client.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;


public class ViewPagerFragment extends Fragment {


    // tab titles
    private String[] titles;

    public ViewPagerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_pager, container, false);
        //setContentView(binding.getRoot());
        titles = new String[]{"TODO", "DONE"};

        init(view);
        return view;
    }

    private void init(View view) {
        ViewPager2 viewPager2 = view.findViewById(R.id.view_pager_fragment_view_pager);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout_fragment_view_pager);

        // removing toolbar elevation
        //((Toolbar)view.findViewById(R.id.toolbar)).setElevation(0);
        StateListAnimator stateListAnimator = new StateListAnimator();
        stateListAnimator.addState(new int[0], ObjectAnimator.ofFloat(view, "elevation", 0));
        ((AppCompatActivity) getActivity()).getSupportActionBar().setElevation(0);


        viewPager2.setAdapter(new ViewPagerFragmentAdapter(getActivity()));

        // attaching tab mediator
        new TabLayoutMediator(tabLayout, viewPager2,
                (tab, position) -> tab.setText(titles[position])).attach();
    }


    private class ViewPagerFragmentAdapter extends FragmentStateAdapter {

        public ViewPagerFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new MergedItemsFragment();
                case 1:
                    return new ValidatedMergedItemsFragment();
            }
            return new MergedItemsFragment();
        }

        @Override
        public int getItemCount() {
            return titles.length;
        }
    }
}

/*
package com.capentory.capentory_client.ui;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.capentory.capentory_client.R;


*/
/**
 * A simple {@link Fragment} subclass.
 *//*

public class ViewPagerFragment extends Fragment {


    public ViewPagerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_pager, container, false);
    }

}
*/
