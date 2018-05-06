package com.studio.smarters.sysoftadmin.fragments;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.studio.smarters.sysoftadmin.R;

public class BatchFragment extends Fragment {
    private AppCompatActivity main;
    private View root;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        main=(AppCompatActivity)getActivity();
        main.getSupportActionBar().setTitle("Batch");
        root=inflater.inflate(R.layout.fragment_batch, container, false);

        return root;
    }

}
