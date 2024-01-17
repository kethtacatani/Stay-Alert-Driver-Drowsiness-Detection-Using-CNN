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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import firebase.classes.FirebaseDatabase;


public class ProfileFrag extends Fragment {
    private static final String TAG = "ProfileFrag";
    ConstraintLayout profileLogout,edit, changePass,cancelSave,showInfoLayout;
    LinearLayout toBeGone;
    TextInputEditText fName,mName,lName,suffix,age,address;
    TextView profileName, profileAddress, save, cancel;
    CircleImageView profileImage;
    ImageView editArrow, profileCam;
    FirebaseFirestore db;
    FirebaseAuth auth;
    FirebaseDatabase firebaseDB;
    Map<String, Object> userData = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileLogout =view.findViewById(R.id.profileLogout);
        edit = view.findViewById(R.id.profileEdit);
        changePass=view.findViewById(R.id.profileChangePass);
        toBeGone=view.findViewById(R.id.profileTobeGone);
        cancelSave=view.findViewById(R.id.ProfileSaveCancel);
        profileName = view.findViewById(R.id.profileName);
        profileAddress = view.findViewById(R.id.profileAddress);
        save=view.findViewById(R.id.ProfileSave);
        cancel=view.findViewById(R.id.ProfileCancel);
        profileCam=view.findViewById(R.id.profileCam);
        profileImage=view.findViewById(R.id.profilePicIV);
        editArrow=view.findViewById(R.id.editArrow);
        showInfoLayout=view.findViewById(R.id.EditProfileLayout);
        fName=view.findViewById(R.id.PE_FName);
        mName=view.findViewById(R.id.PE_MName);
        lName=view.findViewById(R.id.PE_LName);
        suffix=view.findViewById(R.id.PE_Suffix);
        age=view.findViewById(R.id.PE_Age);
        address=view.findViewById(R.id.PE_Address);

        firebaseDB = new FirebaseDatabase();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        CameraActivity cameraActivity = (CameraActivity) getActivity();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(cameraActivity, GoogleSignInOptions.DEFAULT_SIGN_IN);

        userData= cameraActivity.userInfo;
        if(!userData.isEmpty()){
            profileName.setText(userData.get("first_name").toString() + " " + userData.get("middle_name") + ". " + userData.get("last_name") + " " + userData.get("suffix"));
            profileAddress.setText(userData.get("address").toString());
        }

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!userData.isEmpty()){
                    showInfo();
                }else{

                }

            }
        });
        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!userData.isEmpty()){
                    if(!userData.get("sign_in_method").toString().equals("google")){
                        //perform change pass
                    }else {
                        //cannot perform google signed in account
                    }
                }else{
                    //not logged in
                }
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayTBG();
            }
        });

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

    public void showInfo(){
        fName.setText(userData.get("first_name").toString());
        mName.setText(userData.get("middle_name").toString());
        lName.setText(userData.get("last_name").toString());
        suffix.setText(userData.get("suffix").toString());
        age.setText(userData.get("age").toString());
        address.setText(userData.get("address").toString());

        changePass.setVisibility(View.GONE);
        toBeGone.setVisibility(View.GONE);
        cancelSave.setVisibility(View.VISIBLE);
        showInfoLayout.setVisibility(View.VISIBLE);
        editArrow.setRotation(90);
        profileCam.setVisibility(View.VISIBLE);
    }

    public void displayTBG(){
        changePass.setVisibility(View.VISIBLE);
        toBeGone.setVisibility(View.VISIBLE);
        cancelSave.setVisibility(View.GONE);
        showInfoLayout.setVisibility(View.GONE);
        profileCam.setVisibility(View.GONE);
        editArrow.setRotation(0);
    }

}