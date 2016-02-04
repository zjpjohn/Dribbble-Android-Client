package com.lucas.freeshots.ui;

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

import com.lucas.freeshots.R;
import com.lucas.freeshots.common.Dribbble;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.lucas.freeshots.util.Util.$;


public class HomeFragment extends Fragment implements Serializable {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //@Bind(R.id.tab_layout) TabLayout tabLayout;
    //@Bind(R.id.view_pager) ViewPager viewPager;

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
        tabTitleList.add(res.getString(R.string.following));

        TabLayout tabLayout = $(activity, R.id.tab_layout);
        ViewPager viewPager = $(activity, R.id.view_pager);

        tabLayout.addTab(tabLayout.newTab().setText(tabTitleList.get(0)));
        tabLayout.addTab(tabLayout.newTab().setText(tabTitleList.get(1)));
        tabLayout.addTab(tabLayout.newTab().setText(tabTitleList.get(2)));

        DisplayShotsFragment recentShotsFragment = DisplayShotsFragment.newInstance("recentShotsFragment");
        recentShotsFragment.setSource((page) -> Dribbble.downloadShots(page, Dribbble.SHOT_SORT_BY_RECENT));

        DisplayShotsFragment popularShotsFragment = DisplayShotsFragment.newInstance("popularShotsFragment");
        popularShotsFragment.setSource((page) -> Dribbble.downloadShots(page, Dribbble.SHOT_SORT_BY_VIEWS));

        DisplayShotsFragment followingShotsFragment = DisplayShotsFragment.newInstance("followingShotsFragment");
        followingShotsFragment.setSource(Dribbble::downloadFollowingShots);

        final List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.addAll(Arrays.asList(recentShotsFragment, popularShotsFragment, followingShotsFragment));

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