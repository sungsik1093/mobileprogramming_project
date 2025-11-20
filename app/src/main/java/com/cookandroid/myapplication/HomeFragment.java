package com.cookandroid.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    TextView tvDate;
    ImageView moodGood, moodSoso, moodBad;
    String selectedMood = "보통";   // 기본값

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);

        // 날짜
        tvDate = v.findViewById(R.id.tv_today_date);

        // 기분 선택 ImageView
        moodGood = v.findViewById(R.id.mood_good);
        moodSoso = v.findViewById(R.id.mood_soso);
        moodBad = v.findViewById(R.id.mood_bad);

        // 운동하러 가기 버튼
        Button btnGoWorkout = v.findViewById(R.id.btn_go_workout);

        // 오늘 날짜 자동 표시
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 (E)", Locale.KOREA);
        tvDate.setText(sdf.format(new Date()));

        // 기분 클릭 이벤트
        View.OnClickListener moodClickListener = view -> {
            resetMoodBackground();

            if (view.getId() == R.id.mood_good) {
                selectedMood = "좋음";
                moodGood.setBackgroundResource(R.drawable.bg_mood_selected);

            } else if (view.getId() == R.id.mood_soso) {
                selectedMood = "보통";
                moodSoso.setBackgroundResource(R.drawable.bg_mood_selected);

            } else if (view.getId() == R.id.mood_bad) {
                selectedMood = "별로";
                moodBad.setBackgroundResource(R.drawable.bg_mood_selected);
            }

            // TODO: SharedPreferences로 저장하면 추천 화면에서 기분 반영 가능
        };

        // 적용
        moodGood.setOnClickListener(moodClickListener);
        moodSoso.setOnClickListener(moodClickListener);
        moodBad.setOnClickListener(moodClickListener);

        // 추천 화면(운동 추천)으로 이동
        btnGoWorkout.setOnClickListener(view -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity main = (MainActivity) getActivity();

                main.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new RecommendFragment())
                        .commit();
            }
        });

        return v;
    }

    // 선택되지 않은 기분 카드 배경 초기화
    private void resetMoodBackground() {
        moodGood.setBackgroundResource(R.drawable.bg_mood_unselected);
        moodSoso.setBackgroundResource(R.drawable.bg_mood_unselected);
        moodBad.setBackgroundResource(R.drawable.bg_mood_unselected);
    }
}
