package com.cookandroid.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ExerciseDetailActivity extends AppCompatActivity {

    TextView tvName, tvDesc, tvTitle;
    ImageView ivImage;
    LinearLayout layoutStars;
    Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_detail);

        tvName = findViewById(R.id.tv_ex_name);
        tvDesc = findViewById(R.id.tv_ex_desc);
        tvTitle = findViewById(R.id.tv_title);
        ivImage = findViewById(R.id.iv_exercise_image);
        layoutStars = findViewById(R.id.layout_stars);
        btnStart = findViewById(R.id.btn_start_exercise);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // 전달받은 값
        final String name = getIntent().getStringExtra("exercise_name");
        final String desc = getIntent().getStringExtra("exercise_desc");
        final String level = getIntent().getStringExtra("exercise_level");
        final int icon = getIntent().getIntExtra("exercise_icon", R.drawable.ic_plank);
        final String mood = getIntent().getStringExtra("exercise_mood"); // 기분값 받기
        final String date = getIntent().getStringExtra("exercise_date");

        // 화면 세팅
        tvName.setText(name != null ? name : "운동명 없음");
        tvTitle.setText(name != null ? name : "운동명 없음");
        tvDesc.setText(desc != null ? desc : "설명이 없습니다.");
        ivImage.setImageResource(icon);

        setStars(level != null ? level : "☆☆☆☆☆");

        // 운동 완료 버튼 클릭 시 CertActivity로 이동, 기분값 반드시 넘기기!
        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(ExerciseDetailActivity.this, CertActivity.class);
            intent.putExtra("exercise_name", name);
            intent.putExtra("exercise_desc", desc);
            intent.putExtra("exercise_level", level);
            intent.putExtra("exercise_icon", icon);
            intent.putExtra("exercise_mood", mood); // 기분값
            intent.putExtra("exercise_date", date);
            startActivity(intent);
        });
    }

    private void setStars(String level) {
        layoutStars.removeAllViews();
        int filledCount = 0;
        for (char c : level.toCharArray()) {
            if (c == '★') filledCount++;
        }
        for (int i = 0; i < 5; i++) {
            ImageView star = new ImageView(this);
            star.setLayoutParams(new LinearLayout.LayoutParams(50, 50));
            star.setImageResource(i < filledCount ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);
            layoutStars.addView(star);
        }
    }
}
