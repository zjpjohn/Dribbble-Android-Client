package com.lucas.freeshots.ui.fragment;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lucas.freeshots.Dribbble.Dribbble;
import com.lucas.freeshots.Dribbble.DribbbleShot;
import com.lucas.freeshots.R;
import com.lucas.freeshots.common.Common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.lucas.freeshots.util.Util.$;


public class HomeFragment extends Fragment implements Serializable {
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        initTabLayoutAndViewPager(activity);
    }

    /**
     * 初始化TabLayout和ViewPager
     */
    private void initTabLayoutAndViewPager(Activity activity) {
        List<String> tabTitleList = new ArrayList<>();
        Resources res = getResources();

        tabTitleList.add(res.getString(R.string.recent));
        tabTitleList.add(res.getString(R.string.popular));
        if(Common.isLogin()) {
            tabTitleList.add(res.getString(R.string.following));
        }

        TabLayout tabLayout = $(activity, R.id.tab_layout);
        ViewPager viewPager = $(activity, R.id.view_pager);

        for(int i = 0; i < tabTitleList.size(); i++) {
            tabLayout.addTab(tabLayout.newTab().setText(tabTitleList.get(i)));
        }

        final List<Fragment> fragmentList = new ArrayList<>();

        DisplayShotsFragment recentShotsFragment = DisplayShotsFragment.newInstance("recentShotsFragment");
        recentShotsFragment.setSource((page) -> DribbbleShot.getShots(page, Dribbble.SHOT_SORT_BY_RECENT));
        fragmentList.add(recentShotsFragment);

        DisplayShotsFragment popularShotsFragment = DisplayShotsFragment.newInstance("popularShotsFragment");
        popularShotsFragment.setSource((page) -> DribbbleShot.getShots(page, Dribbble.SHOT_SORT_BY_VIEWS));
        fragmentList.add(popularShotsFragment);

        if(Common.isLogin()) {
            DisplayShotsFragment followingShotsFragment = DisplayShotsFragment.newInstance("followingShotsFragment");
            followingShotsFragment.setSource(DribbbleShot::getFollowingShots);
            fragmentList.add(followingShotsFragment);
        }

        PagerAdapter adapter = new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragmentList.get(position);
            }

            @Override
            public int getCount() {
                return fragmentList.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return tabTitleList.get(position);
            }
        };

        viewPager.setOffscreenPageLimit(2); // 设置缓存页面数量（实际有3个，缓存2个+正在显示的1个）
        viewPager.setAdapter(adapter);

        /*
         * 关联TabLayout与ViewPager
         * 同时也要覆写PagerAdapter的getPageTitle方法，否则Tab没有title
         */
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabsFromPagerAdapter(adapter);
    }
}
