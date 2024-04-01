package com.example.stayalert;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


public class PrivacyPolicyFrag extends Fragment {
    ImageButton closAppPrivacy;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_privacy_policy, container, false);


        closAppPrivacy= view.findViewById(R.id.closeAppPrivacy);
        CameraActivity cameraActivity =(CameraActivity) getActivity();

        closAppPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraActivity.addFragment(new ProfileFrag());
            }
        });

        return view;
    }
}