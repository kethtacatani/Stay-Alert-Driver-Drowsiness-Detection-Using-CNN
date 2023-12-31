package com.example.stayalert;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.airbnb.lottie.LottieAnimationView;
import com.vishnusivadas.advanced_httpurlconnection.PutData;

import java.util.HashMap;
import java.util.Map;

public class Sign_up extends AppCompatActivity {

    EditText fName, mName, lName, suffix, age,contact,address,email,username,pass,conPass;
    TextView fNameErr, mNameErr,lNameErr,suffixErr, ageErr,contactErr,addressErr,emailErr, usernameErr,passErr,conPassErr;
    Button signUpBtn;
    Database db;
    Dialog dialog;
    TextView dialogOkay, dialogTitle, dialogInfo;
    LottieAnimationView buttonAnimation;
    boolean offlineMode=false;
    Handler handler;
    Runnable connectivityCheckRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        db= new Database();
        db.isConnected();

        TextView loginAccTV;

        dialog = new Dialog(Sign_up.this);
        dialog.setContentView(R.layout.pop_up_dialog);
        dialogOkay = dialog.findViewById(R.id.TVCancel);
        dialogTitle = dialog.findViewById(R.id.TVTitle);
        dialogInfo = dialog.findViewById(R.id.TVInfo);
        buttonAnimation=findViewById(R.id.BTNloading);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_bg));




        loginAccTV = (TextView)findViewById(R.id.TVloginAcc);
        signUpBtn =  findViewById(R.id.BTNsignup);

        fName = findViewById(R.id.ETfNameSignup);
        mName= findViewById(R.id.ETmInitialSignup);
        lName= findViewById(R.id.ETlNameSignup);
        suffix= findViewById(R.id.ETSuffixSignup);
        age= findViewById(R.id.ETAgeSignup);
        contact= findViewById(R.id.ETContactSignup);
        address= findViewById(R.id.ETAddressSignup);
        email= findViewById(R.id.ETemailSignup);
        username= findViewById(R.id.ETUsernameSignup);
        pass= findViewById(R.id.ETpassSignup);
        conPass= findViewById(R.id.ETconPassSignup);

        fNameErr=findViewById(R.id.TVFNameErr);
        mNameErr=findViewById(R.id.TVMNameErr);
        lNameErr=findViewById(R.id.TVLNameErr);
        suffixErr=findViewById(R.id.TVSuffixErr);
        ageErr=findViewById(R.id.TVAgeErr);
        contactErr=findViewById(R.id.TVContactErr);
        addressErr=findViewById(R.id.TVAddressErr);
        emailErr=findViewById(R.id.TVEmailErr);
        usernameErr=findViewById(R.id.TVUsernameErrSignUp);
        passErr=findViewById(R.id.TVPassErrSignUp);
        conPassErr=findViewById(R.id.TVConPassErr);

        fName.setOnFocusChangeListener((v, hasFocus) -> handleEditTextFocusChange(fName, hasFocus));
        lName.setOnFocusChangeListener((v, hasFocus) -> handleEditTextFocusChange(lName, hasFocus));
        age.setOnFocusChangeListener((v, hasFocus) -> handleEditTextFocusChange(age, hasFocus));
        address.setOnFocusChangeListener((v, hasFocus) -> handleEditTextFocusChange(address, hasFocus));
        contact.setOnFocusChangeListener((v, hasFocus) -> handleEditTextFocusChange(contact, hasFocus));
        email.setOnFocusChangeListener((v, hasFocus) -> handleEditTextFocusChange(email, hasFocus));
        username.setOnFocusChangeListener((v, hasFocus) -> handleEditTextFocusChange(username, hasFocus));
        pass.setOnFocusChangeListener((v, hasFocus) -> handleEditTextFocusChange(pass, hasFocus));
        conPass.setOnFocusChangeListener((v, hasFocus) -> handleEditTextFocusChange(conPass, hasFocus));

        dialogOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(db.isConnected){
                    dialog.dismiss();
                }else{
                    Intent intent = new Intent(getApplicationContext(), DetectorActivity.class);
                    startActivity(intent);
                    stopActivity();
                }

            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {



                        Map<String, String> dataMap = new HashMap<>();

                        dataMap.put("first_name", fName.getText().toString());
                        dataMap.put("middle_name", mName.getText().toString().trim().isEmpty() ? "" : mName.getText().toString());
                        dataMap.put("last_name", lName.getText().toString());
                        dataMap.put("suffix", suffix.getText().toString().trim().isEmpty() ? "" : suffix.getText().toString());
                        dataMap.put("address", address.getText().toString());
                        dataMap.put("age", age.getText().toString());
                        dataMap.put("contact", contact.getText().toString());
                        dataMap.put("email", email.getText().toString());
                        dataMap.put("username", username.getText().toString());
                        dataMap.put("password", pass.getText().toString());

//                        db.getData() to be continued ror

                        if(!fName.getText().toString().trim().isEmpty()  && !lName.getText().toString().trim().isEmpty()
                                && !address.getText().toString().trim().isEmpty() && !age.getText().toString().trim().isEmpty() && !contact.getText().toString().trim().isEmpty()
                                && !email.getText().toString().trim().isEmpty() && !username.getText().toString().trim().isEmpty() && !pass.getText().toString().trim().isEmpty()
                                && !conPass.getText().toString().trim().isEmpty()){
                            if(pass.getText().toString().trim().equals(conPass.getText().toString().trim())){
                                String result= db.loginSingupData("users",dataMap);
                                if(result.equals("Sign Up Success")) {
                                    Intent intent = new Intent(getApplicationContext(), DetectorActivity.class);
                                    startActivity(intent);
                                }
                                else if (result.equals("Error: Database connection")){
                                    showDialog("Login Failed","Error occurred in the database");
                                    hideLoading();
                                }
                                else if (result.equals("No database connection")){
                                    showDialog("Login Failed","No database connection.\n Proceed to Offline mode?");
                                    Intent intent = new Intent(getApplicationContext(), DetectorActivity.class);
//                                startActivity(intent);
                                    hideLoading();
                                }
                            }
                            else{
                                passErr.setText("Password do not match*");
                                conPassErr.setText("Password do not match*");
                                passErr.setVisibility(View.VISIBLE);
                                conPassErr.setVisibility(View.VISIBLE);
                            }
                        }
                        else{
                            fNameErr.setVisibility(fName.getText().toString().trim().isEmpty()?View.VISIBLE:View.GONE);
                            lNameErr.setVisibility(lName.getText().toString().trim().isEmpty()?View.VISIBLE:View.GONE);
                            ageErr.setVisibility(age.getText().toString().trim().isEmpty()?View.VISIBLE:View.GONE);
                            contactErr.setVisibility(contact.getText().toString().trim().isEmpty()?View.VISIBLE:View.GONE);
                            addressErr.setVisibility(address.getText().toString().trim().isEmpty()?View.VISIBLE:View.GONE);
                            emailErr.setVisibility(email.getText().toString().trim().isEmpty()?View.VISIBLE:View.GONE);
                            usernameErr.setVisibility(username.getText().toString().trim().isEmpty()?View.VISIBLE:View.GONE);
                            passErr.setVisibility(pass.getText().toString().trim().isEmpty()?View.VISIBLE:View.GONE);
                            conPassErr.setVisibility(conPass.getText().toString().trim().isEmpty()?View.VISIBLE:View.GONE);

                        }
                        //End Write and Read data with URL
                    }
                });



            }
        });

        loginAccTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Sign_in.class);
                startActivity(intent);
                stopActivity();
            }
        });

        handler = new Handler();

        connectivityCheckRunnable = new Runnable() {
            @Override
            public void run() {
                // Perform database connectivity check
                boolean isConnected = db.isConnected;
                db.isConnected();
                System.out.println("checking");

                // Handle the result as needed
                if (!isConnected) {
                    offlineMode=true;
                    if(!dialog.isShowing()){
                        showDialog("Connection Failed","No database connection.\n Proceed to Offline mode?");
                        System.out.println("showing");
                    }
                } else {
                    if(dialog.isShowing() && offlineMode){
                        dialog.dismiss();
                        offlineMode=false;
                    }
                }

                // Schedule the next check after 3 seconds
                handler.postDelayed(this, 3000); // 3000 milliseconds = 3 seconds
            }
        };
        handler.postDelayed(connectivityCheckRunnable, 2500);


    }

    private void stopActivity(){
        handler.removeCallbacks(connectivityCheckRunnable);
        finish();
    }

    public void showDialog(String title, String info){
        dialogTitle.setText(title);
        dialogInfo.setText(info);
        dialog.show();
    }

    public void hideLoading(){
        buttonAnimation.setVisibility(View.GONE);
        buttonAnimation.pauseAnimation();
        signUpBtn.setText("Sign in");
    }

    private void handleEditTextFocusChange(EditText editText, boolean hasFocus) {
        if (hasFocus) {
            switch (editText.getId()) {
                case R.id.ETfNameSignup:
                    fNameErr.setVisibility(View.GONE);
                    break;
                case R.id.ETlNameSignup:
                    lNameErr.setVisibility(View.GONE);
                    break;
                case R.id.ETAgeSignup:
                    ageErr.setVisibility(View.GONE);
                    break;
                case R.id.ETContactSignup:
                    contactErr.setVisibility(View.GONE);
                    break;
                case R.id.ETAddressSignup:
                    addressErr.setVisibility(View.GONE);
                    break;
                case R.id.ETemailSignup:
                    emailErr.setVisibility(View.GONE);
                    break;
                case R.id.ETUsernameSignup:
                    usernameErr.setVisibility(View.GONE);
                    break;
                case R.id.ETpassSignup:
                    passErr.setVisibility(View.GONE);
                    passErr.setText("Required*");
                    break;
                case R.id.ETconPassSignup:
                    conPassErr.setVisibility(View.GONE);
                    conPassErr.setText("Required*");
                    break;

            }
        }
    }
}