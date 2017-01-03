package com.example.alex.reminderandroidproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    private ActionBarDrawerToggle toggle;
    private DrawerLayout menuDrawerLayout;
    private RelativeLayout mainLayout;
    private ListView reminderListView;
    private SearchView searchView;
    private MenuItem searchMenuItem;
    private MenuItem addMenuItem;
    private ReminderCursorAdapter reminderCursorAdapter;
    private DBAdapter dbAdapter;
    private Cursor reminderCursor;
    private AlarmReceiver alarmReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        menuDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView menuDrawerListView = (ListView) findViewById(R.id.lv_navigation_drawer);
        mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
        toggle = new ActionBarDrawerToggle(this, menuDrawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        menuDrawerLayout.addDrawerListener(toggle);
        DrawerMenuItem[] drawerMenuItems = {
                new DrawerMenuItem("To Do Reminders", R.drawable.ic_alarm_black_48dp),
                new DrawerMenuItem("Import Birthdays", R.drawable.ic_cake_black_24dp),
                new DrawerMenuItem("Report Bug", R.drawable.ic_bug_report_black_48dp),
                new DrawerMenuItem("Exit", R.drawable.ic_exit_to_app_black_48dp)};
        menuDrawerListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        menuDrawerListView.setAdapter(new DrawerMenuItemArrayAdapter(this, drawerMenuItems));
        menuDrawerListView.setOnItemClickListener(new DrawerItemClickListener());

        dbAdapter = new DBAdapter(this);
        if (!dbAdapter.isDBOpen(false)) {
            dbAdapter.open(false);
        }

        alarmReceiver = new AlarmReceiver();
        showReminders();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbAdapter != null) {
            dbAdapter.close();
        }
        if (reminderCursor != null) {
            reminderCursor.close();
        }
    }

    public void btnCall(View view) {
        Uri number = Uri.parse("tel:" + view.getTag());
        Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
        startActivity(callIntent);
    }

    public void btnDelete(final View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure to delete this reminder?")
                .setTitle("Delete reminder")
                .setIcon(R.drawable.ic_delete_black_48dp);
        builder.setNegativeButton("cancel", null);
        builder.setPositiveButton("delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (!dbAdapter.isDBOpen(true)) {
                    dbAdapter.open(true);
                }
                dbAdapter.removeReminder((String) view.getTag());
                reminderCursor = dbAdapter.getReminders();
                reminderCursorAdapter.changeCursor(reminderCursor);
                alarmReceiver.setAlarm(getBaseContext());

            }
        });
        AlertDialog deleteDialog = builder.create();
        deleteDialog.show();
    }


    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            if (searchView != null && !searchView.getQuery().equals("")) {
                searchView.setQuery("", false);
                searchView.setIconified(true);
            }
            switch (position) {
                case 0:
                    //To Do Reminder
                    showReminders();
                    break;
                case 1:
                    //Import Birthdays
                    importBirthdays();
                    break;
                case 2:
                    //Report Bug
                    sendBugReport();
                    break;
                case 3:
                    //Exit
                    finish();
                    break;
            }
            menuDrawerLayout.closeDrawers();
        }
    }


    public void showReminders() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("To Do Reminders");
        }
        mainLayout.removeAllViews();
        reminderCursor = dbAdapter.getReminders();
        if (reminderCursorAdapter == null) {
            reminderCursorAdapter = new ReminderCursorAdapter(this, reminderCursor, 0);
        } else {
            reminderCursorAdapter.notifyDataSetChanged();
        }
        if (reminderListView == null) {
            reminderListView = new ListView(this);
            reminderListView.setAdapter(reminderCursorAdapter);
            reminderListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    final String reminderToEditId = view.findViewById(R.id.imgDelete).getTag().toString();
                    ReminderAddDialog reminderAddDialog = new ReminderAddDialog(MainActivity.this);
                    if (!dbAdapter.isDBOpen(false)) {
                        dbAdapter.open(false);
                    }
                    Cursor cursor = dbAdapter.getReminderById(reminderToEditId);
                    cursor.moveToFirst();
                    ReminderItem reminderItem = new ReminderItem(
                            cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_TITLE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_NOTE)),
                            cursor.getLong(cursor.getColumnIndexOrThrow(DBAdapter.KEY_DATE_IN_MILLIS)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_PHONE)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_REPEAT)));
                    reminderAddDialog.show();
                    reminderAddDialog.fillDialogForEdit(reminderItem);
                    reminderAddDialog.setDialogResult(new ReminderAddDialog.ReminderAddDialogResult() {
                        @Override
                        public void onDialogFinish(ReminderItem newReminderItem) {
                            if (!dbAdapter.isDBOpen(true)) {
                                dbAdapter.open(true);
                            }
                            dbAdapter.editReminder(reminderToEditId, newReminderItem);
                            reminderCursor = dbAdapter.getReminders();
                            reminderCursorAdapter.changeCursor(reminderCursor);

                            alarmReceiver.setAlarm(getBaseContext());
                        }
                    });
                    return false;
                }
            });
        }
        mainLayout.addView(reminderListView);
        if (addMenuItem != null) {
            addMenuItem.setVisible(true);
        }
        if (searchMenuItem != null) {
            searchMenuItem.setVisible(true);
        }
    }

    public void importBirthdays() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure?")
                .setTitle("Import Birthdays")
                .setIcon(R.drawable.ic_cake_black_24dp);
        builder.setNegativeButton("cancel", null);
        builder.setPositiveButton("import", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Uri uri = ContactsContract.Data.CONTENT_URI;
                String[] columns  = new String[]{
                        ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Event.CONTACT_ID,
                        ContactsContract.CommonDataKinds.Event.START_DATE,
                };
                String where = ContactsContract.Data.MIMETYPE + "= ? AND " +
                        ContactsContract.CommonDataKinds.Event.TYPE + "=" +
                        ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY;
                String[] selectionArgs = new String[]{
                        ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE
                };
                Cursor cursor = getContentResolver().query(uri, columns, where, selectionArgs, null);

                if (cursor != null) {
                    int contactBDayColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Event.START_DATE);
                    int contactNameColumn = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    while (cursor.moveToNext()) {
                        if (!dbAdapter.isDBOpen(true)) {
                            dbAdapter.open(true);
                        }
                        Calendar currentDate = Calendar.getInstance();
                        Calendar calendar = Calendar.getInstance();
                        try {
                            calendar.setTime(dateFormat.parse(cursor.getString(contactBDayColumn)));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        calendar.set(Calendar.HOUR_OF_DAY, 10);
                        calendar.set(Calendar.YEAR, currentDate.get(Calendar.YEAR));
                        if (calendar.getTimeInMillis() < currentDate.getTimeInMillis()) {
                            calendar.add(Calendar.YEAR, 1);
                        }
                        String contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Event.CONTACT_ID));
                        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                                new String[]{contactId},
                                null);
                        String contactNumber = "";

                        if (cursorPhone != null && cursorPhone.moveToFirst()) {
                            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            cursorPhone.close();
                        }
                        dbAdapter.addReminder(new ReminderItem(
                                cursor.getString(contactNameColumn) + " Birthday!",
                                "Don't forget to wish a Happy Birthday!",
                                calendar.getTimeInMillis(),
                                contactNumber,
                                1));
                    }
                    cursor.close();
                    reminderCursor = dbAdapter.getReminders();
                    reminderCursorAdapter.changeCursor(reminderCursor);
                    alarmReceiver.setAlarm(getBaseContext());
                }
            }
        });
        AlertDialog importBirthdaysDialog = builder.create();
        importBirthdaysDialog.show();
    }


    private void sendBugReport() {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@reminder.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Report Bug - Reminder App");
        try {
            startActivity(Intent.createChooser(emailIntent, "Choose email client..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this, "No email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reminder, menu);
        searchMenuItem = menu.findItem(R.id.actionSearch);
        addMenuItem = menu.findItem(R.id.actionAdd);
        searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals("")) {
                    reminderCursor = dbAdapter.getReminders();
                    reminderCursorAdapter.changeCursor(reminderCursor);

                } else {
                    reminderCursor = dbAdapter.queryReminders(newText);
                    reminderCursorAdapter.changeCursor(reminderCursor);
                }
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionAdd:
                final ReminderAddDialog reminderAddDialog = new ReminderAddDialog(MainActivity.this);
                reminderAddDialog.show();
                reminderAddDialog.setDialogResult(new ReminderAddDialog.ReminderAddDialogResult() {
                    @Override
                    public void onDialogFinish(ReminderItem newReminderItem) {
                        if (!dbAdapter.isDBOpen(true)) {
                            dbAdapter.open(true);
                        }
                        dbAdapter.addReminder(newReminderItem);
                        reminderCursor = dbAdapter.getReminders();
                        reminderCursorAdapter.changeCursor(reminderCursor);
                        alarmReceiver.setAlarm(getBaseContext());
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }
}
