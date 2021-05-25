package com.project.android.transtalk.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.project.android.transtalk.R;
import com.project.android.transtalk.adapters.MainFragmentPagerAdapter;
import com.google.android.material.tabs.TabLayout;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {


    private TabLayout mTabLayout;
    private MainFragmentPagerAdapter mainFragmentPagerAdapter;
    private ViewPager mViewPager;
    private View mView;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView =  inflater.inflate(R.layout.fragment_home, container, false);
        mTabLayout = mView.findViewById(R.id.main_tab_layout);
        mViewPager = mView.findViewById(R.id.viewpager);
        mainFragmentPagerAdapter = new MainFragmentPagerAdapter(getContext(),getChildFragmentManager());
        mViewPager.setAdapter(mainFragmentPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.setCurrentItem(1,true);
        return mView;
    }

}
