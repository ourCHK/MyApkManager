package com.chk.myapkmanager.MyAdapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.chk.myapkmanager.Bean.MyFile;
import com.chk.myapkmanager.MyInterface.MyItemClickListener;
import com.chk.myapkmanager.R;

import java.util.ArrayList;

/**
 * Created by chk on 17-11-20.
 */

public class FileAdapter extends RecyclerView.Adapter{
    static final int FOLDER = 0;
    static final int FILE = 1;
    ArrayList<MyFile> mMyFileList;

    MyItemClickListener itemClickListener;

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
                FileHolder fileHolder = new FileHolder(fileView);
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        MyFile myFile = mMyFileList.get(position);
        if (holder instanceof FolderHolder) {
            ((FolderHolder) holder).folderName.setText(myFile.getFileName());
//            ((FolderHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    itemClickListener.onClick(position);
//                }
//            });
        } else if (holder instanceof FileHolder) {
            ((FileHolder) holder).fileName.setText(myFile.getFileName());
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.onClick(position);
            }
        });
    }

    public void setItemClickListener(MyItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
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
