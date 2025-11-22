package com.plusstudio.medicentalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;
import java.util.Calendar;

public class AlarmScheduler {

    private Context context;
    private AlarmManager alarmManager;

    public AlarmScheduler(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    /**
     * جدولة منبه جديد
     * @param alarm بيانات المنبه
     */
    public void scheduleAlarm(Alarm alarm) {
        if (alarmManager == null) return;

        // تحويل الوقت
        String[] timeParts = alarm.getStartTime().split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        // حساب الفاصل الزمني بين التنبيهات حسب عدد المرات
        int repeatCount = alarm.getRepeatCount();

        // جدولة كل تنبيه
        for (int i = 0; i < repeatCount; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            // إضافة فاصل زمني لكل تنبيه (توزيع على اليوم)
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
            intent.putExtra("reason", alarm.getReason());
            intent.putExtra("alarm_id", uniqueId);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, uniqueId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // حساب فترة التكرار
            long intervalMillis = getIntervalMillis(alarm.getPeriod());

            // جدولة المنبه
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        intervalMillis,
                        pendingIntent
                );
            }
        }
    }

    /**
     * جدولة تنبيه تجريبي سريع (بعد دقيقة واحدة)
     */
    public void scheduleTestAlarm(Alarm alarm) {
        if (alarmManager == null) return;

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 1); // بعد دقيقة واحدة

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("reason", "🧪 تجربة: " + alarm.getReason());
        intent.putExtra("alarm_id", alarm.getId());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, alarm.getId() + 9000, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        } else {
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }

        Toast.makeText(context, "سيصل التنبيه التجريبي بعد دقيقة واحدة!", Toast.LENGTH_LONG).show();
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
    }

    private long getIntervalMillis(String period) {
        switch (period) {
            case "يومي":
                return AlarmManager.INTERVAL_DAY;
            case "أسبوعي":
                return AlarmManager.INTERVAL_DAY * 7;
            case "شهري":
                return AlarmManager.INTERVAL_DAY * 30;
            case "دائم":
            default:
                return AlarmManager.INTERVAL_DAY;
        }
    }
}