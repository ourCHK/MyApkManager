package com.chk.myapkmanager.MyFragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.chk.myapkmanager.Bean.MyApk;
import com.chk.myapkmanager.MyAdapter.ApkAdapter;
import com.chk.myapkmanager.MyInterface.MyItemClickListener;
import com.chk.myapkmanager.R;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static android.content.Context.STORAGE_SERVICE;

/**
 * Created by chk on 17-11-21.
 */

public class ApkFragment extends Fragment {
    final static String TAG = "ApkFragment";
    final static int SEARCH_APK_COMPLETED = 1;

    Context mContext;
    PackageManager pm;

    TextView searchingApks;
    Handler mHandler;
    Thread mThread;

    View mContentView;
    ArrayList<String> mRootList;
    ArrayList<File> mApkFileList;

    ArrayList<MyApk> mMyApkList;
    RecyclerView mApkRecyclerView;
    ApkAdapter mApkAdapter;

    boolean isFirstOpen = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView =  inflater.from(getActivity()).inflate(R.layout.layout_fragment_apk,container,false);
        return mContentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isFirstOpen) {
            init();
            isFirstOpen = false;
        }
    }

    @SuppressLint("HandlerLeak")
    void init() {
        dataInit();
        viewInit();

        mThread = new Thread(){
            @Override
            public void run() {
                searchFile(mRootList.get(0),"apk");
                mHandler.sendEmptyMessage(SEARCH_APK_COMPLETED);
            }
        };

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case SEARCH_APK_COMPLETED:
                        searchApkCompleted();
                        break;
                }
            }
        };
        mThread.start();
    }

    void dataInit() {
        mContext = getContext();
        pm = mContext.getPackageManager();
        mRootList = new ArrayList<>();
        mApkFileList = new ArrayList<>();
        mMyApkList = new ArrayList<>();
        mApkAdapter = new ApkAdapter(mMyApkList);
        getAllCardPath();
    }

    void viewInit() {
        searchingApks = mContentView.findViewById(R.id.searchingApks);
        mApkRecyclerView = mContentView.findViewById(R.id.apkRecyclerView);
        mApkRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mApkRecyclerView.setAdapter(mApkAdapter);
        mApkAdapter.setItemClickListener(new MyItemClickListener() {
            @Override
            public void onClick(int position) {
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.setDataAndType(Uri.parse("file://" + mMyApkList.get(position).getOriginalName()),"application/vnd.android.package-archive");
//                mContext.startActivity(intent);
                MyApk myApk = mMyApkList.get(position);
                if (myApk.getAppName() == null) {
                    Toast.makeText(mContext, "the package is broken", Toast.LENGTH_SHORT).show();
                } else {
                    installApk(myApk.getOriginalName());
                }
            }
        });
        mApkAdapter.notifyDataSetChanged();
    }

    public void searchFile(String path,String keyword) {
        File file = new File(path);
        if (file.isFile()) {
            String fileName = file.getName();
            int index = fileName.lastIndexOf(".");
            if (index != -1) {
                if (fileName.substring(index).equals(keyword) || fileName.substring(index).equals("."+keyword)) {
                    mApkFileList.add(file);
                }
            }
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for(int i=0; i<files.length; i++) {
                    searchFile(files[i].getAbsolutePath(),keyword);
                }
            }
        }
    }

    void searchApkCompleted() {
        searchingApks.setVisibility(View.GONE);
        for(File file: mApkFileList) {
            apkInfo(file.getAbsolutePath(),mContext);
        }
        mApkAdapter.notifyDataSetChanged();
    }

    /**
     * 获取存储路径
     */
    public void getAllCardPath() {
        mRootList.clear();
        StorageManager sm = (StorageManager) getActivity().getSystemService(STORAGE_SERVICE);
        try {
            Method getVolumePaths = StorageManager.class.getMethod("getVolumePaths",null);
            String[] paths = (String[]) getVolumePaths.invoke(sm,null);
            for (String path:paths) {
                if (getStorageState(path).equals(Environment.MEDIA_MOUNTED)) {
                    mRootList.add(path);
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断存储器的状态
     * @param path 存储器路劲
     * @return
     */
    public String getStorageState(String path) {
        StorageManager sm = (StorageManager) getActivity().getSystemService(STORAGE_SERVICE);
        try {
            Method getVolumeState = StorageManager.class.getMethod("getVolumeState",new Class[] {String.class});
            String state = (String) getVolumeState.invoke(sm,path);
            return state;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return "null";
    }

    /**
     * 获取apk包的信息：版本号，名称，图标等
     * @param absPath apk包的绝对路径
     * @param context
     */
    public void apkInfo(String absPath,Context context) {

//        PackageManager pm = context.getPackageManager();
        PackageInfo pkgInfo = pm.getPackageArchiveInfo(absPath,PackageManager.GET_ACTIVITIES);
        File file = new File(absPath);
        MyApk myApk = new MyApk();
        myApk.setFile(file);
        myApk.setFileName(file.getName());
        myApk.setOriginalName(absPath);
        if (pkgInfo != null) {
            ApplicationInfo appInfo = pkgInfo.applicationInfo;
        /* 必须加这两句，不然下面icon获取是default icon而不是应用包的icon */
            appInfo.sourceDir = absPath;
            appInfo.publicSourceDir = absPath;
            String appName = pm.getApplicationLabel(appInfo).toString();// 得到应用名
            String packageName = appInfo.packageName; // 得到包名
            String version = pkgInfo.versionName; // 得到版本信息
        /* icon1和icon2其实是一样的 */
            Drawable icon1 = pm.getApplicationIcon(appInfo);// 得到图标信息
            Drawable icon2 = appInfo.loadIcon(pm);
            String pkgInfoStr = String.format("PackageName:%s, Vesion: %s, AppName: %s", packageName, version, appName);
            Log.i(TAG, String.format("PkgInfo: %s", pkgInfoStr) + "  "+(icon1 == null));

            myApk.setAppName(appName);
            myApk.setPackageName(packageName);
            myApk.setVersion(version);
            myApk.setIcon(icon1);
        }
        mMyApkList.add(myApk);
    }

    public void installApk(String apkPath) {
        File file = new File(apkPath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri apkUri = FileProvider.getUriForFile(mContext,"com.chk.MyApkManager.FileProvider",file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri,"application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
        }
        mContext.startActivity(intent);
    }

}
