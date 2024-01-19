package com.example.stayalert;

import android.content.Intent;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import firebase.classes.FirebaseDatabase;


public class ProfileFrag extends Fragment implements View.OnKeyListener{
    private static final String TAG = "ProfileFrag";
    ConstraintLayout profileLogout,edit, changePass,cancelSave,showInfoLayout, changPassLayout;
    LinearLayout toBeGone;
    TextInputEditText fName,mName,lName,suffix,contact,age,address,newPassword,conPassword, oldPassword;
    TextInputLayout fNameIL,mNameIL,lNameIL,suffixIL,contactIL,ageIL,addressIL,newPasswordIL,conPasswordIL, oldPasswordIL;
    TextView profileName, profileAddress, save, cancel;
    CircleImageView profileImage;
    ImageView editArrow, profileCam, changePassArrow;
    FirebaseFirestore db;
    FirebaseAuth auth;
    FirebaseDatabase firebaseDB;
    Map<String, Object> userData = new HashMap<>();
    CameraActivity cameraActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        fNameIL=view.findViewById(R.id.PE_FNameLayout);
        mNameIL=view.findViewById(R.id.PE_MNameLayout);
        lNameIL=view.findViewById(R.id.PE_LNameLayout);
        suffixIL=view.findViewById(R.id.PE_SuffixLayout);
        profileLogout =view.findViewById(R.id.profileLogout);
        edit = view.findViewById(R.id.profileEdit);
        contactIL=view.findViewById(R.id.PE_ContactLayout);
        ageIL=view.findViewById(R.id.PE_AgeLayout);
        addressIL=view.findViewById(R.id.PE_AddressLayout);
        newPasswordIL=view.findViewById(R.id.PE_PassLayout);
        conPasswordIL=view.findViewById(R.id.PE_ConPassLayout);
        oldPasswordIL=view.findViewById(R.id.PE_OldPassLayout);
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
        oldPassword=view.findViewById(R.id.PE_OldPass);
        newPassword=view.findViewById(R.id.PE_Pass);
        conPassword=view.findViewById(R.id.PE_ConPass);
        changePassArrow=view.findViewById(R.id.changPassArrow);
        changPassLayout=view.findViewById(R.id.ChangePassLayout);
        contact=view.findViewById(R.id.PE_Contact);

        fName.setOnKeyListener(this);
        mName.setOnKeyListener(this);
        lName.setOnKeyListener(this);
        suffix.setOnKeyListener(this);
        age.setOnKeyListener(this);
        contact.setOnKeyListener(this);
        address.setOnKeyListener(this);
        oldPassword.setOnKeyListener(this);
        newPassword.setOnKeyListener(this);
        conPassword.setOnKeyListener(this);
        


        firebaseDB = new FirebaseDatabase();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        cameraActivity = (CameraActivity) getActivity();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(cameraActivity, GoogleSignInOptions.DEFAULT_SIGN_IN);

