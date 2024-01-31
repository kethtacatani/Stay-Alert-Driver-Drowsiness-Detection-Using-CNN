package com.example.stayalert;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import firebase.classes.FirebaseDatabase;

///**
// * A simple {@link Fragment} subclass.
// * Use the {@link Home_Frag#newInstance} factory method to
// * create an instance of this fragment.
// */
public class HomeFrag extends Fragment {

    private static final String TAG = "HomeFrag";
    public static ImageButton viewDetectionBtn;
    TextView timeofDay;
    public static TextView statusDriverTV, nameDriverTV;
    FirebaseFirestore db;
    FirebaseAuth auth;
    FirebaseDatabase firebaseDB;
    Map<String, Object> userData = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        firebaseDB = new FirebaseDatabase();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        viewDetectionBtn = view.findViewById(R.id.viewDetectionBtn);
        timeofDay = view.findViewById(R.id.timeOfDayTV);
        statusDriverTV= view.findViewById(R.id.statusDriverTV);
        nameDriverTV=view.findViewById(R.id.nameDriverTV);
        CameraActivity cameraActivity = (CameraActivity) getActivity();
        userData= cameraActivity.userInfo;

        if(!userData.isEmpty()){
            nameDriverTV.setText(userData.get("first_name").toString() + " " + userData.get("middle_name") + ". " + userData.get("last_name") + " " + userData.get("suffix"));
        }
        statusDriverTV.setText( cameraActivity.statusDriver);
        // Set an OnClickListener for the viewDetectionBtn
        viewDetectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraActivity.elevation= 30;
                cameraActivity.changeFrameLayoutElevation();

            }
        });


        timeofDay.setText("Good "+getTimeOfDay());

        return view;
    }


    public String getTimeOfDay() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            return "morning";
        } else if(hour >= 12 && hour < 13) {
            return "noon";
        }else if (hour >= 13 && hour < 18) {
            return "afternoon";
        } else {
            return "evening";
        }
    }


}