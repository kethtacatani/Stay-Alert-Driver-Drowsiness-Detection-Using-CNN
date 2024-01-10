package com.example.stayalert;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android. os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

import firebase.classes.FirebaseDatabase;

public class Sign_in extends AppCompatActivity {

    Dialog dialog;
    TextView dialogOkay, dialogTitle, dialogInfo;
    Button signInBtn,googleBtn;
    FirebaseFirestore db;
    LottieAnimationView buttonAnimation;
    boolean offlineMode=false;
    Handler handler;
    Runnable connectivityCheckRunnable;
    FirebaseAuth mAuth;
    FirebaseUser user;
    TextView creatAccTV, usernameErrTV, passErrTV;
    GoogleSignInClient googleSignInClient;
    CollectionReference collection;
    EditText username, password;
    GoogleSignInAccount account;
    FirebaseDatabase firebaseDB;
    Map<String, Object> userData;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            userData = firebaseDB.getUserInfo();
            if(userData.isEmpty()){
                System.out.println("no info");
                signUpUser(currentUser.getEmail());
            }else{
                signInUser();
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseDB = new FirebaseDatabase();

        user = FirebaseAuth.getInstance().getCurrentUser();



        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this,options);
        googleBtn= findViewById(R.id.BTNgoogle);



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

        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = googleSignInClient.getSignInIntent();
                startActivityForResult(intent,1234);
            }
        });




        creatAccTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpUser("");
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




                if(!username.getText().toString().trim().isEmpty() && !password.getText().toString().trim().isEmpty()){
//                            PutData putData = new PutData("http://192.168.0.106/StayAlert/login.php", "POST", field, data);
                    playLoadingAnim();
                    mAuth.signInWithEmailAndPassword(email, pass)
                            .addOnCompleteListener(Sign_in.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        signInUser();
                                    } else if (!task.getException().getLocalizedMessage().contains("network error")){
                                        // If sign in fails, handle different error scenarios
                                        showDialog("Sign in Failed", firebaseDB.failureDialog(task));
                                        hideLoading();

                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    showDialog("Login Failed", "No connection to the database");
                                    hideLoading();
                                    System.out.println("error "+e.getMessage());
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

    private void playLoadingAnim() {

        Handler handler = new Handler(); // write in onCreate function
        handler.post(new Runnable() {
            @Override
            public void run() {
                buttonAnimation.setVisibility(View.VISIBLE);
                buttonAnimation.playAnimation();
                signInBtn.setText("");
            }
        });
    }

    private void signUpUser(String email) {
        Intent intent = new Intent(getApplicationContext(), Sign_up.class);
        intent.putExtra("email",email);
        startActivity(intent);
        stopActivity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1234){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {

                account = task.getResult(ApiException.class);

                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
                FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    isUserInfoExists();
                                    playLoadingAnim();
                                }else if (!task.getException().getLocalizedMessage().contains("network error")){
                                    // If sign in fails, handle different error scenarios
                                    showDialog("Sign in Failed", firebaseDB.failureDialog(task));
                                    hideLoading();

                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                showDialog("Sign in Failed", firebaseDB.onFailureDialog(e));
                                hideLoading();
                            }
                        });

            } catch (ApiException e) {
                e.printStackTrace();

            }

        }

    }

    public void isUserInfoExists(){
        DocumentReference docIdRef = db.collection("users").document(mAuth.getUid());
        docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        signInUser();
                    } else {
                        System.out.println("doesnt exists");
                        signUpUser(account.getEmail());
                    }
                }
            }
        });
    }




    public void signInUser(){
        user = mAuth.getCurrentUser();
        Intent intent = new Intent(getApplicationContext(), DetectorActivity.class);
        startActivity(intent);
        stopActivity();
    }



    public void showDialog(String title, String info){
        dialogTitle.setText(title);
        dialogInfo.setText(info);
        dialog.show();
    }

    public void hideLoading(){
        Toast.makeText(this, "hide", Toast.LENGTH_SHORT).show();
        buttonAnimation.setVisibility(View.GONE);
        buttonAnimation.pauseAnimation();
        signInBtn.setText("Sign in");
    }

    private void stopActivity(){
        finish();
    }





}