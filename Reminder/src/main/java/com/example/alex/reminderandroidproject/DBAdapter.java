package com.example.alex.reminderandroidproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.Closeable;

class DBAdapter extends SQLiteOpenHelper implements Closeable {

    private static final int DATABASE_VERSION = 1;
    private SQLiteDatabase db;

    private static final String DATABASE_NAME = "ReminderDB.db";
    private static final String REMINDERS_TABLE_NAME = "Reminders";
    static final String KEY_ID = "_id";
    static final String KEY_TITLE = "title";
    static final String KEY_NOTE = "note";
    static final String KEY_DATE_IN_MILLIS = "dateInMillis";
    static final String KEY_PHONE = "phone";
    static final String KEY_REPEAT = "repeat";
    private static final String KEY_COMPLETED = "completed";
    private static final String[] REMINDER_COLUMNS =
            {KEY_ID + " as _id", KEY_TITLE, KEY_NOTE, KEY_DATE_IN_MILLIS, KEY_PHONE, KEY_REPEAT, KEY_COMPLETED};
    private static final String CREATE_REMINDERS =
            "CREATE TABLE " + REMINDERS_TABLE_NAME + " ("
                    + KEY_ID + " integer primary key autoincrement,"
                    + KEY_TITLE + " text,"
                    + KEY_NOTE + " text,"
                    + KEY_DATE_IN_MILLIS + " integer,"
                    + KEY_PHONE + " text,"
                    + KEY_REPEAT + " integer,"
                    + KEY_COMPLETED + " integer)";


    DBAdapter(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    long addReminder(ReminderItem reminderItem) {
        if (db == null || !db.isOpen() || db.isReadOnly())
            return -1L;
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, reminderItem.getTitle());
        values.put(KEY_NOTE, reminderItem.getNote());
        values.put(KEY_DATE_IN_MILLIS, reminderItem.getDateInMillis());
        values.put(KEY_PHONE, reminderItem.getPhoneNumber());
        values.put(KEY_REPEAT, reminderItem.getRepeat());
        values.put(KEY_COMPLETED, 0);
        return db.insert(REMINDERS_TABLE_NAME, null, values);
    }

    long editReminder(String itemId, ReminderItem reminderItem) {
        if (db == null || !db.isOpen() || db.isReadOnly())
            return -1L;
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, reminderItem.getTitle());
        values.put(KEY_NOTE, reminderItem.getNote());
        values.put(KEY_DATE_IN_MILLIS, reminderItem.getDateInMillis());
        values.put(KEY_PHONE, reminderItem.getPhoneNumber());
        values.put(KEY_REPEAT, reminderItem.getRepeat());
        values.put(KEY_COMPLETED, 0);
        return db.update(REMINDERS_TABLE_NAME, values, KEY_ID + "=" + itemId, null);
    }


    void removeReminder(String itemId) {
        if (db == null || !db.isOpen() || db.isReadOnly())
            return;
        db.delete(REMINDERS_TABLE_NAME, KEY_ID + "=" + itemId, null);
    }

    Cursor getReminders() {
        if (db == null || !db.isOpen())
            return null;
        return db.query(REMINDERS_TABLE_NAME, REMINDER_COLUMNS, null, null, null, null, KEY_ID + " ASC");
    }

    Cursor getReminderById(String id) {
        if (db == null || !db.isOpen())
            return null;
        String selection = KEY_ID + " like " + id;
        return db.query(REMINDERS_TABLE_NAME, REMINDER_COLUMNS, selection, null, null, null, null);
    }

    Cursor getNearest() {
        if (db == null || !db.isOpen())
            return null;
        String selection = KEY_COMPLETED + " == 0";
        return db.query(REMINDERS_TABLE_NAME, REMINDER_COLUMNS, selection, null, null, null, KEY_DATE_IN_MILLIS, "1");
    }

    Cursor queryReminders(String query) {
        if (db == null || !db.isOpen())
            return null;
        String selection = KEY_TITLE + " like ?";
        String[] selectionArgs = new String[]{"%" + query + "%"};
        return db.query(REMINDERS_TABLE_NAME, REMINDER_COLUMNS, selection, selectionArgs, null, null, KEY_ID + " ASC");
    }

    long setTime(String itemId, long newTime) {
        if (db == null || !db.isOpen() || db.isReadOnly())
            return -1L;
        ContentValues values = new ContentValues();
        values.put(KEY_DATE_IN_MILLIS, newTime);
        return db.update(REMINDERS_TABLE_NAME, values, KEY_ID + "=" + itemId, null);

    }

    long setCompleted(String itemId) {
        if (db == null || !db.isOpen() || db.isReadOnly())
            return -1L;
        ContentValues values = new ContentValues();
        values.put(KEY_COMPLETED, 1);
        return db.update(REMINDERS_TABLE_NAME, values, KEY_ID + "=" + itemId, null);
    }

    @Override
    public void close() {
        Log.d("Alex", "db close()");
        if (db != null)
            db.close();
    }

    public SQLiteDatabase open(boolean write) {
        if (write)
            db = getWritableDatabase();
        else
            db = getReadableDatabase();
        return db;
    }

    boolean isDBOpen(boolean write) {
        if (db != null) {
            if (write) {
                return db.isOpen();
            } else {
                return db.isOpen() || db.isReadOnly();
            }
        } else {
            return false;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_REMINDERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("Alex", "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXIST " + REMINDERS_TABLE_NAME);
        onCreate(db);
    }

}
