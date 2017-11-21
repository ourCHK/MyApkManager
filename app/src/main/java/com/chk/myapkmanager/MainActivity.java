package com.chk.myapkmanager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.chk.myapkmanager.Bean.MyFile;
import com.chk.myapkmanager.MyAdapter.FileAdapter;
import com.chk.myapkmanager.MyInterface.MyItemClickListener;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static final String TAG = "MainActivity";
    RecyclerView fileRecyclerView;
    FileAdapter mFileAdapter;
    TextView textView;

    ArrayList<MyFile> mMyFileList = new ArrayList<>();
    ArrayList<String> fileList = new ArrayList<>();
    ArrayList<String> apkPaths = new ArrayList<>();
    ArrayList<String> rootPaths = new ArrayList<>();
    String parentPath = null;
    /**
     * 层级，用于防止返回至根目录
     */
    int layer = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    public void init() {
        getAllCardPath();
        viewInit();
        requestPermission();    //请求权限
    }

    public void viewInit() {
        mFileAdapter = new FileAdapter(mMyFileList);
        mFileAdapter.setItemClickListener(new MyItemClickListener() {
            @Override
            public void onClick(int position) {
//                fileList.clear();
//                String originalFileName = mMyFileList.get(position).getOriginalName();
//                parentPath = originalFileName;  //记录parent路径
//                File file = new File(originalFileName);
//                File[] files = file.listFiles();
//                for (int i=0; i<files.length; i++) {
//                    fileList.add(files[i].getAbsolutePath());
//                }
//                pathToMyFile();
//                mFileAdapter.notifyDataSetChanged();
                openFolder(mMyFileList.get(position).getOriginalName());
            }
        });
        fileRecyclerView = (RecyclerView) findViewById(R.id.fileRecyclerView);
        fileRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        fileRecyclerView.setAdapter(mFileAdapter);


        textView = (TextView) findViewById(R.id.showText);
    }

//    public void listFile(String path) {
//        File file  = new File(path);
//        if (file == null)
//            return;
//        if (file.isDirectory()) {
//            File[] files = file.listFiles();
//            for (int i=0; files != null && i<files.length; i++) {
//                textView.append(files[i].toString()+"\n");
//            }
//        }
//    }

    /**
     * 获取存储的路径
     */
    public void getAllCardPath() {
        fileList.clear();
        StorageManager sm = (StorageManager) getSystemService(STORAGE_SERVICE);
        try {
            Method getVolumePaths = StorageManager.class.getMethod("getVolumePaths",null);
            String[] paths = (String[]) getVolumePaths.invoke(sm,null);
            for (String path:paths) {
                if (getStorageState(path).equals(Environment.MEDIA_MOUNTED)) {
                    fileList.add(path);
                    rootPaths.add(path);
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

//    public String getFirstCardPath() {
//        return paths[0];
//    }
//
//    public String getSecondCardPath() {
//        return paths[1];
//    }
//
//    public String getThirdCardPath() {
//        return paths[2];
//    }

    public String getStorageState(String path) {
        StorageManager sm = (StorageManager) getSystemService(STORAGE_SERVICE);
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

    public void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "the app need the permission tu run,please grant it", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            } else {
                updateMyFile();
                Log.i(TAG,"the permission has been granted");
            }
        } else {
            getAllCardPath();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length >0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateMyFile();
                    Log.i(TAG,"permission granted successfully");
                } else {
                    Toast.makeText(this, "sorry you do not have granted the permission to me ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    public void updateMyFile() {
        getAllCardPath();
        pathToMyFile();
        mFileAdapter.notifyDataSetChanged();
    }

    /**
     * 将String路劲转化为MyFile
     */
    public void pathToMyFile() {
        mMyFileList.clear();
        for (int i=0; i<fileList.size(); i++) {
            MyFile myFile = new MyFile();
            File file = new File(fileList.get(i));
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
        fileList.clear();
        File file = new File(originalFileName);
        parentPath = file.getParent();  //记录parent路径
        File[] files = file.listFiles();
        for (int i=0; i<files.length; i++) {
            fileList.add(files[i].getAbsolutePath());
        }
        pathToMyFile();
        mFileAdapter.notifyDataSetChanged();
    }

    public void openParent(String parentPath) {
        if (layer == 1)
            super.onBackPressed();
        else if (layer == 2){
            updateMyFile();
            layer--;
        } else {
            fileList.clear();
            File file = new File(parentPath);
            this.parentPath = file.getParent();  //记录parent路径
            File[] files = file.listFiles();
            for (int i=0; i<files.length; i++) {
                fileList.add(files[i].getAbsolutePath());
            }
            pathToMyFile();
            mFileAdapter.notifyDataSetChanged();
            layer--;
        }
    }

    @Override
    public void onBackPressed() {
        openParent(parentPath);
    }


}
