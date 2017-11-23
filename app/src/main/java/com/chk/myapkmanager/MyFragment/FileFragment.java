package com.chk.myapkmanager.MyFragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chk.myapkmanager.Bean.MyFile;
import com.chk.myapkmanager.MyAdapter.FileAdapter;
import com.chk.myapkmanager.MyInterface.MyItemClickListener;
import com.chk.myapkmanager.PagerActivity;
import com.chk.myapkmanager.R;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static android.content.Context.STORAGE_SERVICE;

/**
 * Created by chk on 17-11-21.
 */

public class FileFragment extends Fragment {

    View mContentView;
    Context mContext;
    RecyclerView mFileRecyclerView;
    FileAdapter mFileAdapter;

    /**
     * 层级，用于防止返回至根目录
     */
    int layer = 1;
    String parentPath = null;
    ArrayList<MyFile> mMyFileList;
    ArrayList<String> mFileList;
    ArrayList<String> mRootPaths;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.from(getActivity()).inflate(R.layout.layout_fragment_file,container,false);
        init();
        return mContentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((PagerActivity)getActivity()).requestPermission();
    }

    void init() {
        dataView();
        viewInit();
    }

    void dataView() {
        mContext = getContext();
        mMyFileList = new ArrayList<>();
        mFileList = new ArrayList<>();
        mRootPaths = new ArrayList<>();

        mFileAdapter = new FileAdapter(mMyFileList);
        mFileAdapter.setItemClickListener(new MyItemClickListener() {
            @Override
            public void onClick(int position) {
                if (mMyFileList.get(position).isFolder())
                    openFolder(mMyFileList.get(position).getOriginalName());
                else if (mMyFileList.get(position).getFileName().contains(".apk")) {
                    installApk(mMyFileList.get(position).getOriginalName());
                }
            }
        });

    }

    void viewInit() {
        mFileRecyclerView = mContentView.findViewById(R.id.fileRecyclerView);
        mFileRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFileRecyclerView.setAdapter(mFileAdapter);

    }

    /**
     * 获取存储路径
     */
    public void getAllCardPath() {
        mFileList.clear();
        StorageManager sm = (StorageManager) getActivity().getSystemService(STORAGE_SERVICE);
        try {
            Method getVolumePaths = StorageManager.class.getMethod("getVolumePaths",null);
            String[] paths = (String[]) getVolumePaths.invoke(sm,null);
            for (String path:paths) {
                if (getStorageState(path).equals(Environment.MEDIA_MOUNTED)) {
                    mFileList.add(path);
                    mRootPaths.add(path);
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
     * 将String路劲转化为MyFile
     */
    public void pathToMyFile() {
        mMyFileList.clear();
        for (int i=0; i<mFileList.size(); i++) {
            MyFile myFile = new MyFile();
            File file = new File(mFileList.get(i));
            myFile.setOriginalName(file.getAbsolutePath());
            myFile.setFolder(file.isDirectory());
            myFile.setFileName(file.getName());
            mMyFileList.add(myFile);
        }
    }

    /**
     * 打开给定路径的文件夹
     * @param originalFileName 路径名
     */
    public void openFolder(String originalFileName) {
        layer++;
        mFileList.clear();
        File file = new File(originalFileName);
        parentPath = file.getParent();  //记录parent路径
        File[] files = file.listFiles();
        for (int i=0; i<files.length; i++) {
            mFileList.add(files[i].getAbsolutePath());
        }
        pathToMyFile();
        mFileAdapter.notifyDataSetChanged();
    }

    /**
     * 打开父路径，由Activity调用
     */
    public void openParent() {
        if (layer == 1)
            (getActivity()).finish();
        else if (layer == 2){
            refreshView();
            layer--;
        } else {
            mFileList.clear();
            File file = new File(parentPath);
            this.parentPath = file.getParent();  //记录parent路径
            File[] files = file.listFiles();
            if (files != null) {
                for (int i=0; i<files.length; i++) {
                    mFileList.add(files[i].getAbsolutePath());
                }
            }
            pathToMyFile();
            mFileAdapter.notifyDataSetChanged();
            layer--;
        }
    }


    /**
     * 刷新界面
     */
    public void refreshView() {
        getAllCardPath();
        pathToMyFile();
        mFileAdapter.notifyDataSetChanged();
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
