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

import com.vishnusivadas.advanced_httpurlconnection.PutData;

public class Sign_in extends AppCompatActivity {

    Dialog dialog;
    TextView cancel, dialogTitle, dialogInfo;


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


        TextView creatAccTV, usernameErrTV, passErrTV;
        Button signInBtn;
        EditText username, password;

        creatAccTV = (TextView)findViewById(R.id.TVcreateAcc);
        signInBtn = ( Button)findViewById(R.id.BTNsignin);
        username = findViewById(R.id.ETusername);
        password = findViewById(R.id.ETpass);
        usernameErrTV = findViewById(R.id.TVusernameErr);
        passErrTV=findViewById(R.id.TVpassErr);

        dialog = new Dialog(Sign_in.this);
        dialog.setContentView(R.layout.pop_up_dialog);
        cancel = dialog.findViewById(R.id.TVCancel);
        dialogTitle = dialog.findViewById(R.id.TVTitle);
        dialogInfo = dialog.findViewById(R.id.TVInfo);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_bg));

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
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

                //Start ProgressBar first (Set visibility VISIBLE)
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //Starting Write and Read data with URL
                        //Creating array for parameters
                        String[] field = new String[2];
                        field[0] = "username";
                        field[1] = "password";
                        //Creating array for data
                        String[] data = new String[2];
                        data[0] = username.getText().toString();
                        data[1] = password.getText().toString();
                        if(!username.getText().toString().trim().isEmpty() && !password.getText().toString().trim().isEmpty()){
                            PutData putData = new PutData("http://192.168.0.106/StayAlert/login.php", "POST", field, data);
                            if (putData.startPut()) {
                                if (putData.onComplete()) {
                                    String result = putData.getResult();
                                    System.out.println("login: "+result);
                                    //End ProgressBar (Set visibility to GONE)
                                    if(result.equals("Login Success")){
                                        Intent intent = new Intent(getApplicationContext(), DetectorActivity.class);
                                        startActivity(intent);
                                    }
                                    else if (result.equals("Username or Password is Wrong")){
                                        showDialog("Login Failed","Username or password is incrorrect");
                                    }
                                    else if (result.equals("No account exist")){
                                        showDialog("Login Failed","Account does not exist");
                                    }
                                    else if (result.equals("Error: Database connection")){
                                        showDialog("Login Failed","Error occurred in the database");
                                    }
                                    else{
                                        showDialog("Login Failed","No database connection. Offline mode");
                                        Intent intent = new Intent(getApplicationContext(), DetectorActivity.class);
                                        startActivity(intent);
                                    }
                                }

                            }

                        }
                        else{

                            usernameErrTV.setVisibility(username.getText().toString().trim().isEmpty()?View.VISIBLE:View.GONE);
                            passErrTV.setVisibility(password.getText().toString().trim().isEmpty()?View.VISIBLE:View.GONE);
                        }
                        //End Write and Read data with URL
                    }
                });



            }
        });



    }

    public void showDialog(String title, String info){
        dialogTitle.setText(title);
        dialogInfo.setText(info);
        dialog.show();
    }




}