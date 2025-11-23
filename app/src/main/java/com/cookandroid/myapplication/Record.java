package com.cookandroid.myapplication;

public class Record {

    private long id;
    private String name;
    private String date;
    private String photoPath;
    private String level;
    private String mood;
    private String memo;
    private long timestamp;

    public Record(long id, String name, String date, String photoPath, String level, String mood, String memo, long timestamp) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.photoPath = photoPath;
        this.level = level;
        this.mood = mood;
        this.memo = memo;
        this.timestamp = timestamp;
    }

    public Record(String name, String date, String photoPath, String level, String mood, String memo) {
        this.name = name;
        this.date = date;
        this.photoPath = photoPath;
        this.level = level;
        this.mood = mood;
        this.memo = memo;
        this.timestamp = System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public String getLevel() {
        return level;
    }

    public String getMood() {
        return mood;
    }

    public String getMemo() {
        return memo;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {this.timestamp = timestamp; }
}
