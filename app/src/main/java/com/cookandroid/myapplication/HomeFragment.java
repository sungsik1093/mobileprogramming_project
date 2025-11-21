package com.cookandroid.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    TextView tvDate;
    ImageView moodGood, moodSoso, moodBad;
    LinearLayout todayExerciseLayout;
    String selectedMood = "보통";  // 기본값

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);

        tvDate = v.findViewById(R.id.tv_today_date);
        moodGood = v.findViewById(R.id.mood_good);
        moodSoso = v.findViewById(R.id.mood_soso);
        moodBad = v.findViewById(R.id.mood_bad);
        todayExerciseLayout = v.findViewById(R.id.layout_today_exercise);
        Button btnGoWorkout = v.findViewById(R.id.btn_go_workout);

        // 오늘 날짜 표시
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 (E)", Locale.KOREA);
        tvDate.setText(sdf.format(new Date()));

        // 기분 버튼 클릭 이벤트
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
            showTodayExercise(); // 기분 변경 시 운동 추천 갱신
        };

        moodGood.setOnClickListener(moodClickListener);
        moodSoso.setOnClickListener(moodClickListener);
        moodBad.setOnClickListener(moodClickListener);

        btnGoWorkout.setOnClickListener(view -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new RecommendFragment())
                        .commit();
            }
        });

        showTodayExercise(); // 초기 진입 시 운동 추천

        return v;
    }

    private void resetMoodBackground() {
        moodGood.setBackgroundResource(R.drawable.bg_mood_unselected);
        moodSoso.setBackgroundResource(R.drawable.bg_mood_unselected);
        moodBad.setBackgroundResource(R.drawable.bg_mood_unselected);
    }

    private void showTodayExercise() {
        todayExerciseLayout.removeAllViews();
        ArrayList<String> exercises = getExercisesByMoodAndTime(selectedMood);

        for (String ex : exercises) {
            TextView tv = new TextView(getContext());
            tv.setText("• " + ex);
            tv.setTextSize(16);
            tv.setPadding(0, 8, 0, 8);
            tv.setTextColor(0xFF6F6F6F);
            todayExerciseLayout.addView(tv);
        }
    }

    // 🔥 기분 + 시간대에 따른 운동 추천
    private ArrayList<String> getExercisesByMoodAndTime(String mood) {
        ArrayList<String> list = new ArrayList<>();

        String timeZone = getTimeZone(); // morning / afternoon / evening

        switch (mood) {

            case "좋음":
                if (timeZone.equals("morning")) {
                    list.add("가벼운 조깅 20분");
                    list.add("요가 스트레칭");
                    list.add("하체 강화 루틴");
                } else if (timeZone.equals("afternoon")) {
                    list.add("인터벌 달리기");
                    list.add("버피 테스트 3세트");
                    list.add("케틀벨 스윙");
                } else {
                    list.add("고강도 타바타 10분");
                    list.add("가벼운 맨몸 근력운동");
                }
                break;

            case "보통":
                if (timeZone.equals("morning")) {
                    list.add("산책 20분");
                    list.add("플랭크 1분 × 3회");
                    list.add("가볍게 스트레칭");
                } else if (timeZone.equals("afternoon")) {
                    list.add("스쿼트 3세트");
                    list.add("런지 3세트");
                    list.add("상체 근력 루틴");
                } else {
                    list.add("페이스 조절 조깅");
                    list.add("가벼운 사이클 15분");
                }
                break;

            case "별로":
                if (timeZone.equals("morning")) {
                    list.add("가벼운 스트레칭");
                    list.add("명상 10분");
                } else if (timeZone.equals("afternoon")) {
                    list.add("천천히 걷기 20분");
                    list.add("요가 동작 따라하기");
                } else {
                    list.add("피로 회복 스트레칭");
                    list.add("초저강도 홈트 10분");
                }
                break;
        }

        return list;
    }

    // 🔥 시간대 판별 (morning / afternoon / evening)
    private String getTimeZone() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) return "morning";
        else if (hour >= 12 && hour < 18) return "afternoon";
        else return "evening";
    }
}