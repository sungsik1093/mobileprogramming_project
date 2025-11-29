package com.cookandroid.myapplication;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RecordDetailActivity extends AppCompatActivity {

    private TextView tvDate, tvInfo, tvMemo;
    private ImageView imgPhoto;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_detail);

        tvDate = findViewById(R.id.tv_record_date);
        tvInfo = findViewById(R.id.tv_record_info);
        tvMemo = findViewById(R.id.tv_record_memo);
        imgPhoto = findViewById(R.id.img_record_photo);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        dbHelper = new DBHelper(this);

        long recordId = getIntent().getLongExtra("record_id", -1);
        if (recordId != -1) {
            loadRecordDetails(recordId);
        } else {
            Toast.makeText(this, "기록 ID를 찾을 수 없습니다.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loadRecordDetails(long id) {
        Record record = dbHelper.getRecordById(id);

        if (record != null) {

            String emoji = getMoodEmoji(record.getMood());
            tvDate.setText(record.getDate());
            tvInfo.setText(record.getName() + " · 난이도 " + record.getLevel() + " · 기분 " + emoji);
            tvMemo.setText(record.getMemo());

            String photoPath = record.getPhotoPath();
            if (photoPath != null && !photoPath.isEmpty()) {
                // 저장된 파일 경로를 URI로 변환하여 ImageView에 설정
                Uri imageUri = Uri.parse(photoPath);
                imgPhoto.setImageURI(imageUri);
            } else {
                imgPhoto.setImageDrawable(null);
            }
        } else {
            Toast.makeText(this, "상세 기록을 불러올 수 없습니다.", Toast.LENGTH_LONG).show();
        }
    }

    private String getMoodEmoji(String mood) {
        if (mood == null) return "😐";
        switch (mood) {
            case "좋음": return "😊";
            case "보통": return "😐";
            case "별로": return "😡";
        }
        return "😐";
    }
}
