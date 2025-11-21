package com.cookandroid.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
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
            } else if (id == R.id.nav_cert) {

                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                String mood = "보통"; // 기본값

                if (currentFragment instanceof HomeFragment) {
                    mood = ((HomeFragment) currentFragment).getSelectedMood();
                }

                Intent intent = new Intent(MainActivity.this, CertActivity.class);
                intent.putExtra("exercise_mood", mood);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_record) {
                selected = new CalendarFragment();
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
}
