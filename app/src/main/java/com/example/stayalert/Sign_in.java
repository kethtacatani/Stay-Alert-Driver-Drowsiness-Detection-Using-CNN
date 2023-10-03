package com.example.stayalert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android. os.Bundle;
import android.view.View;
import android.widget.TextView;

public class Sign_in extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);


        TextView creatAccTV;

        creatAccTV = (TextView)findViewById(R.id.TVcreateAcc);

        creatAccTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Sign_up.class);
                startActivity(intent);
            }
        });
    }
}