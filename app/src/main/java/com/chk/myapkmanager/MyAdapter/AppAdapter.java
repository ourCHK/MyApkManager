package com.chk.myapkmanager.MyAdapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.chk.myapkmanager.Bean.MyApp;
import com.chk.myapkmanager.MyInterface.MyCheckedChangedListener;
import com.chk.myapkmanager.MyInterface.MyItemClickListener;
import com.chk.myapkmanager.MyInterface.MyItemLongClickListener;
import com.chk.myapkmanager.R;

import java.util.ArrayList;

/**
 * Created by chk on 17-11-22.
 */

public class AppAdapter extends RecyclerView.Adapter{

    static final int SYSTEM_APP = 1;
    static final int USER_APP = 2;
    ArrayList<MyApp> mMyAppList;

    MyItemClickListener itemClickListener;
    MyItemLongClickListener itemLongClickListener;
    MyCheckedChangedListener myCheckedChangedListener;

    boolean isInActionMode;


    public AppAdapter(ArrayList<MyApp> mMyAppList) {
        this.mMyAppList = mMyAppList;
    }

    @Override
    public int getItemCount() {
        return mMyAppList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mMyAppList.get(position).isSystem()?SYSTEM_APP:USER_APP;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case SYSTEM_APP:
                View itemView_system = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_app_system,parent,false);
                SystemAppHolder systemAppHolder = new SystemAppHolder(itemView_system);
                return systemAppHolder;
            case USER_APP:
                View itemView_user = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_app_user,parent,false);
                UserAppHolder userAppHolder = new UserAppHolder(itemView_user);
                return userAppHolder;
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        MyApp myApp = mMyAppList.get(position);
        if (holder instanceof SystemAppHolder) {
            ((SystemAppHolder) holder).appImageView.setImageDrawable(myApp.getIcon());
            ((SystemAppHolder) holder).appName.setText(myApp.getAppName());
            ((SystemAppHolder) holder).version.setText(myApp.getVersion());
        } else if (holder instanceof UserAppHolder) {
            ((UserAppHolder) holder).appImageView.setImageDrawable(myApp.getIcon());
            ((UserAppHolder) holder).appName.setText(myApp.getAppName());
            ((UserAppHolder) holder).version.setText(myApp.getVersion());
            ((UserAppHolder) holder).checkApp.setTag(position);
            ((UserAppHolder) holder).checkApp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int pos = (int) buttonView.getTag();
                    if (myCheckedChangedListener != null) {
                        myCheckedChangedListener.onCheckedChanged(buttonView,isChecked,pos);
                    }
                }
            });
            if (myApp.isCheck())
                ((UserAppHolder) holder).checkApp.setChecked(true);
            else
                ((UserAppHolder) holder).checkApp.setChecked(false);
//            ((UserAppHolder) holder).checkApp.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                }
//            });
            if (isInActionMode)
                ((UserAppHolder) holder).checkApp.setVisibility(View.VISIBLE);
            else{
                ((UserAppHolder) holder).checkApp.setVisibility(View.INVISIBLE);
            }
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onClick(position);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (itemLongClickListener != null) {
                    itemLongClickListener.onLongClick(position);
                }
                return false;
            }
        });
    }


    public void setInActionMode(boolean inActionMode) {
        isInActionMode = inActionMode;
    }

    public void setItemClickListener(MyItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setItemLongClickListener(MyItemLongClickListener itemLongClickListener) {
        this.itemLongClickListener = itemLongClickListener;
    }

    public void setMyCheckedChangedListener(MyCheckedChangedListener myCheckedChangedListener) {
        this.myCheckedChangedListener = myCheckedChangedListener;
    }

    static class UserAppHolder extends RecyclerView.ViewHolder {

        ImageView appImageView;
        TextView appName;
        TextView version;
        CheckBox checkApp;

        public UserAppHolder(View itemView) {
            super(itemView);
            appImageView = itemView.findViewById(R.id.userAppImageView);
            appName = itemView.findViewById(R.id.userAppName);
            version = itemView.findViewById(R.id.version);
            checkApp = itemView.findViewById(R.id.checkApp);
        }
    }

    static class SystemAppHolder extends RecyclerView.ViewHolder {

        ImageView appImageView;
        TextView appName;
        TextView version;

        public SystemAppHolder(View itemView) {
            super(itemView);
            appImageView = itemView.findViewById(R.id.systemAppImageView);
            appName = itemView.findViewById(R.id.systemAppName);
            version = itemView.findViewById(R.id.version);
        }
    }
 }
