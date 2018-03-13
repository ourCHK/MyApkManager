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
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.chk.myapkmanager.Bean.MyApp;
import com.chk.myapkmanager.Bean.MyFile;
import com.chk.myapkmanager.MyAdapter.FileAdapter;
import com.chk.myapkmanager.MyInterface.MyCheckedChangedListener;
import com.chk.myapkmanager.MyInterface.MyItemClickListener;
import com.chk.myapkmanager.MyInterface.MyItemLongClickListener;
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

    ActionMode mActionMode;
    boolean isInActionMode = false;
    boolean isReView = false;

    /**
     * 层级，用于防止返回至根目录
     */
    int layer = 1;
    String parentPath = null;
    ArrayList<MyFile> mMyFileList;
    ArrayList<String> mFileList;
    ArrayList<String> mRootPaths;

    ArrayList<MyFile> mPendingOperation;  //用于存储准备操作的MyFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.from(getActivity()).inflate(R.layout.layout_fragment_file,container,false);
        return mContentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        init();
    }

    void init() {
        dataView();
        viewInit();
        refreshView();
    }

    void dataView() {
        mContext = getContext();
        mMyFileList = new ArrayList<>();
        mFileList = new ArrayList<>();
        mRootPaths = new ArrayList<>();
        mPendingOperation = new ArrayList<>();

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

        mFileAdapter.setItemLongClickListener(new MyItemLongClickListener() {
            @Override
            public void onLongClick(int position) {
                if (!isInActionMode) {
                    ((PagerActivity)mContext).startActionMode(new MyCallBack());
                }
            }
        });

        mFileAdapter.setMyCheckedChangedListener(new MyCheckedChangedListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked, int position) {
                MyFile myFile = mMyFileList.get(position);
                if (isChecked) {
                    mMyFileList.get(position).setCheck(true);
                    mPendingOperation.add(myFile);
                } else {
                    mMyFileList.get(position).setCheck(false);
                    mPendingOperation.remove(myFile);
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
        if (layer == 1) //根目录按返回键退出
            (getActivity()).finish();
        else if (layer == 2){   //根目录下一级目录直接刷新至根目录
            refreshView();
            layer--;
        } else {    //其他返回父目录
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


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            exitActionMode();
        }
    }

    public void exitActionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
            mActionMode = null;
        }
    }

    /**
     * 用于判断当前Fragment是否在ActionMode
     * @return
     */
    public boolean isInActionMode() {
        return isInActionMode;
    }

    private class MyCallBack implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.action_mode_share,menu);
            mActionMode = mode;
            isInActionMode = true;
            mFileAdapter.setInActionMode(true);
            mFileAdapter.notifyDataSetChanged();
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            mPendingOperation.clear();
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.share:
                    Toast.makeText(mContext, "you click the share button", Toast.LENGTH_SHORT).show();
                    mode.finish();
                    break;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            isInActionMode  = false;
            mFileAdapter.setInActionMode(false);
            for (MyFile myFile:mMyFileList) {
                if (myFile.isCheck())
                    myFile.setCheck(false);
            }
            mFileAdapter.notifyDataSetChanged();
        }
    }
}
