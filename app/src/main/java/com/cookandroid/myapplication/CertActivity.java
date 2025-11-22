package com.cookandroid.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CertActivity extends AppCompatActivity {

    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    private File photoFile;
    private String currentPhotoPath;

    private ImageView ivPreview;
    private TextView tvOverlayDate, tvOverlayInfo, tvOverlayLabel, tvResult;
    private EditText etMemo;
    private Button btnTakePhoto, btnSave, btnShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_cert); // fragment 레이아웃 그대로 사용

        // XML 연결
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

        // 날짜, 난이도, 기분 표시
        String moodEmoji = convertMoodToEmoji(mood);
        String levelStar = (level != null) ? level : "☆☆☆";
        String infoText = (name != null ? name : "운동") + " · 난이도 " + levelStar + " · 기분 " + moodEmoji;

        ivPreview.setImageResource(icon); // 운동 이미지
        tvOverlayLabel.setText("오운완!");
        tvOverlayDate.setText(date != null ? date : new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        tvOverlayInfo.setText(infoText);

        // 카메라 권한 및 결과 처리 세팅
        setupPermissionLauncher();
        setupCameraLauncher();

        // 버튼 클릭 이벤트
        btnTakePhoto.setOnClickListener(v -> permissionLauncher.launch(android.Manifest.permission.CAMERA));

        btnSave.setOnClickListener(v -> {
            String memo = etMemo.getText().toString();
            tvResult.setText("오늘의 기록이 저장되었습니다! \n메모: " + memo);
        });

        btnShare.setOnClickListener(v -> tvResult.setText("운동 메이트에게 인증을 보냈습니다! ✨"));
    }

    // 카메라 권한 요청
    private void setupPermissionLauncher() {
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    // 카메라 실행
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            try {
                photoFile = createImageFile();

                Uri photoUri = FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".provider",
                        photoFile
                );

                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                cameraLauncher.launch(intent);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "사진 파일 생성 실패", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "카메라 앱이 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 카메라 결과 받기
    private void setupCameraLauncher() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                        if (bitmap != null) {
                            ivPreview.setImageBitmap(bitmap);
                        } else {
                            Toast.makeText(this, "사진 로드 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    // 이미지 파일 생성
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "IMG_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(fileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // 기분을 이모지로 변환
    private String convertMoodToEmoji(String mood) {
        if (mood == null) return "😐";
        switch (mood) {
            case "좋음": return "😊";
            case "보통": return "😐";
            case "별로": return "😡";
        }
        return "😐";
    }
}