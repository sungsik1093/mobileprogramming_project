package com.cookandroid.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CertFragment extends Fragment {

    ImageView ivPreview;
    TextView tvOverlayDate, tvOverlayInfo, tvOverlayLabel, tvResult;
    EditText etMemo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_cert, container, false);

        // XML 연결
        Button btnTakePhoto = v.findViewById(R.id.btn_take_photo);
        Button btnSave = v.findViewById(R.id.btn_save_record);
        Button btnShare = v.findViewById(R.id.btn_share_mate);

        ivPreview = v.findViewById(R.id.iv_photo_preview);
        tvOverlayDate = v.findViewById(R.id.tv_overlay_date);
        tvOverlayInfo = v.findViewById(R.id.tv_overlay_info);
        tvOverlayLabel = v.findViewById(R.id.tv_overlay_label);
        tvResult = v.findViewById(R.id.tv_save_result);
        etMemo = v.findViewById(R.id.et_today_memo);

        // 샘플 이미지 (파일 없을 경우 기본 이미지로 대체)
        btnTakePhoto.setOnClickListener(view -> {
            ivPreview.setImageResource(R.drawable.ic_plank);
        });

        // 저장 버튼
        btnSave.setOnClickListener(view -> {
            String memo = etMemo.getText().toString();
            tvResult.setText("오늘의 기록이 저장되었습니다! \n메모: " + memo);
        });

        // 공유 버튼
        btnShare.setOnClickListener(view -> {
            tvResult.setText("운동 메이트에게 인증을 보냈습니다! ✨");
        });

        return v;
    }
}
