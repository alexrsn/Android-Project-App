package com.example.alex.reminderandroidproject;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

class ReminderCursorAdapter extends CursorAdapter {

    ReminderCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.item_reminder, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView txtTitle = (TextView) view.findViewById(R.id.txtTitle);
        TextView txtNote = (TextView) view.findViewById(R.id.txtNote);
        TextView txtDate = (TextView) view.findViewById(R.id.txtDate);
        TextView txtTime = (TextView) view.findViewById(R.id.txtTime);
        TextView txtRepeat = (TextView) view.findViewById(R.id.txtRepeat);
        ImageView imgCall = (ImageView) view.findViewById(R.id.imgCall);
        ImageView imgDelete = (ImageView) view.findViewById(R.id.imgDelete);
        TextView txtDaysLeft = (TextView) view.findViewById(R.id.txtDaysLeft);

        txtTitle.setText(String.format("Title: %s", cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_TITLE))));
        txtNote.setText(String.format("Note: %s", cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_NOTE))));
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm",Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(cursor.getLong(cursor.getColumnIndexOrThrow(DBAdapter.KEY_DATE_IN_MILLIS)));
        txtDate.setText(dateFormat.format(calendar.getTime()));
        txtTime.setText(timeFormat.format(calendar.getTime()));
        txtRepeat.setText(ReminderItem.reminderRepeatStrings[cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_REPEAT))]);
        int daysLeft = AlarmReceiver.daysLeft(calendar.getTimeInMillis(), System.currentTimeMillis());
        txtDaysLeft.setText(daysLeft < 0 ? "Passed" : "Days left: " + daysLeft);
        imgCall.setTag(cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_PHONE)));
        if (imgCall.getTag().equals("")) {
            imgCall.setVisibility(View.GONE);
        } else {
            imgCall.setVisibility(View.VISIBLE);
        }
        String itemId = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_ID));
        imgDelete.setTag(itemId);
    }


}
