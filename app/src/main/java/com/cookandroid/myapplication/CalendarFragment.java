package com.cookandroid.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.GridLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class CalendarFragment extends Fragment {

    GridLayout gridCalendar;
    LinearLayout listContainer;
    TextView tvMonthTitle;
    ImageView btnPrev, btnNext;

    // DB 연동 및 실제 데이터 저장 변수
    private DBHelper dbHelper;
    private HashMap<Integer, List<Record>> dailyRecordMap = new HashMap<>();

    // 현재 달 이동 값(0 = 이번달, -1 = 이전달, +1 = 다음달)
    int monthOffset = 0;

    // 예시 운동 기록 데이터
    HashMap<Integer, String> recordMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_calendar, container, false);

        gridCalendar = v.findViewById(R.id.grid_calendar);
        listContainer = v.findViewById(R.id.list_container);
        tvMonthTitle = v.findViewById(R.id.tv_month_title);
        btnPrev = v.findViewById(R.id.btn_prev_month);
        btnNext = v.findViewById(R.id.btn_next_month);

        dbHelper = new DBHelper(getContext());

        buildCalendar();

        // ⬅ 이전달 버튼
        btnPrev.setOnClickListener(vw -> {
            monthOffset--;
            buildCalendar();

            listContainer.removeAllViews();
        });

        // ➡ 다음달 버튼
        btnNext.setOnClickListener(vw -> {
            monthOffset++;
            buildCalendar();

            listContainer.removeAllViews();
        });

        return v;
    }

    private void buildCalendar() {

        loadRecord();
        gridCalendar.removeAllViews();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, monthOffset); // ← 핵심 : 이동된 월로 세팅

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        // 상단 월 제목 업데이트
        tvMonthTitle.setText(year + "년 " + (month + 1) + "월");

        // 날짜 계산 시작
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        int todayYear = Calendar.getInstance().get(Calendar.YEAR);
        int todayMonth = Calendar.getInstance().get(Calendar.MONTH);
        int todayDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        int dayNum = 1;

        for (int i = 0; i < 42; i++) {

            TextView tv = new TextView(getContext());
            tv.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            tv.setTextSize(16);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setPadding(0, 10, 0, 0);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 150;
            params.columnSpec = GridLayout.spec(i % 7, 1f);
            params.rowSpec = GridLayout.spec(i / 7);
            params.setMargins(4, 4, 4, 4);

            tv.setLayoutParams(params);

            if (i < firstDayOfWeek || dayNum > maxDay) {
                tv.setText("");
                gridCalendar.addView(tv);
                continue;
            }

            tv.setText(String.valueOf(dayNum));
            tv.setTextColor(Color.parseColor("#111111"));

            if (i % 7 == 0) tv.setTextColor(Color.parseColor("#E53935"));
            if (i % 7 == 6) tv.setTextColor(Color.parseColor("#1E88E5"));

            // 오늘 표시 (달이 같을 때만)
            if (year == todayYear && month == todayMonth && dayNum == todayDay) {
                tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_today_border));
            }

            // ● 운동 기록
            if (dailyRecordMap.containsKey(dayNum)) {
                tv.append("\n●");
            }

            final int selectedDay = dayNum;
            tv.setOnClickListener(v -> showRecord(selectedDay));

            gridCalendar.addView(tv);
            dayNum++;
        }
    }

    private void showRecord(int day) {

        listContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, monthOffset);

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;

        List<Record> records = dailyRecordMap.get(day);
        if (records != null && !records.isEmpty()) {
            for (Record record : records) {
                View item = inflater.inflate(R.layout.item_record, listContainer, false);

                TextView tvDate = item.findViewById(R.id.tv_record_date);
                TextView tvInfo = item.findViewById(R.id.tv_record_info);

                tvDate.setText(year + "년 " + month + "월 " + day + "일");
                String emoji = getMoodEmoji(record.getMood());
                String info = record.getName() + " · " + emoji;
                tvInfo.setText(info);

                // 상세 페이지 이동
                item.setOnClickListener(v -> {
                    Intent intent = new Intent(getContext(), RecordDetailActivity.class);

                    intent.putExtra("record_id", record.getId());

                    startActivity(intent);
                });

                listContainer.addView(item);
            }
        } else {
            View item = inflater.inflate(R.layout.item_record, listContainer, false);
            TextView tvDate = item.findViewById(R.id.tv_record_date);
            TextView tvInfo = item.findViewById(R.id.tv_record_info);

            tvDate.setText(year + "년 " + month + "월 " + day + "일");
            tvInfo.setText("기록 없음"); // 기록이 없을 때만 표시

            listContainer.addView(item);
        }
    }

    private void loadRecord() {

        List<Record> allRecords = dbHelper.getAllRecords();

        dailyRecordMap.clear();

        // 현재 년/월 설정
        Calendar currentCal = Calendar.getInstance();
        currentCal.add(Calendar.MONTH, monthOffset);
        int targetYear = currentCal.get(Calendar.YEAR);
        int targetMonth = currentCal.get(Calendar.MONTH) + 1;

        for (Record record : allRecords) {

            String[] dateParts = record.getDate().split("-");

            if (dateParts.length < 3) continue;

            try {
                int recordYear = Integer.parseInt(dateParts[0]);
                int recordMonth = Integer.parseInt(dateParts[1]);

                if (recordYear == targetYear && recordMonth == targetMonth) {

                    int recordDay = Integer.parseInt(dateParts[2]);

                    // List에 기록을 누적하여 저장
                    List<Record> recordsForDay = dailyRecordMap.get(recordDay);

                    if (recordsForDay == null) {
                        recordsForDay = new ArrayList<>();
                        dailyRecordMap.put(recordDay, recordsForDay);
                    }

                    recordsForDay.add(record);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    private String getMoodEmoji(String mood) {
        if (mood == null) return "😐";
        switch (mood) {
            case "좋음": return "😊";
            case "보통": return "😐";
            case "별로": return "😡";
        }
        return "😐";
    }
}
