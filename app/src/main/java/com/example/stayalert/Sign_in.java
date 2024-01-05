package com.example.stayalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android. os.Bundle;
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
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.vishnusivadas.advanced_httpurlconnection.PutData;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Sign_in extends AppCompatActivity {

    Dialog dialog;
    TextView dialogOkay, dialogTitle, dialogInfo;
    Button signInBtn;
    Database db;
    LottieAnimationView buttonAnimation;
    boolean offlineMode=false;
    Handler handler;
    Runnable connectivityCheckRunnable;
    private FirebaseAuth mAuth;
    TextView creatAccTV, usernameErrTV, passErrTV;

    EditText username, password;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }



        //ADD CONNECTION CHECKER TO MYSQL DB
        //
        //#####################################################################

//        db = new Database();
//        db.isConnected();
//        db.checkConnection();

        mAuth = FirebaseAuth.getInstance();




        creatAccTV = (TextView)findViewById(R.id.TVcreateAcc);
        signInBtn = ( Button)findViewById(R.id.BTNsignin);
        buttonAnimation=findViewById(R.id.BTNloading);
        username = findViewById(R.id.ETusername);
        password = findViewById(R.id.ETpass);
        usernameErrTV = findViewById(R.id.TVusernameErr);
        passErrTV=findViewById(R.id.TVpassErr);

        dialog = new Dialog(Sign_in.this);
        dialog.setContentView(R.layout.pop_up_dialog);
        dialogOkay = dialog.findViewById(R.id.TVCancel);
        dialogTitle = dialog.findViewById(R.id.TVTitle);
        dialogInfo = dialog.findViewById(R.id.TVInfo);

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_bg));

        dialogOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
//                if(db.isConnected){
//                    dialog.dismiss();
//                }else{
//                    Intent intent = new Intent(getApplicationContext(), DetectorActivity.class);
//                    startActivity(intent);
//                    stopActivity();
//                }

            }
        });



        creatAccTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Sign_up.class);
                startActivity(intent);
                stopActivity();
            }
        });

        username.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    usernameErrTV.setVisibility(View.GONE);
                }
            }
        });

        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    passErrTV.setVisibility(View.GONE);
                }
            }
        });


        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = username.getText().toString().trim();
                String pass = password.getText().toString().trim();
                System.out.println("tentn"+mAuth.getTenantId());




                if(!username.getText().toString().trim().isEmpty() && !password.getText().toString().trim().isEmpty()){
//                            PutData putData = new PutData("http://192.168.0.106/StayAlert/login.php", "POST", field, data);
                    buttonAnimation.setVisibility(View.VISIBLE);
                    buttonAnimation.playAnimation();
                    signInBtn.setText("");

                    mAuth.signInWithEmailAndPassword(email, pass)
                            .addOnCompleteListener(Sign_in.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Toast.makeText(Sign_in.this, "Succ", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), DetectorActivity.class);

                                        startActivity(intent);
                                        stopActivity();
                                    } else {
                                        // If sign in fails, handle different error scenarios
                                        Exception exception = task.getException();
                                        if (exception instanceof FirebaseAuthInvalidUserException) {
                                            // Invalid user (user doesn't exist or is disabled)
                                            showDialog("Login Failed", "No account exist");
                                            hideLoading();
                                        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                                            // Invalid credentials provided (wrong password, etc.)
                                            showDialog("Login Failed", "Invalid username or password");
                                            hideLoading();
                                        } else {
                                            // Other errors occurred during sign-in
                                            showDialog("Login Failed", "No connection to database");
                                            hideLoading();
                                        }
                                    }
                                }
                            });

                }
                else{

                    usernameErrTV.setVisibility(username.getText().toString().trim().isEmpty()?View.VISIBLE:View.GONE);
                    passErrTV.setVisibility(password.getText().toString().trim().isEmpty()?View.VISIBLE:View.GONE);
                }



            }
        });



    }

    public void showDialog(String title, String info){
        dialogTitle.setText(title);
        dialogInfo.setText(info);
        dialog.show();
    }

    public void hideLoading(){
        buttonAnimation.setVisibility(View.GONE);
        buttonAnimation.pauseAnimation();
        signInBtn.setText("Sign in");
    }

    private void stopActivity(){
        handler.removeCallbacks(connectivityCheckRunnable);
        finish();
    }





}