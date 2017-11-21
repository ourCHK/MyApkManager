package com.chk.myapkmanager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.chk.myapkmanager.MyAdapter.MyPagerAdapter;
import com.chk.myapkmanager.MyFragment.ApkFragment;
import com.chk.myapkmanager.MyFragment.FileFragment;

import java.util.ArrayList;

public class PagerActivity extends AppCompatActivity {

    final static String TAG = "PAGER_ACTIVITY";

    ViewPager mViewPager;
    ArrayList<Fragment> mFragmentList;
    FileFragment mFileFragment;
    ApkFragment mApkFragment;
    MyPagerAdapter mMyPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void init() {
        dataInit();
        viewInit();
    }

    public void dataInit() {
        mFileFragment = new FileFragment();
        mApkFragment = new ApkFragment();
        mFragmentList = new ArrayList<>();
        mFragmentList.add(mFileFragment);
        mFragmentList.add(mApkFragment);
        mMyPagerAdapter = new MyPagerAdapter(getSupportFragmentManager(),mFragmentList);
    }

    public void viewInit() {
        mViewPager = (ViewPager) findViewById(R.id.myViewPager);
        mViewPager.setAdapter(mMyPagerAdapter);
    }

    public void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "the app need the permission tu run,please grant it", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            } else {
                mFileFragment.refreshView();
                Log.i(TAG,"the permission has been granted");
            }
        } else {
            mFileFragment.refreshView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length >0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mFileFragment.refreshView();
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
        if (mViewPager.getCurrentItem() == 0)
            mFileFragment.openParent();
        else
            super.onBackPressed();
    }
}
