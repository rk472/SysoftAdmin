package com.studio.smarters.sysoftadmin.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.studio.smarters.sysoftadmin.R;


public class SuccessFragment extends Fragment {
    private AppCompatActivity main;
    private View root;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        main=(AppCompatActivity)getActivity();
        main.getSupportActionBar().setTitle("Success");
        root=inflater.inflate(R.layout.fragment_success, container, false);

        return root;
    }


}
