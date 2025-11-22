package com.plusstudio.medicentalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import java.util.Calendar;

public class AlarmScheduler {

    private static final String TAG = "AlarmScheduler";
    private Context context;
    private AlarmManager alarmManager;

    public AlarmScheduler(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    /**
     * جدولة منبه جديد - يعمل حتى لو التطبيق مغلق
     */
    public void scheduleAlarm(Alarm alarm) {
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager غير متوفر");
            return;
        }

        String[] timeParts = alarm.getStartTime().split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        int repeatCount = alarm.getRepeatCount();

        for (int i = 0; i < repeatCount; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            if (repeatCount > 1) {
                int intervalHours = 24 / repeatCount;
                calendar.add(Calendar.HOUR_OF_DAY, i * intervalHours);
            }

            // إذا كان الوقت قد مضى، اجعله للغد
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            int uniqueId = alarm.getId() * 100 + i;

            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.setAction("com.example.medicinealarm.ALARM_TRIGGERED");
            intent.putExtra("reason", alarm.getReason());
            intent.putExtra("alarm_id", uniqueId);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, uniqueId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // جدولة المنبه
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setAlarmClock(
                                new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pendingIntent),
                                pendingIntent
                        );
                    } else {
                        alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                calendar.getTimeInMillis(),
                                pendingIntent
                        );
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAlarmClock(
                            new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pendingIntent),
                            pendingIntent
                    );
                } else {
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                }

                Log.d(TAG, "✅ تم جدولة المنبه: " + alarm.getReason() + " في " + calendar.getTime());
            } catch (SecurityException e) {
                Log.e(TAG, "❌ لا يوجد إذن للمنبهات: " + e.getMessage());
            }
        }
    }

    /**
     * تنبيه تجريبي فوري
     */
    public void scheduleTestAlarm(Alarm alarm) {
        Log.d(TAG, "جدولة تنبيه تجريبي للمنبه: " + alarm.getReason());

        Toast.makeText(context, "⏳ التنبيه سيصل بعد 3 ثواني...", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Log.d(TAG, "إرسال التنبيه الآن!");

            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("reason", "🧪 تجربة: " + alarm.getReason());
            intent.putExtra("alarm_id", alarm.getId() + 9000);

            AlarmReceiver receiver = new AlarmReceiver();
            receiver.onReceive(context, intent);

        }, 3000);
    }

    /**
     * جدولة تنبيه حقيقي بعد ثواني محددة (للتجربة مع التطبيق مغلق)
     */
    public void scheduleRealTestAlarm(Alarm alarm, int seconds) {
        if (alarmManager == null) return;

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, seconds);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("com.example.medicinealarm.ALARM_TRIGGERED");
        intent.putExtra("reason", "🧪 تجربة: " + alarm.getReason());
        intent.putExtra("alarm_id", alarm.getId() + 8000);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, alarm.getId() + 8000, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAlarmClock(
                            new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pendingIntent),
                            pendingIntent
                    );
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAlarmClock(
                        new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pendingIntent),
                        pendingIntent
                );
            }

            Toast.makeText(context, "✅ سيصل التنبيه بعد " + seconds + " ثانية\nيمكنك إغلاق التطبيق!",
                    Toast.LENGTH_LONG).show();
            Log.d(TAG, "✅ تم جدولة تنبيه تجريبي بعد " + seconds + " ثانية");
        } catch (Exception e) {
            Log.e(TAG, "❌ خطأ في جدولة المنبه: " + e.getMessage());
            Toast.makeText(context, "❌ فشل جدولة المنبه", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * إلغاء منبه
     */
    public void cancelAlarm(Alarm alarm) {
        for (int i = 0; i < alarm.getRepeatCount(); i++) {
            int uniqueId = alarm.getId() * 100 + i;

            Intent intent = new Intent(context, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, uniqueId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
        }
        Log.d(TAG, "✅ تم إلغاء المنبه: " + alarm.getReason());
    }
}