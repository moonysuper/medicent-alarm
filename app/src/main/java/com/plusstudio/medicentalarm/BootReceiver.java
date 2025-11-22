package com.plusstudio.medicentalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.List;


public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            AlarmScheduler scheduler = new AlarmScheduler(context);

            List<Alarm> alarms = dbHelper.getAllAlarms();
            for (Alarm alarm : alarms) {
                scheduler.scheduleAlarm(alarm);
            }
        }
    }
}
