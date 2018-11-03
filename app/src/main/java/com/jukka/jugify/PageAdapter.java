package com.jukka.jugify;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PageAdapter extends FragmentStatePagerAdapter {


    int mNumOfTabs;

    public PageAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                UserTab userTab = new UserTab();
                return userTab;
            case 1:
                ListenTab listenTab = new ListenTab();
                return listenTab;
            case 2:
                ExploreTab exploreTab = new ExploreTab();
                return exploreTab;
            case 3:
                SearchTab searchTab = new SearchTab();
                return searchTab;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

}