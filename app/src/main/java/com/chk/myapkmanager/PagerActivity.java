package com.chk.myapkmanager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.chk.myapkmanager.MyAdapter.MyPagerAdapter;
import com.chk.myapkmanager.MyFragment.ApkFragment;
import com.chk.myapkmanager.MyFragment.AppFragment;
import com.chk.myapkmanager.MyFragment.FileFragment;

import java.util.ArrayList;

public class PagerActivity extends AppCompatActivity {

    final static String TAG = "PAGER_ACTIVITY";

    ViewPager mViewPager;
    TabLayout mTabLayout;

    ArrayList<Fragment> mFragmentList;
    FileFragment mFileFragment;
    ApkFragment mApkFragment;
    AppFragment mAppFragment;
    MyPagerAdapter mMyPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);
        viewInit();
        requestPermission();
    }

    public void dataInit() {
        mFileFragment = new FileFragment();
        mApkFragment = new ApkFragment();
        mAppFragment = new AppFragment();
        mFragmentList = new ArrayList<>();
        mFragmentList.add(mFileFragment);
        mFragmentList.add(mApkFragment);
        mFragmentList.add(mAppFragment);
        mMyPagerAdapter = new MyPagerAdapter(getSupportFragmentManager(),mFragmentList);
        mViewPager.setAdapter(mMyPagerAdapter);
    }

    public void viewInit() {
        mViewPager = (ViewPager) findViewById(R.id.myViewPager);

        mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    public void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "the app need the permission tu run,please grant it", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            } else {
//                mFileFragment.refreshView();
                dataInit();
                Log.i(TAG,"the permission has been granted");
            }
        } else {
//            mFileFragment.refreshView();
            dataInit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length >0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    mFileFragment.refreshView();
                    dataInit();
                    Log.i(TAG,"permission granted successfully");
                } else {
                    Toast.makeText(this, "sorry you do not have granted the permission to me ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() == 0) {
            if (mFileFragment.isInActionMode()) {
                super.onBackPressed();
            } else {
                mFileFragment.openParent();
            }
        }
        else
            super.onBackPressed();
    }

}




