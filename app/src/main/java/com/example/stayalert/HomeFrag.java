package com.example.stayalert;

import android.os.Bundle;

import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

///**
// * A simple {@link Fragment} subclass.
// * Use the {@link Home_Frag#newInstance} factory method to
// * create an instance of this fragment.
// */
public class HomeFrag extends Fragment {

    Button viewDetectionBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_, container, false);

        // Initialize the viewDetectionBtn by finding it from the inflated layout
        viewDetectionBtn = view.findViewById(R.id.viewDetectionBtn);

        // Set an OnClickListener for the viewDetectionBtn
        viewDetectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraActivity cameraActivity = (CameraActivity) getActivity();
                cameraActivity.changeFrameLayoutElevation(30);
            }
        });

        return view;
    }
}