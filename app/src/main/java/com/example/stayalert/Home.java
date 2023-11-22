package com.example.stayalert;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.example.stayalert.databinding.ActivityHomeBinding;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class Home extends AppCompatActivity {

    ActivityHomeBinding binding;
    MeowBottomNavigation bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNavigation.add(new MeowBottomNavigation.Model(1, R.drawable.ic_menu));
        binding.bottomNavigation.add(new MeowBottomNavigation.Model(2, R.drawable.ic_call));
        binding.bottomNavigation.add(new MeowBottomNavigation.Model(3, R.drawable.ic_home));
        binding.bottomNavigation.add(new MeowBottomNavigation.Model(4, R.drawable.ic_stats));
        binding.bottomNavigation.add(new MeowBottomNavigation.Model(5, R.drawable.ic_profile));

        binding.bottomNavigation.show(3,true);
        binding.bottomNavigation.clearCount(1);

        replaceFragment(new MenuFrag());
        binding.bottomNavigation.setOnClickMenuListener(new Function1<MeowBottomNavigation.Model, Unit>() {
            @Override
            public Unit invoke(MeowBottomNavigation.Model model) {
                switch (model.getId()){
                    case 1:
                        replaceFragment(new MenuFrag());
                        Toast.makeText(Home.this, "Home", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        replaceFragment(new PhoneFrag());
                        Toast.makeText(Home.this, "Phone", Toast.LENGTH_SHORT).show();

                        break;
                    case 3:
                        replaceFragment(new HomeFrag());
                        Toast.makeText(Home.this, "Home", Toast.LENGTH_SHORT).show();

                        break;
                    case 4:
                        replaceFragment(new StatsFrag());
                        Toast.makeText(Home.this, "Stats", Toast.LENGTH_SHORT).show();

                        break;
                    case 5:
                        replaceFragment(new ProfileFrag());
                        Toast.makeText(Home.this, "Profile", Toast.LENGTH_SHORT).show();

                        break;
                }
                return null;
            }
        });

    }
    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout,fragment);
        transaction.commit();
    }
}