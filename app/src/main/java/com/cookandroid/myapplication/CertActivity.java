package com.cookandroid.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class CertActivity extends AppCompatActivity {

    ImageView ivPreview;
    TextView tvOverlayDate, tvOverlayInfo, tvOverlayLabel, tvResult;
    EditText etMemo;
    Button btnTakePhoto, btnSave, btnShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_cert);

        ivPreview = findViewById(R.id.iv_photo_preview);
        tvOverlayDate = findViewById(R.id.tv_overlay_date);
        tvOverlayInfo = findViewById(R.id.tv_overlay_info);
        tvOverlayLabel = findViewById(R.id.tv_overlay_label);
        tvResult = findViewById(R.id.tv_save_result);
        etMemo = findViewById(R.id.et_today_memo);

        btnTakePhoto = findViewById(R.id.btn_take_photo);
        btnSave = findViewById(R.id.btn_save_record);
        btnShare = findViewById(R.id.btn_share_mate);

        // 운동 정보 받기(기분 포함)
        final String name = getIntent().getStringExtra("exercise_name");
        final String desc = getIntent().getStringExtra("exercise_desc");
        final String level = getIntent().getStringExtra("exercise_level");
        final int icon = getIntent().getIntExtra("exercise_icon", R.drawable.ic_plank);
        final String mood = getIntent().getStringExtra("exercise_mood");
        final String date = getIntent().getStringExtra("exercise_date");

        String moodEmoji = convertMoodToEmoji(mood);
        String levelStar = (level != null) ? level : "☆☆☆";
        String infoText = name + " · 난이도 " + levelStar + " · 기분 " + moodEmoji;

        ivPreview.setImageResource(icon); // 운동 이미지
        tvOverlayLabel.setText("오운완!");
        tvOverlayDate.setText(date != null ? date : "2025-11-21");
        tvOverlayInfo.setText(infoText); // 기분 이모지까지 표시

        btnTakePhoto.setOnClickListener(v -> ivPreview.setImageResource(icon));
        btnSave.setOnClickListener(v -> tvResult.setText("오늘의 기록이 저장되었습니다!"));
        btnShare.setOnClickListener(v -> tvResult.setText("운동 메이트에게 인증을 보냈습니다! ✨"));
    }

    private String convertMoodToEmoji(String mood) {
        if (mood == null) return "😐"; // 선택되지 않은 경우 보통
        switch (mood) {
            case "좋음": return "😊";
            case "보통": return "😐";
            case "별로": return "😡";
        }
        return "😐";
    }

}
