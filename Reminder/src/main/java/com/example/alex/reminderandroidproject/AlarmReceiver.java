package com.example.alex.reminderandroidproject;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.NotificationCompat;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String ID = "id";
    public static final String REPEAT = "repeat";
    public static final String DATE_IN_MILLIS = "date";
    public static final String TITLE = "title";
    public static final String NOTE = "note";
    public static final int REPEAT_NONE = 0;
    public static final int REPEAT_YEARLY = 1;
    public static final int REPEAT_MONTHLY = 2;
    public static final int REPEAT_WEEKLY = 3;
    public static final int REPEAT_DAILY = 4;
    public static final int REPEAT_HOURLY = 5;
    private DBAdapter dbAdapter;

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.setContentTitle("Reminder: " + intent.getStringExtra(TITLE));
        notificationBuilder.setContentText(intent.getStringExtra(NOTE));
        notificationBuilder.setAutoCancel(true);
        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        notificationBuilder.setContentIntent(pendingIntent);
        notificationBuilder.setDefaults(Notification.DEFAULT_ALL);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notificationBuilder.build());
        long nextAlarmTime = intent.getLongExtra(DATE_IN_MILLIS, System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(nextAlarmTime);
        int repeat = intent.getIntExtra(REPEAT, 0);
        if (dbAdapter == null) {
            dbAdapter = new DBAdapter(context);
        }
        if (!dbAdapter.isDBOpen(true)) {
            dbAdapter.open(true);
        }
        switch (repeat) {
            case REPEAT_NONE:
                dbAdapter.setCompleted(intent.getStringExtra(ID));
                break;
            case REPEAT_YEARLY:
                calendar.add(Calendar.YEAR, 1);
                nextAlarmTime = calendar.getTimeInMillis();
                dbAdapter.setTime(intent.getStringExtra(ID), nextAlarmTime);
                break;
            case REPEAT_MONTHLY:
                calendar.add(Calendar.MONTH, 1);
                nextAlarmTime = calendar.getTimeInMillis();
                dbAdapter.setTime(intent.getStringExtra(ID), nextAlarmTime);
                break;
            case REPEAT_WEEKLY:
                nextAlarmTime += AlarmManager.INTERVAL_DAY * 7;
                dbAdapter.setTime(intent.getStringExtra(ID), nextAlarmTime);
                break;
            case REPEAT_DAILY:
                nextAlarmTime += AlarmManager.INTERVAL_DAY;
                dbAdapter.setTime(intent.getStringExtra(ID), nextAlarmTime);
                break;
            case REPEAT_HOURLY:
                nextAlarmTime += AlarmManager.INTERVAL_HOUR;
                dbAdapter.setTime(intent.getStringExtra(ID), nextAlarmTime);
                break;
        }
        setAlarm(context);
    }

    public void setAlarm(Context context) {
        if (dbAdapter == null) {
            dbAdapter = new DBAdapter(context);
        }
        if (!dbAdapter.isDBOpen(false)) {
            dbAdapter.open(false);
        }
        Cursor cursor = dbAdapter.getNearest();
        if (cursor.moveToFirst()) {
            long alarmTime = cursor.getLong(cursor.getColumnIndexOrThrow(DBAdapter.KEY_DATE_IN_MILLIS));
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent alarmIntent = new Intent(context, AlarmReceiver.class);
            String title = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_TITLE));
            alarmIntent.putExtra(TITLE, title);
            alarmIntent.putExtra(NOTE, cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_NOTE)));
            alarmIntent.putExtra(DATE_IN_MILLIS, cursor.getLong(cursor.getColumnIndexOrThrow(DBAdapter.KEY_DATE_IN_MILLIS)));
            alarmIntent.putExtra(ID, cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_ID)));
            alarmIntent.putExtra(REPEAT, cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_REPEAT)));
            PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, 0,
                    alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, alarmPendingIntent);
        }
        dbAdapter.close();
    }

    public static int daysLeft(long day1, long day2) {
        Double delta = (double) (day1 - day2);
        delta = delta / 1000 / 60 / 60 / 24;
        return delta < 0 ? -1 : delta.intValue();
    }

}
