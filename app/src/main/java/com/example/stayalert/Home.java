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

//        replaceFragment(new HomeFrag());
//        binding.bottomNavigationView.setBackground(null);
//        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
//            switch (item.getItemId()) {
//                case R.id.home:
//                    replaceFragment(new HomeFrag());
//                    break;
//                case R.id.phone:
//                    replaceFragment(new PhoneFrag());
//                    break;
//                case R.id.stats:
//                    replaceFragment(new StatsFrag());
//                    break;
//                case R.id.profile:
//                    replaceFragment(new ProfileFrag());
//                    break;
//            }
//            return true;
//        });
//
//        binding.bottomNavigationView.setItemIconTintList(null);
//
//        binding.menuFab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                replaceFragment(new MenuFrag());
//            }
//        });

    }
//    private void replaceFragment(Fragment fragment) {
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.replace(R.id.frame_layout, fragment);
//        fragmentTransaction.commit();
//    }
}