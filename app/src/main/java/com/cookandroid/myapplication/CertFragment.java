package com.cookandroid.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CertFragment extends Fragment {

    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    private File photoFile;
    private String currentPhotoPath;
    private ImageView ivPreview;

    TextView tvOverlayDate, tvOverlayInfo, tvOverlayLabel, tvResult;
    EditText etMemo;

    // 카메라 권한 요청
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();   // 권한 승인되면 카메라 실행
                    } else {
                        Toast.makeText(requireContext(), "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_cert, container, false);

        ivPreview = v.findViewById(R.id.iv_photo_preview);
        tvOverlayDate = v.findViewById(R.id.tv_overlay_date);
        tvOverlayInfo = v.findViewById(R.id.tv_overlay_info);
        tvOverlayLabel = v.findViewById(R.id.tv_overlay_label);
        tvResult = v.findViewById(R.id.tv_save_result);
        etMemo = v.findViewById(R.id.et_today_memo);

        Button btnTakePhoto = v.findViewById(R.id.btn_take_photo);
        Button btnSave = v.findViewById(R.id.btn_save_record);
        Button btnShare = v.findViewById(R.id.btn_share_mate);

        // 오늘 날짜 표시
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        tvOverlayDate.setText(today);

        // 카메라 결과 받기 세팅
        setupCameraLauncher();

        // 카메라 권한 요청
        btnTakePhoto.setOnClickListener(view ->
                permissionLauncher.launch(android.Manifest.permission.CAMERA)
        );

        btnSave.setOnClickListener(view -> {
            String memo = etMemo.getText().toString();
            tvResult.setText("오늘의 기록이 저장되었습니다! \n메모: " + memo);
        });

        btnShare.setOnClickListener(view -> {
            tvResult.setText("운동 메이트에게 인증을 보냈습니다! ✨");
        });

        return v;
    }

    // 카메라 실행
    private void openCamera() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // 카메라 앱 확인
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {

            try {
                // 저장할 파일 생성
                photoFile = createImageFile();

                // fileprovider로 URI 변환
                String authority = requireContext().getPackageName() + ".provider";
                Uri photoUri = FileProvider.getUriForFile(
                        requireContext(),
                        authority,
                        photoFile
                );

                // 카메라 앱에 저장 위치 전달 (중요!)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

                // 카메라 실행
                cameraLauncher.launch(intent);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(requireContext(), "카메라 앱이 없습니다.", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(requireContext(), "사진 로드 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    // 사진 저장할 실제 이미지 파일 생성
    private File createImageFile() throws IOException {
        // 타임스탬프 기반 파일명
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "IMG_" + timeStamp;

        // 앱 전용 사진 저장 폴더
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // 실제 파일 생성
        File image = File.createTempFile(
                fileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
