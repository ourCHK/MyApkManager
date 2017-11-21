package com.chk.myapkmanager.MyFragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chk.myapkmanager.R;

/**
 * Created by chk on 17-11-21.
 */

public class ApkFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.from(getActivity()).inflate(R.layout.layout_fragment_apk,container,false);
    }
}
