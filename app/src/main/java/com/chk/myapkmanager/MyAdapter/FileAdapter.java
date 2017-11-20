package com.chk.myapkmanager.MyAdapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chk.myapkmanager.Bean.MyFile;
import com.chk.myapkmanager.R;

import java.util.ArrayList;

/**
 * Created by chk on 17-11-20.
 */

public class FileAdapter extends RecyclerView.Adapter{
    static final int FOLDER = 0;
    static final int FILE = 1;
    ArrayList<MyFile> mMyFileList;



    public FileAdapter(ArrayList<MyFile> mMyFileList) {
        this.mMyFileList = mMyFileList;
    }

    @Override
    public int getItemCount() {
        return mMyFileList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case FOLDER:
                View folderView  = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_folder,parent,false);
                FolderHolder folderHolder = new FolderHolder(folderView);
                return folderHolder;
            case FILE:
                View fileView  = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_file,parent,false);
                FolderHolder fileHolder = new FolderHolder(fileView);
                return fileHolder;
            default:
                return null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mMyFileList.get(position).isFolder() ? FOLDER : FILE;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MyFile myFile = mMyFileList.get(position);
        if (holder instanceof FolderHolder) {
            ((FolderHolder) holder).folderName.setText(myFile.getFileName());
        } else if (holder instanceof FileHolder) {
            ((FileHolder) holder).fileName.setText(myFile.getFileName());
        }
    }

    static class FolderHolder extends RecyclerView.ViewHolder {
        TextView folderIcon;
        TextView folderName;

        public FolderHolder(View itemView) {
            super(itemView);
            folderName = itemView.findViewById(R.id.folderName);
        }
    }

    static class FileHolder extends RecyclerView.ViewHolder {
        TextView fileIcon;
        TextView fileName;

        public FileHolder(View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.fileName);
        }
    }
}
