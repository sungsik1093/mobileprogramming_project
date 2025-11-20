package com.cookandroid.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ExerciseDetailActivity extends AppCompatActivity {

    TextView tvName, tvDesc, tvTitle;
    ImageView ivImage;
    LinearLayout layoutStars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_detail);

        tvName = findViewById(R.id.tv_ex_name);
        tvDesc = findViewById(R.id.tv_ex_desc);
        tvTitle = findViewById(R.id.tv_title);
        ivImage = findViewById(R.id.iv_exercise_image);
        layoutStars = findViewById(R.id.layout_stars);

        // 전달받은 값
        String name = getIntent().getStringExtra("name");
        String desc = getIntent().getStringExtra("desc");
        String level = getIntent().getStringExtra("level");   // ex: "★★☆"
        int icon = getIntent().getIntExtra("icon", 0);

        // 세팅
        tvName.setText(name);
        tvTitle.setText(name);
        ivImage.setImageResource(icon);
        tvDesc.setText(desc);

        // 별 생성
        setStars(level);
    }

    private void setStars(String level) {
        layoutStars.removeAllViews(); // 초기화

        int filledCount = 0;

        // ★★☆ 형태 해석
        for (char c : level.toCharArray()) {
            if (c == '★') filledCount++;
        }

        for (int i = 0; i < 5; i++) {
            ImageView star = new ImageView(this);
            star.setLayoutParams(new LinearLayout.LayoutParams(50, 50));

            if (i < filledCount) {
                star.setImageResource(R.drawable.ic_star_filled);
            } else {
                star.setImageResource(R.drawable.ic_star_empty);
            }

            layoutStars.addView(star);
        }
    }
}
