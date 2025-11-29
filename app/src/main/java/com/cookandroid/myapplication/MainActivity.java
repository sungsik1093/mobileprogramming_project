package com.cookandroid.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("오운완 트래커");

        bottomNav = findViewById(R.id.bottom_nav);

        // 앱 실행 시 기본 홈 화면
        replaceFragment(new HomeFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;

            int id = item.getItemId();
            if (id == R.id.nav_home) {
                selected = new HomeFragment();
            } else if (id == R.id.nav_recommend) {
                selected = new RecommendFragment();
            } else if (id == R.id.nav_record) {
                selected = new CalendarFragment();
            } else if (id == R.id.nav_map) {
                selected = new MapFragment();
            }

            if (selected != null) {
                replaceFragment(selected);
                return true;
            }
            return false;
        });
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
    /*수정*/
    @Override
    protected void onResume() {
        super.onResume();

        if (getIntent() != null && getIntent().getBooleanExtra("open_calendar", false)) {

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new CalendarFragment())
                    .commit();
            getIntent().removeExtra("open_calendar");
        }
    }

}