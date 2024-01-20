package com.example.stayalert;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import firebase.classes.FirebaseDatabase;
import helper.classes.DialogHelper;

public class Sign_up extends AppCompatActivity {


    private static final String TAG = "Sign_up";
    EditText fName, mName, lName, suffix, age,contact,address,email,pass,conPass;
    TextView fNameErr, mNameErr,lNameErr,suffixErr, ageErr,contactErr,addressErr,emailErr,passErr,conPassErr;
    TextInputLayout emailLayout, passLayout, conPassLayout;
    Button signUpBtn;
    Dialog dialog;
    TextView dialogOkay, dialogTitle, dialogInfo;
    LottieAnimationView buttonAnimation;
    boolean offlineMode=false;
    Handler handler;
    FirebaseAuth auth;
    FirebaseFirestore db;
    Timer timer = new Timer();
    int signInRetries=0;
    String userId;
    GoogleSignInClient googleSignInClient;
    Button googleSignUp;
    private static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.
    private boolean showOneTapUI = true;
    String signUpMethod="email";
    String userEmail="";
    FirebaseDatabase firebaseDB;
    boolean cameraPermissionDialog=false, userDataExist=false;
    private DialogHelper dialogHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        firebaseDB = new FirebaseDatabase();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this,options);

        dialogHelper=new DialogHelper(Sign_up.this);
        TextView loginAccTV;

        loginAccTV = (TextView)findViewById(R.id.TVloginAcc);
        signUpBtn =  findViewById(R.id.BTNsignup);
        googleSignUp= findViewById(R.id.BTNgoogleSignup);

        buttonAnimation=findViewById(R.id.BTNSign_upLoading);
        fName = findViewById(R.id.ETfNameSignup);
        mName= findViewById(R.id.ETmInitialSignup);
        lName= findViewById(R.id.ETlNameSignup);
        suffix= findViewById(R.id.ETSuffixSignup);
        age= findViewById(R.id.ETAgeSignup);
        contact= findViewById(R.id.ETContactSignup);
        address= findViewById(R.id.ETAddressSignup);
        email= findViewById(R.id.ETemailSignup);
        pass= findViewById(R.id.ETpassSignup);
        conPass= findViewById(R.id.ETconPassSignup);
        emailLayout=findViewById(R.id.ETemailSignupLayout);
        passLayout= findViewById(R.id.ETpassSignupLayout);
        conPassLayout= findViewById(R.id.ETconPassSignupLayout);

        fNameErr=findViewById(R.id.TVFNameErr);
        mNameErr=findViewById(R.id.TVMNameErr);
        lNameErr=findViewById(R.id.TVLNameErr);
        suffixErr=findViewById(R.id.TVSuffixErr);
        ageErr=findViewById(R.id.TVAgeErr);
        addressErr=findViewById(R.id.TVAddressErr);
        emailErr=findViewById(R.id.TVEmailErr);
        passErr=findViewById(R.id.TVPassErrSignUp);
        conPassErr=findViewById(R.id.TVConPassErr);

        fName.setOnFocusChangeListener((v, hasFocus) -> handleEditTextFocusChange(fName, hasFocus));
        lName.setOnFocusChangeListener((v, hasFocus) -> handleEditTextFocusChange(lName, hasFocus));
        age.setOnFocusChangeListener((v, hasFocus) -> handleEditTextFocusChange(age, hasFocus));
        address.setOnFocusChangeListener((v, hasFocus) -> handleEditTextFocusChange(address, hasFocus));
        contact.setOnFocusChangeListener((v, hasFocus) -> handleEditTextFocusChange(contact, hasFocus));
        email.setOnFocusChangeListener((v, hasFocus) -> handleEditTextFocusChange(email, hasFocus));
        pass.setOnFocusChangeListener((v, hasFocus) -> handleEditTextFocusChange(pass, hasFocus));
        conPass.setOnFocusChangeListener((v, hasFocus) -> handleEditTextFocusChange(conPass, hasFocus));

        dialogHelper = new DialogHelper(this, new DialogHelper.DialogClickListener() {
            @Override
            public void onOkayClicked() {
                if(cameraPermissionDialog){
                    viewCameraPermission();
                    cameraPermissionDialog=false;
                }
            }

            @Override
            public void onActionClicked() {

            }

        });


        googleSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                googleSignInClient.signOut();
                if(!email.isEnabled()){creatAccountWithEmail();}
                Intent intent = googleSignInClient.getSignInIntent();
                startActivityForResult(intent,1234);
            }
        });



        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map<String, Object> userData = new HashMap<>();

                userData.put("first_name", fName.getText().toString());
                userData.put("middle_name", mName.getText().toString().trim().isEmpty() ? "" : mName.getText().toString());
                userData.put("last_name", lName.getText().toString());
                userData.put("suffix", suffix.getText().toString().trim().isEmpty() ? "" : suffix.getText().toString());
                userData.put("contact", "");
                userData.put("address", address.getText().toString());
                userData.put("age", age.getText().toString());
                userData.put("email", email.getText().toString());
                userData.put("password", pass.getText().toString());
                userData.put("sign_in_method", signUpMethod);


                if(!fName.getText().toString().trim().isEmpty()  && !lName.getText().toString().trim().isEmpty()
                        && !address.getText().toString().trim().isEmpty() && !age.getText().toString().trim().isEmpty()
                        && !email.getText().toString().trim().isEmpty()  && (!pass.getText().toString().trim().isEmpty() || passLayout.getVisibility()==View.GONE)
                        && (!conPass.getText().toString().trim().isEmpty() || conPassLayout.getVisibility()==View.GONE)){
                    if((pass.getText().toString().trim().length()<6 || conPass.getText().toString().trim().length()<6 ||
                            !Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) && signUpMethod.equals("email")){
                        if(!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()){
                            emailErr.setText("Invalid email*");
                            emailErr.setVisibility(View.VISIBLE);
                        }
                        if(pass.getText().toString().trim().length()<6 ){
                            passErr.setText("Password must be atleast 6 characters long*");
                            passErr.setVisibility(View.VISIBLE);
                        }
                        if(conPass.getText().toString().trim().length()<6 ){
                            conPassErr.setText("Password must be atleast 6 characters long*");
                            conPassErr.setVisibility(View.VISIBLE);
                        }


                    }else if(pass.getText().toString().trim().equals(conPass.getText().toString().trim())){
                        playLoadingAnim();
                        if(signInRetries<3){
                            switch (signUpMethod){
                                case "email":
                                    auth.createUserWithEmailAndPassword(email.getText().toString().trim(),pass.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if(task.isSuccessful()){
                                                String result = firebaseDB.writeUserInfo(userData);
                                                if(result!="success"){
                                                    signInUser();
                                                    userDataExist=true;
                                                }else{
                                                    dialogHelper.showDialog("Sign up Failed",result);
                                                    hideLoading();
                                                }
                                            }
                                            else if (task.getException().getLocalizedMessage().contains("network error")){
                                                // If sign in fails, handle different error scenarios
                                                dialogHelper.showDialog("Sign in Failed", "No connection to the database");
                                                hideLoading();
                                            }else{
                                                dialogHelper.showDialog("Sign in Failed", firebaseDB.failureDialog(task));
                                                hideLoading();
                                            }
                                        }
                                    });
                                    break;
                                case "google":
                                    String result = firebaseDB.writeUserInfo(userData);
                                    if(result=="success"){
                                        signInUser();
                                        userDataExist=true;
                                    }else{
                                        dialogHelper.showDialog("Sign up Failed",result);
                                        hideLoading();
                                    }
                                    break;
                            }

                        }
                        else{
                            dialogHelper.showDialog("Sign up Failed","Too much requests, please try again later");
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
                    addressErr.setVisibility(address.getText().toString().trim().isEmpty()?View.VISIBLE:View.GONE);
                    emailErr.setVisibility(email.getText().toString().trim().isEmpty()?View.VISIBLE:View.GONE);
                    passErr.setVisibility(pass.getText().toString().trim().isEmpty()?View.VISIBLE:View.GONE);
                    conPassErr.setVisibility(conPass.getText().toString().trim().isEmpty()?View.VISIBLE:View.GONE);
                    passErr.setText("Requried*");
                    conPassErr.setText("Requried*");
                    emailErr.setText("Requried*");

                }






            }
        });

        loginAccTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), Sign_in.class);
                startActivity(intent);
                auth.signOut();
                googleSignInClient.signOut();
                stopActivity();
            }
        });

        String email =getIntent().getStringExtra("email");
        if (!email.equals("")){
            createAccountWithGoogle(email);
        }


    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1234){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                userEmail = account.getEmail();
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
                FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    playLoadingAnim();
                                    isUserHasInfo();
                                }else if (!task.getException().getLocalizedMessage().contains("network error")){
                                    // If sign in fails, handle different error scenarios
                                    dialogHelper.showDialog("Sign up Failed",firebaseDB.failureDialog(task));
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialogHelper.showDialog("Sign up Failed",firebaseDB.onFailureDialog(e));
                            }
                        });

            } catch (ApiException e) {
                e.printStackTrace();
                Log.e("Sign up",e.getMessage());
                if(e.getStatusCode()==12500){
                    dialogHelper.showDialog("Sign in Failed", "No google play services installed");
                    hideLoading();
                }
                else if(e.getStatusCode()!=12501){
                    dialogHelper.showDialog("Sign in Failed", firebaseDB.onFailureDialog(e));
                    hideLoading();
                }

            }

        }

    }

    private void isUserHasInfo(){
        firebaseDB.readData("users", auth.getUid(),"default", new FirebaseDatabase.OnGetDataListener() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d(TAG, "DocumentSnapshot datas: " + documentSnapshot.getData());
                if(documentSnapshot.getData().isEmpty()){
                    createAccountWithGoogle(userEmail);
                    hideLoading();
                }else{
                    signInUser();
                }
            }

            @Override
            public void onStart() {
                Log.d(TAG, "Start getting user info");
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG,"Failed getting user info: "+e);
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
                signUpBtn.setText("");

            }
        });


    }


    public void createAccountWithGoogle(String email){
        passLayout.setVisibility(View.GONE);
        conPassLayout.setVisibility(View.GONE);
        this.email.setText(email);
        emailLayout.setEnabled(false);
        signUpMethod="google";
        dialogHelper.showDialog("Register Account","Set up your account information");

    }

    private  void creatAccountWithEmail(){
        passLayout.setVisibility(View.VISIBLE);
        conPassLayout.setVisibility(View.VISIBLE);
        this.email.setText("");
        emailLayout.setEnabled(true);
        signUpMethod="email";
    }



    public void signInUser(){
        if(ifHasCameraPermission()){
            Intent intent = new Intent(getApplicationContext(), DetectorActivity.class);
            startActivity(intent);
            stopActivity();
        }else{
            dialogHelper.showDialog("Sign up Failed", "You need to allow permission for camera usage as it is required for detection");
            hideLoading();
            cameraPermissionDialog=true;
        }
    }

    private void stopActivity(){
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

   public void stopSignInTimeout(){
       timer.schedule(new TimerTask() {
           @Override
           public void run() {
               signInRetries=0;
           }
       }, 10000);
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

    public boolean ifHasCameraPermission(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            return false;
        }
        return true;
    }

    public void viewCameraPermission(){
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA},100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            System.out.println("ok cam");
            // Check if the CAMERA permission is granted after the user responds to the permission request
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(userDataExist){
                    signInUser();
                }
            } else {
                dialogHelper.showDialog("Sign up Failed", "You need to allow permission for camera usage as it is required for detection");
                hideLoading();
            }
        }
    }
}