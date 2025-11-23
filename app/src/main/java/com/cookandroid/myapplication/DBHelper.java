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
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LEVEL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOOD)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEMO))
                );
                // TIMESTAMP는 Record 객체에 세터로 설정
                record.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));

                recordList.add(record);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        return recordList;
    }
}
