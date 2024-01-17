package com.example.stayalert;

import android.content.Intent;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import firebase.classes.FirebaseDatabase;


public class ProfileFrag extends Fragment {
    private static final String TAG = "ProfileFrag";
    ConstraintLayout profileLogout;
    Button cpuBtn,gpuBtn,nnapiBtn;
    TextView profileName;
    FirebaseFirestore db;
    FirebaseAuth auth;
    FirebaseDatabase firebaseDB;
    Map<String, Object> userData = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileLogout =view.findViewById(R.id.profileLogout);
        profileName = view.findViewById(R.id.profileName);

        firebaseDB = new FirebaseDatabase();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        CameraActivity cameraActivity = (CameraActivity) getActivity();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(cameraActivity, GoogleSignInOptions.DEFAULT_SIGN_IN);

        userData= cameraActivity.userInfo;
        if(!userData.isEmpty()){
            profileName.setText(userData.get("first_name").toString() + " " + userData.get("middle_name") + ". " + userData.get("last_name") + " " + userData.get("suffix"));
        }



        profileLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Sign_in.class);

                cameraActivity.stopActivity();
                cameraActivity.ringtone.stop();
                startActivity(intent);
                FirebaseAuth.getInstance().signOut();
                googleSignInClient.signOut();
            }
        });

        return view;


    }

}