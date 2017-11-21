package com.chk.myapkmanager.MyAdapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * Created by chk on 17-11-21.
 */

public class MyPagerAdapter extends FragmentStatePagerAdapter{

    ArrayList<Fragment> mFragmentList;

    public MyPagerAdapter(FragmentManager fm, ArrayList<Fragment> mFragmentList) {
        super(fm);
        this.mFragmentList = mFragmentList;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }
}
