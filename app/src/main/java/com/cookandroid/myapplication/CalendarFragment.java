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

    private DBHelper dbHelper;
    private HashMap<Integer, List<Record>> dailyRecordMap = new HashMap<>();

    int monthOffset = 0;

    // ⭐ 선택된 날짜 저장
    private int selectedDay = -1;

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

        btnPrev.setOnClickListener(vw -> {
            monthOffset--;
            selectedDay = -1;   // 월 이동 시 선택 초기화
            buildCalendar();
            listContainer.removeAllViews();
        });

        btnNext.setOnClickListener(vw -> {
            monthOffset++;
            selectedDay = -1;
            buildCalendar();
            listContainer.removeAllViews();
        });

        return v;
    }

    private void buildCalendar() {

        loadRecord();
        gridCalendar.removeAllViews();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, monthOffset);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        tvMonthTitle.setText(year + "년 " + (month + 1) + "월");

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // 오늘 날짜
        Calendar today = Calendar.getInstance();
        int todayYear = today.get(Calendar.YEAR);
        int todayMonth = today.get(Calendar.MONTH);
        int todayDay = today.get(Calendar.DAY_OF_MONTH);

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

            boolean isToday = (year == todayYear && month == todayMonth && dayNum == todayDay);
            boolean isSelected = (dayNum == selectedDay);

            // ⭐ 선택 / 오늘 순서 중요!
            if (isSelected) {
                tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_selected_border));
            } else if (isToday) {
                tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_today_gray_border));
            }

            if (dailyRecordMap.containsKey(dayNum)) {
                tv.append("\n●");
            }

            final int dayCopy = dayNum;

            tv.setOnClickListener(v -> {
                selectedDay = dayCopy;
                buildCalendar();
                showRecord(dayCopy);
            });

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
                tvInfo.setText(record.getName() + " · " + emoji);

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
            tvInfo.setText("기록 없음");

            listContainer.addView(item);
        }
    }

    private void loadRecord() {

        List<Record> allRecords = dbHelper.getAllRecords();

        dailyRecordMap.clear();

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
