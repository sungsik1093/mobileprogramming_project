package com.cookandroid.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class RecommendFragment extends Fragment {

    GridLayout gridIndoor, gridOutdoor;

    public RecommendFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_recommend, container, false);

        gridIndoor = v.findViewById(R.id.grid_indoor);
        gridOutdoor = v.findViewById(R.id.grid_outdoor);

        // 운동 목록 채우기
        ArrayList<Exercise> indoor = getIndoorExercises();
        ArrayList<Exercise> outdoor = getOutdoorExercises();

        addExerciseCards(gridIndoor, indoor);
        addExerciseCards(gridOutdoor, outdoor);

        return v;
    }

    private void addExerciseCards(GridLayout grid, ArrayList<Exercise> exercises) {
        for (Exercise ex : exercises) {

            LinearLayout card = new LinearLayout(getContext());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(20, 20, 20, 20);
            card.setGravity(android.view.Gravity.CENTER);
            card.setBackgroundResource(R.drawable.bg_exercise_card);

            GridLayout.LayoutParams params =
                    new GridLayout.LayoutParams();
            params.width = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(16, 16, 16, 16);
            card.setLayoutParams(params);

            ImageView iv = new ImageView(getContext());
            iv.setImageResource(ex.iconRes);
            iv.setLayoutParams(new LinearLayout.LayoutParams(100, 100));

            TextView tv = new TextView(getContext());
            tv.setText(ex.name);
            tv.setTextColor(0xFF111827);
            tv.setTextSize(16);
            tv.setPadding(0, 10, 0, 0);

            card.addView(iv);
            card.addView(tv);

            card.setOnClickListener(v -> openDetail(ex));

            grid.addView(card);
        }
    }

    private void openDetail(Exercise e) {
        Intent intent = new Intent(getContext(), ExerciseDetailActivity.class);
        intent.putExtra("name", e.name);
        intent.putExtra("desc", e.description);
        intent.putExtra("level", e.level);
        intent.putExtra("icon", e.iconRes);
        startActivity(intent);
    }

    // 운동 목록 데이터

    private ArrayList<Exercise> getIndoorExercises() {
        ArrayList<Exercise> list = new ArrayList<>();

        list.add(new Exercise("스쿼트", "하체 강화, 체지방 감소", "★★☆", R.drawable.ic_squat));
        list.add(new Exercise("플랭크", "코어 근육 강화", "★★☆", R.drawable.ic_plank));
        list.add(new Exercise("버피 테스트", "전신 운동, 높은 칼로리 소모", "★★★☆", R.drawable.ic_burpee));
        list.add(new Exercise("런지", "하체 집중, 균형감 개선", "★★☆", R.drawable.ic_lunge));
        list.add(new Exercise("점핑잭", "유산소 + 심박수 상승", "★★☆", R.drawable.ic_jumpingjack));
        list.add(new Exercise("크런치", "복근 운동", "★☆☆", R.drawable.ic_crunch));
        list.add(new Exercise("힙브릿지", "둔근 강화, 허리 지지", "★☆☆", R.drawable.ic_hipbridge));

        return list;
    }

    private ArrayList<Exercise> getOutdoorExercises() {
        ArrayList<Exercise> list = new ArrayList<>();

        list.add(new Exercise("조깅", "전신 유산소, 체력 향상", "★★☆", R.drawable.ic_jogging));
        list.add(new Exercise("파워워킹", "누구나 가능한 유산소 운동", "★☆☆", R.drawable.ic_powerwalk));
        list.add(new Exercise("자전거", "하체 강화, 지속적 유산소", "★★☆", R.drawable.ic_cycling));
        list.add(new Exercise("등산", "근지구력 향상", "★★★☆", R.drawable.ic_hiking));
        list.add(new Exercise("줄넘기", "고효율 유산소, 칼로리 소모", "★★★☆", R.drawable.ic_jumprope));
        list.add(new Exercise("야외 스트레칭", "가볍게 시작하기 좋음", "★☆☆", R.drawable.ic_stretching));
        list.add(new Exercise("공원 스포츠", "재미 + 유산소 + 협응", "★★☆", R.drawable.ic_sports));

        return list;
    }

    // 운동 데이터 클래스

    class Exercise {
        String name;
        String description;
        String level;
        int iconRes;

        Exercise(String name, String desc, String level, int icon) {
            this.name = name;
            this.description = desc;
            this.level = level;
            this.iconRes = icon;
        }
    }
}
