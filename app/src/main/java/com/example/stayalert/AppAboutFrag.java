package com.example.stayalert;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


public class AppAboutFrag extends Fragment {

    ImageButton closAppAbout;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_app_about, container, false);

        CameraActivity cameraActivity = (CameraActivity) getActivity();
        closAppAbout= view.findViewById(R.id.closeAppAbout);

        closAppAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraActivity.addFragment(new ProfileFrag());
            }
        });

        return view;
    }
}