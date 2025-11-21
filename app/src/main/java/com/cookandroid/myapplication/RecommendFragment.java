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
    private FusedLocationProviderClient fusedLocationClient;
    private final String API_KEY = "b173bcdd6617f3a5dc76e5136f9ba1c0";
    private double userLat = 37.5501;
    private double userLon = 126.9237;

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
        queue = Volley.newRequestQueue(requireContext());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        requestLocation();

        btnRandom.setOnClickListener(view -> recommendRandom());

        return v;
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(),
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

                        gridIndoor.removeAllViews();
                        gridOutdoor.removeAllViews();

                        if (weather.equals("Rain") || weather.equals("Snow") ||
                                weather.equals("Thunderstorm")) {
                            addExerciseCards(gridIndoor, indoor);
                        } else {
                            addExerciseCards(gridIndoor, indoor);
                            addExerciseCards(gridOutdoor, outdoor);
                        }

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
        ArrayList<Exercise> all = new ArrayList<>();
        all.addAll(getIndoorExercises());
        all.addAll(getOutdoorExercises());

        if (!all.isEmpty() && isAdded()) {
            int index = (int) (Math.random() * all.size());
            openDetail(all.get(index));
        }
    }

    private void addExerciseCards(GridLayout grid, ArrayList<Exercise> exercises) {
        for (Exercise ex : exercises) {
            LinearLayout card = new LinearLayout(requireContext());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(20, 20, 20, 20);
            card.setGravity(Gravity.CENTER);
            card.setBackgroundResource(R.drawable.bg_exercise_card);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(16, 16, 16, 16);
            card.setLayoutParams(params);

            ImageView iv = new ImageView(requireContext());
            if (ex.iconRes != 0) iv.setImageResource(ex.iconRes);
            iv.setLayoutParams(new LinearLayout.LayoutParams(100, 100));

            TextView tv = new TextView(requireContext());
            tv.setText(ex.name != null ? ex.name : "운동명 없음");
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
        if (!isAdded()) return;

        Intent intent = new Intent(requireContext(), ExerciseDetailActivity.class);
        // ✅ ExerciseDetailActivity와 키 일치
        intent.putExtra("exercise_name", e.name != null ? e.name : "운동명 없음");
        intent.putExtra("exercise_desc", e.description != null ? e.description : "설명이 없습니다.");
        intent.putExtra("exercise_level", e.level != null ? e.level : "☆☆☆☆☆");
        intent.putExtra("exercise_icon", e.iconRes);
        startActivity(intent);
    }

    private ArrayList<Exercise> getIndoorExercises() {
        ArrayList<Exercise> list = new ArrayList<>();
        list.add(new Exercise("스쿼트", "하체 강화, 체지방 감소", "★★☆", R.drawable.ic_squat));
        list.add(new Exercise("플랭크", "코어 근육 강화", "★★☆", R.drawable.ic_plank));
        list.add(new Exercise("버피 테스트", "전신 운동, 칼로리 소모 높음", "★★★☆", R.drawable.ic_burpee));
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