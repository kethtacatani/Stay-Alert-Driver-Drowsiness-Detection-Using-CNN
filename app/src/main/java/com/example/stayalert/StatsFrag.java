package com.example.stayalert;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class StatsFrag extends Fragment {
    RecyclerView detectionLogsRV;
    LinearLayoutManager linearLayoutManager;
    ArrayList<DetectionLogsInfo> info = new ArrayList<>();
    DetectionLogsRecycleViewAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_stats, container, false);


        detectionLogsRV=view.findViewById(R.id.detectionLogsRV);
        detectionLogsRV.setHasFixedSize(false);

        info.add(new DetectionLogsInfo("Yawn","samplefilename","bentig","50","89","title"));
        info.add(new DetectionLogsInfo("Drowsy","samplefilename","bentig","50","89","title"));

        linearLayoutManager = new LinearLayoutManager((CameraActivity)getActivity());
        detectionLogsRV.setLayoutManager(linearLayoutManager);
        adapter= new DetectionLogsRecycleViewAdapter(info, (CameraActivity)getActivity());
        detectionLogsRV.setAdapter(adapter);

        return view;
    }
}