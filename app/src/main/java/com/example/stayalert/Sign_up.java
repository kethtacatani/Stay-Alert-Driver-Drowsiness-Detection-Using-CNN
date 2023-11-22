package com.example.stayalert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.vishnusivadas.advanced_httpurlconnection.PutData;

public class Sign_up extends AppCompatActivity {

    EditText fName, mName, lName, suffix, age,contact,address,email,username,pass,conPass;
    TextView fNameErr, mNameErr,lNameErr,suffixErr, ageErr,contactErr,addressErr,emailErr, usernameErr,passErr,conPassErr;
    Button signUpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        TextView loginAccTV;


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



        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //Starting Write and Read data with URL
                        //Creating array for parameters
                        String[] field = new String[10];
                        field[0] = "first_name";
                        field[1] = "middle_name";
                        field[2] = "last_name";
                        field[3] = "suffix";
                        field[4] = "address";
                        field[5] = "age";
                        field[6] = "contact";
                        field[7] = "email";
                        field[8] = "username";
                        field[9] = "password";

                        //Creating array for data
                        String[] data = new String[10];
                        data[0] = fName.getText().toString();
                        data[1] = mName.getText().toString().trim().isEmpty()?"":mName.getText().toString();
                        data[2] = lName.getText().toString();
                        data[3] = suffix.getText().toString().trim().isEmpty()?"":suffix.getText().toString();
                        data[4] = address.getText().toString();
                        data[5] = age.getText().toString();
                        data[6] = contact.getText().toString();
                        data[7] = email.getText().toString();
                        data[8] = username.getText().toString();
                        data[9] = pass.getText().toString();

                        if(!fName.getText().toString().trim().isEmpty()  && !lName.getText().toString().trim().isEmpty()
                                && !address.getText().toString().trim().isEmpty() && !age.getText().toString().trim().isEmpty() && !contact.getText().toString().trim().isEmpty()
                                && !email.getText().toString().trim().isEmpty() && !username.getText().toString().trim().isEmpty() && !pass.getText().toString().trim().isEmpty()
                                && !conPass.getText().toString().trim().isEmpty()){
                            if(pass.getText().toString().trim().equals(conPass.getText().toString().trim())){
                                PutData putData = new PutData("http://192.168.0.106/StayAlert/signup.php", "POST", field, data);
                                if (putData.startPut()) {
                                    if (putData.onComplete()) {
                                        String result = putData.getResult();
                                        //End ProgressBar (Set visibility to GONE)
                                        Log.i("PutData", result);
                                        System.out.println("sign up: "+result);
                                        if(result.equals("Sign Up Success")) {
                                            Intent intent = new Intent(getApplicationContext(), DetectorActivity.class);
                                            startActivity(intent);
                                        }
                                    }
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
            }
        });




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