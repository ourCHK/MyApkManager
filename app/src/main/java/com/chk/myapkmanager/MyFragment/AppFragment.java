package com.chk.myapkmanager.MyFragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.chk.myapkmanager.Bean.MyApp;
import com.chk.myapkmanager.MyAdapter.AppAdapter;
import com.chk.myapkmanager.MyInterface.MyCheckedChangedListener;
import com.chk.myapkmanager.MyInterface.MyItemClickListener;
import com.chk.myapkmanager.MyInterface.MyItemLongClickListener;
import com.chk.myapkmanager.PagerActivity;
import com.chk.myapkmanager.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chk on 17-11-22.
 */

public class AppFragment extends Fragment {
    final static String TAG = "AppFragment";
    final static int SEARCH_APP_COMPLETED = 1;

    TextView searchingApps;
    Handler mHandler;
    Thread mThread;

    View mContentView;
    Context mContext;
    PackageReceiver mPackageReceiver;

    RecyclerView mAppRecyclerView;
    AppAdapter mAppAdapter;

    ArrayList<MyApp> mMyAppList;
    ArrayList<MyApp> mPendingRemovedList;

    android.view.ActionMode mActionMode;
    boolean isInActionMode = false;
    boolean isReView = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG,"onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        mPackageReceiver = new PackageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addDataScheme("package");
        mContext.registerReceiver(mPackageReceiver,filter);
        dataInit();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.from(getActivity()).inflate(R.layout.layout_fragment_app,container,false);
        return mContentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        init();
        Log.i(TAG,"isReView" + isReView);
    }

    @SuppressLint("HandlerLeak")
    void init() {
        viewInit();

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case SEARCH_APP_COMPLETED:
                        searchAppCompleted();
                        break;
                }
            }
        };

        mThread = new Thread(){
            @Override
            public void run() {
                getInstalledPackage();
                mHandler.sendEmptyMessage(SEARCH_APP_COMPLETED);
            }
        };
        mThread.start();
    }

    void dataInit() {
        mMyAppList = new ArrayList<>();
        mPendingRemovedList = new ArrayList<>();
        mAppAdapter = new AppAdapter(mMyAppList);
        Log.i("AppFragment","dataInit");
    }

    void viewInit() {
        searchingApps = mContentView.findViewById(R.id.searchingApps);
        mAppRecyclerView = mContentView.findViewById(R.id.appRecyclerView);
        mAppRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAppRecyclerView.setAdapter(mAppAdapter);
        mAppAdapter.setItemClickListener(new MyItemClickListener() {
            @Override
            public void onClick(int position) {
                if (isInActionMode) {

                }
            }
        });

        mAppAdapter.setItemLongClickListener(new MyItemLongClickListener() {
            @Override
            public void onLongClick(int position) {
                if (!isInActionMode)
                    ((PagerActivity) mContext).startActionMode(new MyCallback());
            }
        });

        mAppAdapter.setMyCheckedChangedListener(new MyCheckedChangedListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked, int position) {
                MyApp myApp = mMyAppList.get(position);
                if (isChecked) {
                    mMyAppList.get(position).setCheck(true);
                    mPendingRemovedList.add(myApp);
                } else {
                    mMyAppList.get(position).setCheck(false);
                    mPendingRemovedList.remove(myApp);
                }
            }
        });
        mAppAdapter.notifyDataSetChanged();
    }

    void getInstalledPackage() {
        mMyAppList.clear();
        PackageManager pm = mContext.getPackageManager();
        List<PackageInfo> piList = pm.getInstalledPackages(0);
        ArrayList<MyApp> tempUserList = new ArrayList<>();
        ArrayList<MyApp> tempSystemList = new ArrayList<>();
        for (PackageInfo pi:piList) {
            ApplicationInfo appInfo = pi.applicationInfo;
            MyApp myApp = new MyApp();
            myApp.setAppName(pm.getApplicationLabel(appInfo).toString());
            myApp.setPackageName(pi.packageName);
            myApp.setVersion(pi.versionName);
            myApp.setIcon(pm.getApplicationIcon(appInfo));
            if ((pi.applicationInfo.flags & pi.applicationInfo.FLAG_SYSTEM) <= 0) { //第三方应用
                myApp.setSystem(false);
                tempUserList.add(myApp);
            } else {    //系统预装应用
                myApp.setSystem(true);
                tempSystemList.add(myApp);
            }
        }
        mMyAppList.addAll(tempUserList);
        mMyAppList.addAll(tempSystemList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isReView  = true;
        Log.i(TAG,"OnDestoryView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"OnDestory");
        mContext.unregisterReceiver(mPackageReceiver);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG,"OnDetach");
    }

    void uninstall(MyApp myApp) {
        Intent uninstall_intent = new Intent();
        uninstall_intent.setAction(Intent.ACTION_DELETE);
        uninstall_intent.setData(Uri.parse("package:"+myApp.getPackageName()));
        startActivity(uninstall_intent);
    }

    public void exitActionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
            mActionMode = null;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            exitActionMode();
        }
    }


    private class MyCallback implements android.view.ActionMode.Callback {
        @Override
        public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete:
                    if (mPendingRemovedList.size() != 0) {
                        for(MyApp myApp:mPendingRemovedList) {
                            uninstall(myApp);
                        }
                        mode.finish();
                    }
//                    Toast.makeText(mContext, "de you want to delete the app??", Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }

        @Override
        public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.action_mode_menu,menu);
            isInActionMode = true;
            mActionMode = mode;
            mAppAdapter.setInActionMode(true);
            mAppAdapter.notifyDataSetChanged();
            return true;
        }

        @Override
        public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
            mPendingRemovedList.clear();
            return false;
        }

        @Override
        public void onDestroyActionMode(android.view.ActionMode mode) {
            isInActionMode  = false;
            mAppAdapter.setInActionMode(false);
            for (MyApp myApp:mMyAppList) {
                if (myApp.isCheck())
                    myApp.setCheck(false);
            }
            mAppAdapter.notifyDataSetChanged();
        }
    }

    void searchAppCompleted() {
        searchingApps.setVisibility(View.GONE);
        mAppAdapter.notifyDataSetChanged();
    }

    private class PackageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String packageName = intent.getDataString();
            switch(intent.getAction()) {
                case Intent.ACTION_PACKAGE_ADDED:
                    Toast.makeText(context, packageName+" has been installed", Toast.LENGTH_SHORT).show();
                    break;
                case Intent.ACTION_PACKAGE_REMOVED:
//                    Toast.makeText(context, packageName+" has been uninstalled", Toast.LENGTH_SHORT).show();

                    break;
            }
        }
    }
}
