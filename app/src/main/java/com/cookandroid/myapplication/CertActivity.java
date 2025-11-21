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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_cert); // xml 그대로 사용

        ivPreview = findViewById(R.id.iv_photo_preview);
        tvOverlayDate = findViewById(R.id.tv_overlay_date);
        tvOverlayInfo = findViewById(R.id.tv_overlay_info);
        tvOverlayLabel = findViewById(R.id.tv_overlay_label);
        tvResult = findViewById(R.id.tv_save_result);
        etMemo = findViewById(R.id.et_today_memo);

        Button btnTakePhoto = findViewById(R.id.btn_take_photo);
        Button btnSave = findViewById(R.id.btn_save_record);
        Button btnShare = findViewById(R.id.btn_share_mate);

        // Intent로 전달받은 값
        String name = getIntent().getStringExtra("exercise_name");
        String level = getIntent().getStringExtra("exercise_level");
        int icon = getIntent().getIntExtra("exercise_icon", R.drawable.ic_plank);

        // 화면 표시
        tvOverlayInfo.setText(name + " · 난이도 " + level + " · 기분 😊");
        tvOverlayLabel.setText("오운완!");
        ivPreview.setImageResource(icon);

        // 버튼 기능
        btnTakePhoto.setOnClickListener(v -> ivPreview.setImageResource(icon));

        btnSave.setOnClickListener(v -> {
            String memo = etMemo.getText().toString();
            tvResult.setText("오늘의 기록이 저장되었습니다! \n메모: " + memo);
        });

        btnShare.setOnClickListener(v -> tvResult.setText("운동 메이트에게 인증을 보냈습니다! ✨"));
    }
}