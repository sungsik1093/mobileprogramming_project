package com.cookandroid.myapplication;

import android.content.Intent;
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

    String selectedMood = null; // 초기값: 선택하지 않음

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

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 (E)", Locale.KOREA);
        tvDate.setText(sdf.format(new Date()));

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
            showTodayExercise();
        };

        moodGood.setOnClickListener(moodClickListener);
        moodSoso.setOnClickListener(moodClickListener);
        moodBad.setOnClickListener(moodClickListener);

        btnGoWorkout.setOnClickListener(view -> {
            // 운동하러 가기 클릭 시 RecommendFragment로 이동, 기분값 전달
            RecommendFragment fragment = new RecommendFragment();
            Bundle bundle = new Bundle();
            bundle.putString("selectedMood", selectedMood);
            fragment.setArguments(bundle);
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        });

        showTodayExercise();
        return v;
    }

    private void resetMoodBackground() {
        moodGood.setBackgroundResource(R.drawable.bg_mood_unselected);
        moodSoso.setBackgroundResource(R.drawable.bg_mood_unselected);
        moodBad.setBackgroundResource(R.drawable.bg_mood_unselected);
    }

    public String getSelectedMood() {
        return selectedMood;
    }

    private void showTodayExercise() {
        todayExerciseLayout.removeAllViews();
        if (selectedMood == null) {
            TextView tv = new TextView(getContext());
            tv.setText("오늘 기분을 선택하면 추천 운동이 나타나요 💪");
            tv.setTextSize(16);
            tv.setTextColor(0xFF6F6F6F);
            tv.setPadding(0, 28, 0, 28);
            todayExerciseLayout.addView(tv);
        } else {
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
    }

    private ArrayList<String> getExercisesByMoodAndTime(String mood) {
        ArrayList<String> list = new ArrayList<>();
        String timeZone = getTimeZone();

        switch (mood) {
            case "좋음":
                if (timeZone.equals("morning")) {
                    list.add("가벼운 조깅 20분");
                    list.add("요가 스트레칭");
                } else if (timeZone.equals("afternoon")) {
                    list.add("인터벌 달리기");
                    list.add("버피 테스트 3세트");
                } else {
                    list.add("고강도 타바타 10분");
                }
                break;
            case "보통":
                if (timeZone.equals("morning")) {
                    list.add("산책 20분");
                    list.add("플랭크 1분 × 3회");
                } else if (timeZone.equals("afternoon")) {
                    list.add("스쿼트 3세트");
                } else {
                    list.add("페이스 조절 조깅");
                }
                break;
            case "별로":
                if (timeZone.equals("morning")) {
                    list.add("가벼운 스트레칭");
                    list.add("명상 10분");
                } else if (timeZone.equals("afternoon")) {
                    list.add("천천히 걷기 20분");
                } else {
                    list.add("피로 회복 스트레칭");
                }
                break;
        }
        return list;
    }

    private String getTimeZone() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) return "morning";
        else if (hour >= 12 && hour < 18) return "afternoon";
        else return "evening";
    }
}
