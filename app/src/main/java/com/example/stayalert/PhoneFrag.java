package com.example.stayalert;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class PhoneFrag extends Fragment {

    RecyclerView contactsRV;
    ContactsRecycleViewAdapter contactsRecycleViewAdapter;
    ArrayList<ContactsInfo> info = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_phone, container, false);

        contactsRV = view.findViewById(R.id.contactsRV);

        contactsRV.setHasFixedSize(false);
        linearLayoutManager = new LinearLayoutManager((CameraActivity)getActivity());
        contactsRV.setLayoutManager(linearLayoutManager);


        return view;
    }

//    public void displayDetectionLogs(){
//        info= cameraActivity.detectionLogsInfo;
//        if(info!=null && !info.isEmpty()){
//            progressBar.setVisibility(View.GONE);
//            recyleViewAdapter = new DetectionLogsRecycleViewAdapter(info, (CameraActivity)getActivity());
//            detectionLogsRV.setAdapter(recyleViewAdapter);
//            recyleViewAdapter.notifyDataSetChanged();
//        }else{
//            queryDetection();
//        }
//    }
}