package com.cookandroid.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    TextView tvDate, tvTodayTitle;
    ImageView moodGood, moodSoso, moodBad;
    LinearLayout todayExerciseLayout;

    private String selectedMood = null; // 초기값: 선택하지 않음

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);

        tvDate = v.findViewById(R.id.tv_today_date);
        tvTodayTitle = v.findViewById(R.id.tv_today_exercise_title); // 제목 TextView
        moodGood = v.findViewById(R.id.mood_good);
        moodSoso = v.findViewById(R.id.mood_soso);
        moodBad = v.findViewById(R.id.mood_bad);
        todayExerciseLayout = v.findViewById(R.id.layout_today_exercise);
        Button btnGoWorkout = v.findViewById(R.id.btn_go_workout);

        // 오늘 날짜 표시
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 (E)", Locale.KOREA);
        tvDate.setText(sdf.format(new Date()));

        // 초기에는 제목 숨김
        tvTodayTitle.setVisibility(View.GONE);

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
            if (selectedMood == null) {
                // 기분을 선택하지 않은 경우 → 이동 막기 + 안내 표시
                Toast.makeText(getContext(), "기분을 먼저 선택해주세요 😊", Toast.LENGTH_SHORT).show();
                return;
            }

            // 기분 선택한 경우 → RecommendFragment 이동
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

    /** 외부에서 mood 조회 가능하도록 public 메서드 제공 */
    public String getSelectedMood() {
        return selectedMood;
    }

    private void showTodayExercise() {
        // 기존 운동 목록 제거 (첫 번째 자식: 제목 제외)
        int childCount = todayExerciseLayout.getChildCount();
        for (int i = childCount - 1; i >= 1; i--) {
            todayExerciseLayout.removeViewAt(i);
        }

        if (selectedMood == null) {
            // 기분 선택 전: 제목 숨기고 안내 문구만 표시
            tvTodayTitle.setVisibility(View.GONE);

            TextView tv = new TextView(getContext());
            tv.setText("오늘 기분을 선택하면 추천 운동이 나타나요 💪");
            tv.setTextSize(16);
            tv.setTextColor(0xFF6F6F6F);
            tv.setPadding(0, 28, 0, 28);
            todayExerciseLayout.addView(tv);
        } else {
            // 기분 선택 후: 제목 표시 + 운동 목록
            tvTodayTitle.setVisibility(View.VISIBLE);

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
                    list.add("스쿼트 3세트");
                    list.add("마운틴클라이머 1분 × 3회");
                    list.add("조깅 20분");
                } else if (timeZone.equals("afternoon")) {
                    list.add("버피테스트 20회 × 3세트");
                    list.add("인터버러닝 10분");
                    list.add("점핑잭 50회 × 2세트");
                    list.add("스프린트 5세트");
                } else {
                    list.add("브이업 15회 × 3세트");
                    list.add("사이드플랭크 40초 × 2회");
                    list.add("슬로우 조깅 15분");
                }
                break;

            case "보통":
                if (timeZone.equals("morning")) {
                    list.add("플랭크 1분 × 2회");
                    list.add("힙브릿지 20회");
                    list.add("가벼운 걷기 10분");
                } else if (timeZone.equals("afternoon")) {
                    list.add("런지 15회 × 3세트");
                    list.add("푸시업 10~15회 × 3세트");
                    list.add("자전거 타기 20분");
                    list.add("계단오르기 10분");
                } else {
                    list.add("레그레이즈 15회 × 2세트");
                    list.add("걷기 런지 10분");
                    list.add("야외 스트레칭 10분");
                }
                break;

            case "별로":
                if (timeZone.equals("morning")) {
                    list.add("스트레칭 10분");
                    list.add("데드버그 15회");
                    list.add("느린 걷기 10~15분");
                } else if (timeZone.equals("afternoon")) {
                    list.add("파워워킹 15분");
                    list.add("언덕 걷기 10분");
                    list.add("보조풀업(쉬운 버전) 5회");
                } else {
                    list.add("천천히 걷기 20분");
                    list.add("야외 스트레칭 10분");
                    list.add("버드독 10회 × 2세트");
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