package com.cookandroid.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;

import java.util.ArrayList;

public class RecommendFragment extends Fragment {

    GridLayout gridIndoor, gridOutdoor;
    TextView tvWeatherInfo, tvWeatherRecommend;
    Button btnRandom;
    RequestQueue queue;
    LinearLayout layoutRecommend;
    private FusedLocationProviderClient fusedLocationClient;
    private final String API_KEY = "b173bcdd6617f3a5dc76e5136f9ba1c0";
    private double userLat = 37.5501;
    private double userLon = 126.9237;

    private String selectedMood; // 기분 정보

    public RecommendFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_recommend, container, false);

        gridIndoor = v.findViewById(R.id.grid_indoor);
        gridOutdoor = v.findViewById(R.id.grid_outdoor);
        tvWeatherInfo = v.findViewById(R.id.tv_weather_info);
        tvWeatherRecommend = v.findViewById(R.id.tv_weather_recommend);
        btnRandom = v.findViewById(R.id.btn_random_exercise);
        layoutRecommend = v.findViewById(R.id.layout_recommend);

        // 🔥 초기 상태: 텍스트만 로딩중 표시
        tvWeatherInfo.setText("날씨 정보를 불러오는 중...");
        tvWeatherRecommend.setText("잠시만 기다려 주세요!");

        queue = Volley.newRequestQueue(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        if (getArguments() != null) {
            selectedMood = getArguments().getString("selectedMood");
        }

        requestLocation();

        btnRandom.setOnClickListener(view -> recommendRandom());

        return v;
    }


    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1000
            );
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                userLat = location.getLatitude();
                userLon = location.getLongitude();
            }
            fetchWeather();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation();
            } else {
                Toast.makeText(requireContext(),
                        "위치 권한이 필요합니다. 기본 위치를 사용합니다.",
                        Toast.LENGTH_SHORT).show();
                fetchWeather();
            }
        }
    }

    private void fetchWeather() {
        String url = String.format(
                "https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&units=metric&appid=%s",
                userLat, userLon, API_KEY
        );

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String weather = response.getJSONArray("weather")
                                .getJSONObject(0)
                                .getString("main");

                        double temp = response.getJSONObject("main").getDouble("temp");

                        tvWeatherInfo.setText(String.format("현재 %.1f℃ · %s",
                                temp, translateWeather(weather)));

                        tvWeatherRecommend.setText(getRecommendMessage(weather));

                        ArrayList<Exercise> indoor = getIndoorExercises();
                        ArrayList<Exercise> outdoor = getOutdoorExercises();

                        // UI 초기화
                        gridIndoor.removeAllViews();
                        gridOutdoor.removeAllViews();

                        switch (weather) {
                            case "Clear":
                                // 맑음 → 실외 + 실내 전체
                                addExerciseCards(gridIndoor, indoor);
                                addExerciseCards(gridOutdoor, outdoor);
                                break;

                            case "Clouds":
                                // 흐림 → 산책 등 실외 기본 운동 + 실내 전체
                                ArrayList<Exercise> cloudsOutdoor = new ArrayList<>();
                                for (Exercise ex : outdoor) {
                                    if (ex.name.contains("조깅") || ex.name.contains("파워워킹")) {
                                        cloudsOutdoor.add(ex);
                                    }
                                }
                                addExerciseCards(gridIndoor, indoor);
                                addExerciseCards(gridOutdoor, cloudsOutdoor);
                                break;

                            case "Drizzle":
                                // 이슬비 → 실내 운동만
                                addExerciseCards(gridIndoor, indoor);
                                break;

                            case "Rain":
                            case "Snow":
                            case "Thunderstorm":
                                // 위험한 날씨 → 실내 운동만
                                addExerciseCards(gridIndoor, indoor);
                                break;

                            default:
                                // 예외 → 실내 + 실외 전체
                                addExerciseCards(gridIndoor, indoor);
                                addExerciseCards(gridOutdoor, outdoor);
                                break;
                        }

                        layoutRecommend.setVisibility(View.VISIBLE);

                    } catch (JSONException e) {
                        Toast.makeText(requireContext(), "날씨 정보 파싱 오류", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(requireContext(), "날씨 API 오류", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }

    private String translateWeather(String w) {
        switch (w) {
            case "Clear": return "맑음";
            case "Clouds": return "흐림";
            case "Rain": return "비";
            case "Snow": return "눈";
            case "Drizzle": return "이슬비";
            case "Thunderstorm": return "천둥번개";
            default: return w;
        }
    }

    private String getRecommendMessage(String weather) {
        switch (weather) {
            case "Clear": return "날씨가 좋아요! 가벼운 조깅이나 야외 운동 어때요?";
            case "Clouds": return "흐린 날엔 산책이나 실내 운동이 좋아요!";
            case "Rain": return "비가 와요. 실내에서 코어 운동이나 스트레칭을 추천해요!";
            case "Snow": return "눈 오는 날엔 미끄러울 수 있어요. 실내 운동을 권장해요!";
            case "Drizzle": return "이슬비가 내려요. 가볍게 실내 운동을 해보세요!";
            case "Thunderstorm": return "⚡ 위험한 날씨! 반드시 실내 운동하세요!";
            default: return "오늘은 컨디션에 맞는 운동을 선택해보세요!";
        }
    }

    private void recommendRandom() {
        // 현재 날씨 기준으로 화면에 표시된 운동만 랜덤 추천
        ArrayList<Exercise> availableExercises = new ArrayList<>();

        // 화면에 보여진 실내 운동
        for (int i = 0; i < gridIndoor.getChildCount(); i++) {
            Object tag = gridIndoor.getChildAt(i).getTag();
            if (tag instanceof Exercise) {
                availableExercises.add((Exercise) tag);
            }
        }

        // 화면에 보여진 실외 운동
        for (int i = 0; i < gridOutdoor.getChildCount(); i++) {
            Object tag = gridOutdoor.getChildAt(i).getTag();
            if (tag instanceof Exercise) {
                availableExercises.add((Exercise) tag);
            }
        }

        if (!availableExercises.isEmpty() && isAdded()) {
            int index = (int) (Math.random() * availableExercises.size());
            openDetail(availableExercises.get(index));
        } else {
            Toast.makeText(requireContext(), "추천할 운동이 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addExerciseCards(GridLayout grid, ArrayList<Exercise> exercises) {
        int cardWidthPx = dpToPx(110);
        int cardHeightPx = dpToPx(135);
        int imageSizePx = dpToPx(52);

        for (Exercise ex : exercises) {
            LinearLayout card = new LinearLayout(requireContext());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2));
            card.setGravity(Gravity.CENTER_HORIZONTAL);
            card.setBackgroundResource(R.drawable.bg_exercise_card);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = cardWidthPx;
            params.height = cardHeightPx;
            params.setMargins(dpToPx(7), dpToPx(7), dpToPx(7), dpToPx(7));
            card.setLayoutParams(params);

            ImageView iv = new ImageView(requireContext());
            if (ex.iconRes != 0) iv.setImageResource(ex.iconRes);
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(imageSizePx, imageSizePx);
            imageParams.gravity = Gravity.CENTER_HORIZONTAL;
            iv.setLayoutParams(imageParams);

            TextView tv = new TextView(requireContext());
            tv.setText(ex.name != null ? ex.name : "운동명 없음");
            tv.setTextColor(0xFF111827);
            tv.setTextSize(15);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(0, dpToPx(4), 0, 0);

            tv.setSingleLine(false);
            tv.setMaxLines(2);
            tv.setEllipsize(null);
            tv.setWidth(cardWidthPx - dpToPx(4));

            card.addView(iv);
            card.addView(tv);

            // 운동 객체 태그로 저장
            card.setTag(ex);

            card.setOnClickListener(v -> openDetail(ex));

            grid.addView(card);
        }
    }

    private int dpToPx(int dp) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void openDetail(Exercise e) {
        if (!isAdded()) return;

        Intent intent = new Intent(requireContext(), ExerciseDetailActivity.class);
        intent.putExtra("exercise_name", e.name != null ? e.name : "운동명 없음");
        intent.putExtra("exercise_desc", e.description != null ? e.description : "설명이 없습니다.");
        intent.putExtra("exercise_level", e.level != null ? e.level : "☆☆☆☆☆");
        intent.putExtra("exercise_icon", e.iconRes);
        intent.putExtra("exercise_mood", selectedMood);
        startActivity(intent);
    }

    private ArrayList<Exercise> getIndoorExercises() {
        ArrayList<Exercise> list = new ArrayList<>();
        list.add(new Exercise(
                "스쿼트",
                "하체 강화, 체지방 감소\n\n[방법] 다리를 어깨너비로 벌리고, 무릎과 엉덩이를 뒤로 빼며 앉았다가 일어섭니다. 허리는 곧게 펴고 무릎이 발끝을 넘지 않게!",
                "★★★☆☆", R.drawable.ic_squat));          // (3/5)

        list.add(new Exercise(
                "플랭크",
                "코어 근육 강화\n\n[방법] 팔꿈치를 어깨 아래 두고, 몸을 일직선으로 유지해 30~60초 버팁니다. 엉덩이가 올라가거나 허리가 꺾이지 않게 유지하세요.",
                "★★★☆☆", R.drawable.ic_plank));          // (3/5)

        list.add(new Exercise(
                "버피\n테스트",
                "전신 운동, 칼로리 소모 높음\n\n[방법] 완전히 서있는 자세 → 스쿼트 → 팔꿈치 짚고 점프 → 푸시업 → 다시 일어서서 점프! 연속 반복.",
                "★★★★☆", R.drawable.ic_burpee));         // (4/5)

        list.add(new Exercise(
                "런지",
                "하체 집중, 균형감 개선\n\n[방법] 한 발을 앞으로 내딛고 무릎을 90도로 굽혀 앉았다 일어섭니다. 좌우 번갈아 실시, 무릎이 발끝을 넘지 않게!",
                "★★★☆☆", R.drawable.ic_lunge));           // (3/5)

        list.add(new Exercise(
                "점핑잭",
                "유산소 + 심박수 상승\n\n[방법] 두 발을 모으고 서있다 다리를 벌리고 팔을 들어 점프! 팔과 다리를 동시에 양옆으로 벌리고 모으는 동작 반복.",
                "★★★☆☆", R.drawable.ic_jumpingjack));      // (3/5)

        list.add(new Exercise(
                "크런치",
                "복근 운동\n\n[방법] 무릎을 세우고 누운 다음, 복부 힘으로 어깨를 들어올립니다. 허리를 완전히 들지 않고, 복근만 자극.",
                "★★☆☆☆", R.drawable.ic_crunch));          // (2/5)

        list.add(new Exercise(
                "힙브릿지",
                "둔근 강화, 허리 지지\n\n[방법] 무릎을 구부리고 누워 엉덩이를 들어올립니다. 등-무릎 일직선, 엉덩이와 허리 힘으로 천천히 올렸다 내립니다.",
                "★★☆☆☆", R.drawable.ic_hipbridge));        // (2/5)

        return list;
    }

    private ArrayList<Exercise> getOutdoorExercises() {
        ArrayList<Exercise> list = new ArrayList<>();
        list.add(new Exercise(
                "조깅",
                "전신 유산소, 체력 향상\n\n[방법] 리듬 있게 천천히 달리면서, 시선은 정면, 어깨와 팔은 편안하게!",
                "★★★☆☆", R.drawable.ic_jogging));             // (3/5)

        list.add(new Exercise(
                "파워워킹",
                "누구나 가능한 유산소 운동\n\n[방법] 빠르게 걷기. 팔을 가볍게 흔들며, 발뒤꿈치부터 착지해서 전진하세요.",
                "★★☆☆☆", R.drawable.ic_powerwalk));           // (2/5)

        list.add(new Exercise(
                "자전거",
                "하체 강화, 지속적 유산소\n\n[방법] 안장 높이를 체형에 맞추고, 페달을 일정한 리듬으로 계속 밟으세요. 바른 자세 유지!",
                "★★★☆☆", R.drawable.ic_cycling));              // (3/5)

        list.add(new Exercise(
                "등산",
                "근지구력 향상\n\n[방법] 등산화 착용, 등산 스틱 활용. 한 걸음씩 착실히 걷고, 경사에서는 무릎에 충격이 가지 않게 주의!",
                "★★★★☆", R.drawable.ic_hiking));               // (4/5)

        list.add(new Exercise(
                "줄넘기",
                "고효율 유산소, 칼로리 소모\n\n[방법] 양손에 줄을 쥐고 규칙적으로 점프. 무릎은 약간 굽히고, 발 앞부분만 착지!",
                "★★★★☆", R.drawable.ic_jumprope));              // (4/5)

        list.add(new Exercise(
                "야외\n스트레칭",
                "가볍게 시작하기 좋음\n\n[방법] 목·어깨·팔·허리·다리 등 부위별로 천천히 늘리고, 깊게 숨 쉬면서 10~30초 유지합니다.",
                "★★☆☆☆", R.drawable.ic_stretching));            // (2/5)

        list.add(new Exercise(
                "공원\n스포츠",
                "재미 + 유산소 + 협응\n\n[방법] 농구, 배드민턴, 축구, 피구 등 야외 스포츠를 10분 이상 즐겨 운동 효과 업!",
                "★★★☆☆", R.drawable.ic_sports));                 // (3/5)

        return list;
    }


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
