package com.example.stayalert;

import android.content.Intent;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class ProfileFrag extends Fragment {
    ConstraintLayout profileLogout;
    Button cpuBtn,gpuBtn,nnapiBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileLogout =view.findViewById(R.id.profileLogout);
        cpuBtn=view.findViewById(R.id.cpu);
        gpuBtn=view.findViewById(R.id.gpu);
        nnapiBtn=view.findViewById(R.id.nnapi);

        CameraActivity cameraActivity = (CameraActivity) getActivity();

        cpuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraActivity.defaultDeviceIndex=0;
                cameraActivity.updateActiveModel();
            }
        });
        gpuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                cameraActivity.defaultDeviceIndex=1;
//                cameraActivity.updateActiveModel();
                gpuBtn.setText("Switch Cam");
                cameraActivity.rearCam=cameraActivity.rearCam?false:true;
                cameraActivity.setFragment();

            }
        });
        nnapiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraActivity.defaultDeviceIndex=2;
                cameraActivity.updateActiveModel();
            }
        });

        profileLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Sign_in.class);

                cameraActivity.stopActivity();
                cameraActivity.ringtone.stop();
                startActivity(intent);
            }
        });

        return view;


    }
}