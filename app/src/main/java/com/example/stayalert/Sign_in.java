package com.example.stayalert;

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

        db = new Database();
        db.isConnected();
//        db.checkConnection();


        TextView creatAccTV, usernameErrTV, passErrTV;

        EditText username, password;

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
                if(db.isConnected){
                    dialog.dismiss();
                }else{
                    Intent intent = new Intent(getApplicationContext(), DetectorActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        });



        creatAccTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Sign_up.class);
                startActivity(intent);
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



                Map<String, String> dataMap = new HashMap<>();

                dataMap.put("username", username.getText().toString());
                dataMap.put("password", password.getText().toString());

                if(!username.getText().toString().trim().isEmpty() && !password.getText().toString().trim().isEmpty()){
//                            PutData putData = new PutData("http://192.168.0.106/StayAlert/login.php", "POST", field, data);
                    buttonAnimation.setVisibility(View.VISIBLE);
                    buttonAnimation.playAnimation();
                    signInBtn.setText("");

                    String result= db.getData("users",dataMap);
                    if(result.equals("Login Success")){
                        Intent intent = new Intent(getApplicationContext(), DetectorActivity.class);
                                startActivity(intent);
                        finish();
                        hideLoading();
                    }
                    else if (result.equals("Username or Password is Wrong")){
                        showDialog("Login Failed","Username or password is incrorrect");
                        hideLoading();
                    }
                    else if (result.equals("No account exist")){
                        showDialog("Login Failed","Account does not exist");
                        hideLoading();
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

                    usernameErrTV.setVisibility(username.getText().toString().trim().isEmpty()?View.VISIBLE:View.GONE);
                    passErrTV.setVisibility(password.getText().toString().trim().isEmpty()?View.VISIBLE:View.GONE);
                }



            }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
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
        }, 2500);

//        if(!db.isConnected){
//            showDialog("Connection Failed","No database connection.\n Proceed to Offline mode?");
//
//        }



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




}