        displayUserInfo();

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(toBeGone.getVisibility()==View.VISIBLE){
                    if(!userData.isEmpty()){
                        showInfo();
                    }else{
                        Toast.makeText(cameraActivity, "You are not signed in", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    displayTBG();
                }

            }
        });

        profileCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(toBeGone.getVisibility()==View.VISIBLE) {
                    if (!userData.isEmpty()) {
                        if (!userData.get("sign_in_method").toString().equals("google")) {
                            showChangePass();
                        } else {
                            Toast.makeText(cameraActivity, "You are signed in using a Google", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(cameraActivity, "You are not signed in", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    displayTBG();
                }
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(showInfoLayout.getVisibility()==View.VISIBLE){
                    Map<String, Object> userData = new HashMap<>();

                    userData.put("first_name", fName.getText().toString());
                    userData.put("middle_name", mName.getText().toString().trim().isEmpty() ? "" : mName.getText().toString());
                    userData.put("last_name", lName.getText().toString());
                    userData.put("suffix", suffix.getText().toString().trim().isEmpty() ? "" : suffix.getText().toString());
                    userData.put("contact", contact.getText().toString());
                    userData.put("address", address.getText().toString());
                    userData.put("age", age.getText().toString());

                    if(!isNoChanges()){
                        if(!fName.getText().toString().trim().isEmpty()  && !lName.getText().toString().trim().isEmpty()
                                && !address.getText().toString().trim().isEmpty() && !age.getText().toString().trim().isEmpty()){
                            String result=firebaseDB.upddateUserInfo(userData);
                            if(result=="success"){
                                displayTBG();
                                getUserInfo();
                            }else{
                                Toast.makeText(cameraActivity, "Error "+result, Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            fNameIL.setError((fName.getText().toString().trim().isEmpty())?"Required*":null);
                            lNameIL.setError((lName.getText().toString().trim().isEmpty())?"Required*":null);
                            addressIL.setError((address.getText().toString().trim().isEmpty())?"Required*":null);
                            ageIL.setError((age.getText().toString().trim().isEmpty())?"Required*":null);
                        }
                    }else{
                        Toast.makeText(cameraActivity, "No changes have been made", Toast.LENGTH_SHORT).show();
                    }

                }else if(changPassLayout.getVisibility()==View.VISIBLE){
                    Map<String, Object> userNewPass = new HashMap<>();

                    userNewPass.put("password", newPassword.getText().toString());

                    if(!passIsEmpty()){
                        if(oldPassword.getText().toString().equals(userData.get("password"))){
                            if(!oldPassword.getText().toString().equals(newPassword.getText().toString())){
                                if(newPassword.getText().toString().equals(conPassword.getText().toString())){
                                    if(userData.get("password").equals(newPassword.getText().toString())){
                                        String result =firebaseDB.upddateUserInfo(userNewPass);
                                        if(result.equals("success")){
                                            Toast.makeText(cameraActivity, "Password successfully changed", Toast.LENGTH_SHORT).show();
                                            displayTBG();
                                        }
                                    }
                                }else{
                                    newPasswordIL.setError("Password do not match*");
                                    conPasswordIL.setError("Password do not match*");
                                    newPasswordIL.setErrorIconDrawable(null);
                                    conPasswordIL.setErrorIconDrawable(null);
                                    newPasswordIL.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
                                    conPasswordIL.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
                                }
                            }else{
                                newPasswordIL.setError("New password matched the old one*");
                                conPasswordIL.setError("New password matched the old one*");
                                newPasswordIL.setErrorIconDrawable(null);
                                conPasswordIL.setErrorIconDrawable(null);
                                newPasswordIL.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
                                conPasswordIL.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
                            }
                        }else{
                            oldPasswordIL.setError("Wrong password");
                            oldPasswordIL.setErrorIconDrawable(null);
                            oldPasswordIL.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
                        }

                    }else{
                        oldPasswordIL.setError((oldPassword.getText().toString().trim().isEmpty())?"Required*":null);
                        newPasswordIL.setError((newPassword.getText().toString().trim().isEmpty())?"Required*":null);
                        conPasswordIL.setError((conPassword.getText().toString().trim().isEmpty())?"Required*":null);
                    }


                }


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

    public void getUserInfo(){
        firebaseDB.readData("users", auth.getUid(),"cache", new FirebaseDatabase.OnGetDataListener() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d(TAG, "DocumentSnapshot datas: " + documentSnapshot.getData());
                cameraActivity.userInfo=documentSnapshot.getData();
                displayUserInfo();
            }

            @Override
            public void onStart() {
                Log.d(TAG, "Start getting user info");
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG,"Failed getting user info: "+e);
                Toast.makeText(cameraActivity, "Unable to save user information", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private  void displayUserInfo(){
        userData= cameraActivity.userInfo;
        if(!userData.isEmpty()){
            profileName.setText(userData.get("first_name").toString() + " " + userData.get("middle_name") + ". " + userData.get("last_name") + " " + userData.get("suffix"));
            profileAddress.setText(userData.get("address").toString());
        }
    }



    public void showChangePass(){
        changPassLayout.setVisibility(View.VISIBLE);
        edit.setVisibility(View.GONE);
        toBeGone.setVisibility(View.GONE);
        oldPassword.setText("");
        newPassword.setText("");
        conPassword.setText("");
        oldPasswordIL.setErrorEnabled(false);
        newPasswordIL.setErrorEnabled(false);
        conPasswordIL.setErrorEnabled(false);

        cancelSave.setVisibility(View.VISIBLE);
        changePassArrow.setRotation(90);
    }

    public void showInfo(){
        fName.setText(userData.get("first_name").toString());
        mName.setText(userData.get("middle_name").toString());
        lName.setText(userData.get("last_name").toString());
        suffix.setText(userData.get("suffix").toString());
        contact.setText(userData.get("contact").toString());
        age.setText(userData.get("age").toString());
        address.setText(userData.get("address").toString());

        changePass.setVisibility(View.GONE);
        toBeGone.setVisibility(View.GONE);
        cancelSave.setVisibility(View.VISIBLE);
        showInfoLayout.setVisibility(View.VISIBLE);
        editArrow.setRotation(90);
        profileCam.setVisibility(View.VISIBLE);
    }

    public boolean isNoChanges(){
        if(fName.getText().toString().trim().equals(userData.get("first_name").toString())  && mName.getText().toString().trim().equals(userData.get("middle_name").toString())
                && lName.getText().toString().trim().equals(userData.get("last_name").toString()) && suffix.getText().toString().trim().equals(userData.get("suffix").toString())
                && contact.getText().toString().trim().equals(userData.get("contact").toString())  && address.getText().toString().trim().equals(userData.get("address").toString())
                && age.getText().toString().trim().equals(userData.get("age").toString())){
            return true;
        }
        return false;
    }

    public void displayTBG(){
        edit.setVisibility(View.VISIBLE);
        changePass.setVisibility(View.VISIBLE);
        toBeGone.setVisibility(View.VISIBLE);
        cancelSave.setVisibility(View.GONE);
        showInfoLayout.setVisibility(View.GONE);
        changPassLayout.setVisibility(View.GONE);
        profileCam.setVisibility(View.GONE);
        editArrow.setRotation(0);
        changePassArrow.setRotation(0);
    }

    public boolean passIsEmpty(){
        if(oldPassword.getText().toString().trim().isEmpty()  || newPassword.getText().toString().trim().isEmpty() || conPassword.getText().toString().trim().isEmpty()){
            return true;
        }
        return false;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (v.getId()) {
                case R.id.PE_FName:
                    fNameIL.setErrorEnabled(false);
                    break;
                case R.id.PE_LName:
                    lNameIL.setErrorEnabled(false);
                    break;
                case R.id.PE_Age:
                    ageIL.setErrorEnabled(false);
                    break;
                case R.id.PE_Address:
                    addressIL.setErrorEnabled(false);
                    break;
                case R.id.PE_OldPass:
                    oldPasswordIL.setErrorEnabled(false);
                    break;
                case R.id.PE_Pass:
                    newPasswordIL.setErrorEnabled(false);
                    break;
                case R.id.PE_ConPass:
                    conPasswordIL.setErrorEnabled(false);
                    break;
            }
        }

        return false;
    }
}