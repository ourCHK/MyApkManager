package com.chk.myapkmanager.MyAdapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chk.myapkmanager.Bean.MyApk;
import com.chk.myapkmanager.MyInterface.MyItemClickListener;
import com.chk.myapkmanager.R;

import java.util.ArrayList;

/**
 * Created by chk on 17-11-22.
 */

public class ApkAdapter extends RecyclerView.Adapter{

    ArrayList<MyApk> mMyApkList;
    MyItemClickListener mMyItemViewClickListener;

    public ApkAdapter(ArrayList<MyApk> mApkList) {
        this.mMyApkList = mApkList;
    }

    @Override
    public int getItemCount() {
        return mMyApkList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_apk,parent,false);
        return new ApkHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        MyApk myApk = mMyApkList.get(position);
        if (myApk.getIcon() != null) {
            ((ApkHolder) holder).apkName.setText(myApk.getPackageName());
            ((ApkHolder) holder).apkImageView.setImageDrawable(myApk.getIcon());
        }
        else {  //包出错了
            ((ApkHolder) holder).apkName.setText(myApk.getFileName());
        }
        if (mMyItemViewClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMyItemViewClickListener.onClick(position);
                }
            });
        }
    }

    static class ApkHolder extends RecyclerView.ViewHolder {

        public ImageView apkImageView;
        public TextView apkName;

        public ApkHolder(View itemView) {
            super(itemView);
            apkImageView = itemView.findViewById(R.id.apkImageView);
            apkName = itemView.findViewById(R.id.apkName);
        }
    }

    public void setItemClickListener(MyItemClickListener itemViewClickListener) {
        this.mMyItemViewClickListener = itemViewClickListener;
    }
}
