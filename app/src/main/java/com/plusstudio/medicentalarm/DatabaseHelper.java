package com.plusstudio.medicentalarm;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MedicineAlarm.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_ALARMS = "alarms";
    private static final String COL_ID = "id";
    private static final String COL_REASON = "reason";
    private static final String COL_START_TIME = "start_time";
    private static final String COL_REPEAT_COUNT = "repeat_count";
    private static final String COL_PERIOD = "period";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_ALARMS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_REASON + " TEXT, " +
                COL_START_TIME + " TEXT, " +
                COL_REPEAT_COUNT + " INTEGER, " +
                COL_PERIOD + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALARMS);
        onCreate(db);
    }

    public long addAlarm(Alarm alarm) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_REASON, alarm.getReason());
        values.put(COL_START_TIME, alarm.getStartTime());
        values.put(COL_REPEAT_COUNT, alarm.getRepeatCount());
        values.put(COL_PERIOD, alarm.getPeriod());

        long result = db.insert(TABLE_ALARMS, null, values);
        db.close();
        return result;
    }

    public List<Alarm> getAllAlarms() {
        List<Alarm> alarmList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ALARMS + " ORDER BY " + COL_ID + " DESC", null);

        if (cursor.moveToFirst()) {
            do {
                Alarm alarm = new Alarm(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_REASON)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_START_TIME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_REPEAT_COUNT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_PERIOD))
                );
                alarmList.add(alarm);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return alarmList;
    }

    public void deleteAlarm(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ALARMS, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
}
