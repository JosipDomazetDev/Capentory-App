package com.capentory.capentory_client.ui;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.capentory.capentory_client.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Objects;


public class ViewPagerFragment extends Fragment {


    private String[] titles;

    public ViewPagerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_pager, container, false);
        titles = new String[]{getString(R.string.todo_viewpager_fragment), getString(R.string.done_todo_viewpager_fragment)};

        init(view);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                ViewPagerFragment.this.handleOnBackPressed();
            }
        });

        return view;
    }

    public void handleOnBackPressed() {
        // new MaterialAlertDialogBuilder(context)
        new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                .setTitle(getString(R.string.title_onback_fragment_mergeditems))
                .setMessage(getString(R.string.msg_onback_fragment_mergeditems))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> NavHostFragment.findNavController(ViewPagerFragment.this).popBackStack())
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void init(View view) {
        ViewPager2 viewPager2 = view.findViewById(R.id.view_pager_fragment_view_pager);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout_fragment_view_pager);
        viewPager2.setUserInputEnabled(true);

        // removing toolbar elevation
        //((Toolbar)view.findViewById(R.id.toolbar)).setElevation(0);
        viewPager2.setAdapter(new ViewPagerFragmentAdapter(ViewPagerFragment.this));

        // attaching tab mediator
        new TabLayoutMediator
                (tabLayout, viewPager2,
                        (tab, position) -> tab.setText(titles[position])).attach();
    }


    private class ViewPagerFragmentAdapter extends FragmentStateAdapter {


        public ViewPagerFragmentAdapter(@NonNull Fragment fragment) {
            super(fragment);
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
