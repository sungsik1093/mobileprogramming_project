package com.cookandroid.myapplication;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "cert_records.db";
    private static final int DATABASE_VERSION = 1;

    // 테이블 이름 정의
    public static final String TABLE_NAME = "records";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "exercise_name";
    public static final String COLUMN_DATE = "record_date";
    public static final String COLUMN_PHOTO = "photo_path";
    public static final String COLUMN_LEVEL = "exercise_level";
    public static final String COLUMN_MOOD = "mood";
    public static final String COLUMN_MEMO = "memo";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    // 테이블 생성 쿼리
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_NAME + " TEXT," +
                    COLUMN_DATE + " TEXT," +
                    COLUMN_PHOTO + " TEXT," +
                    COLUMN_LEVEL + " TEXT," +
                    COLUMN_MOOD + " TEXT," +
                    COLUMN_MEMO + " TEXT," +
                    COLUMN_TIMESTAMP + " INTEGER)";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean isRecordExists(String date, String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean exists = false;
        Cursor cursor = null;

        try {
            cursor = db.query(
                    TABLE_NAME,
                    new String[]{COLUMN_ID},
                    COLUMN_DATE + " = ? AND " + COLUMN_NAME + " = ?",
                    new String[]{date, name},
                    null, null, null, "1" // LIMIT 1
            );

            exists = (cursor.getCount() > 0);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return exists;
    }

    // 전체 record 가져오기
    public List<Record> getAllRecords() {
        List<Record> recordList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Record record = new Record(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LEVEL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOOD)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEMO)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                );

                recordList.add(record);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        return recordList;
    }

    // 하나 record 가져오기
    public Record getRecordById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Record record = null;
        Cursor cursor = null;

        try {
            cursor = db.query(
                    TABLE_NAME,
                    null, // 모든 컬럼
                    COLUMN_ID + " = ?", // ID로 WHERE 조건 설정
                    new String[]{String.valueOf(id)},
                    null, null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {

                // 커서에서 모든 데이터 추출
                long recordId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                String photoPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO));
                String level = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LEVEL));
                String mood = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOOD));
                String memo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEMO));
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));

                // Record 객체 생성
                record = new Record(recordId, name, date, photoPath, level, mood, memo, timestamp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return record;
    }
}
