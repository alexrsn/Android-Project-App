package com.example.alex.reminderandroidproject;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

class ReminderAddDialog extends Dialog implements View.OnClickListener {

    private TextView txtDate;
    private TextView txtTime;
    private EditText txtTitle;
    private EditText txtNote;
    private EditText txtPhone;
    private Spinner spnRepeat;
    private ReminderAddDialogResult dialogResult;
    private Calendar calendar;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    ReminderAddDialog(Activity activity) {
        super(activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_reminder);
        txtTitle = (EditText) findViewById(R.id.txtTitle);
        txtNote = (EditText) findViewById(R.id.txtNote);
        txtPhone = (EditText) findViewById(R.id.txtPhone);
        txtDate = (TextView) findViewById(R.id.txtDate);
        txtTime = (TextView) findViewById(R.id.txtTime);
        spnRepeat = (Spinner) findViewById(R.id.spnRepeat);
        Button save = (Button) findViewById(R.id.btnSave);
        Button cancel = (Button) findViewById(R.id.btnCancel);
        calendar = Calendar.getInstance();
        txtDate.setText(dateFormat.format(calendar.getTimeInMillis()));
        txtTime.setText(timeFormat.format(calendar.getTimeInMillis()));
        spnRepeat.setAdapter(new ArrayAdapter<>(getContext(), R.layout.item_spinner, ReminderItem.reminderRepeatStrings));
        save.setOnClickListener(this);
        cancel.setOnClickListener(this);
        txtDate.setOnClickListener(this);
        txtTime.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSave:
                if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                    Toast.makeText(getContext(), "Please set reminder to upcoming time", Toast.LENGTH_SHORT).show();
                } else if (txtTitle.getText().toString().equals("")) {
                    Toast.makeText(getContext(), "Please set reminder title", Toast.LENGTH_SHORT).show();
                } else {
                    ReminderItem newReminderItem = new ReminderItem(
                            txtTitle.getText().toString(),
                            txtNote.getText().toString(),
                            calendar.getTimeInMillis(),
                            txtPhone.getText().toString(),
                            spnRepeat.getSelectedItemPosition());
                    if (dialogResult != null) {
                        dialogResult.onDialogFinish(newReminderItem);
                    }
                    dismiss();
                }
                break;
            case R.id.btnCancel:
                dismiss();
                break;
            case R.id.txtDate:
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        txtDate.setText(String.format("%s/%s/%s", day < 10 ? "0" + day : day, month + 1 < 10 ? "0" + (month + 1) : (month + 1), year));
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, day);
                    }
                }, year, month, day);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                datePickerDialog.show();
                break;
            case R.id.txtTime:
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        txtTime.setText(String.format
                                ("%s:%s", hourOfDay < 10 ? "0" + hourOfDay : hourOfDay, minute < 10 ? "0" + minute : minute));
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        calendar.set(Calendar.SECOND, 0);
                    }
                }, hour, minute, DateFormat.is24HourFormat(getContext()));
                timePickerDialog.show();
                break;
            default:
                dismiss();
                break;
        }
    }

    void setDialogResult(ReminderAddDialogResult dialogResult) {
        this.dialogResult = dialogResult;
    }

    void fillDialogForEdit(ReminderItem reminderItem) {
        txtTitle.setText(reminderItem.getTitle());
        txtNote.setText(reminderItem.getNote());
        txtPhone.setText(reminderItem.getPhoneNumber());
        calendar.setTimeInMillis(reminderItem.getDateInMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        txtDate.setText(dateFormat.format(calendar.getTime()));
        txtTime.setText(timeFormat.format(calendar.getTime()));
        spnRepeat.setSelection(reminderItem.getRepeat());
    }

    interface ReminderAddDialogResult {
        void onDialogFinish(ReminderItem newReminderItem);
    }
}

