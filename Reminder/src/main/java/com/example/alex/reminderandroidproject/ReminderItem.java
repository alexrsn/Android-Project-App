package com.example.alex.reminderandroidproject;

class ReminderItem {
    private String title;
    private String note;
    private long dateInMillis;
    private String phoneNumber;
    private int repeat;
    static final String[] reminderRepeatStrings = {"None", "Yearly", "Monthly", "Weekly", "Daily", "Hourly"};


    ReminderItem(String title, String note, long dateInMillis, String phoneNumber, int repeat) {
        this.title = title;
        this.note = note;
        this.dateInMillis = dateInMillis;
        this.phoneNumber = phoneNumber;
        this.repeat = repeat;
    }

    String getTitle() {
        return title;
    }

    String getNote() {
        return note;
    }

    int getRepeat() {
        return repeat;
    }

    long getDateInMillis() {
        return dateInMillis;
    }

    String getPhoneNumber() {
        return phoneNumber;
    }
}
