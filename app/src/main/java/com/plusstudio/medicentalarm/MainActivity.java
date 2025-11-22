package com.plusstudio.medicentalarm;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.app.AlarmManager;
import java.util.ArrayList;
import java.util.List;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.RingtoneManager;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity implements AlarmAdapter.OnAlarmDeleteListener {

    private static final int NOTIFICATION_PERMISSION_CODE = 100;

    private RecyclerView recyclerView;
    private AlarmAdapter adapter;
    private List<Alarm> alarmList;
    private DatabaseHelper dbHelper;
    private AlarmScheduler alarmScheduler;
    private TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        alarmScheduler = new AlarmScheduler(this);

        recyclerView = findViewById(R.id.alarmsRecyclerView);
        emptyText = findViewById(R.id.emptyText);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddAlarm);

        alarmList = new ArrayList<>();
        adapter = new AlarmAdapter(alarmList, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddAlarmActivity.class);
            startActivity(intent);
        });

        // زر التجربة الفورية - اضغط مطولاً على FAB
        fabAdd.setOnLongClickListener(v -> {
            testNotificationDirectly();
            return true;
        });

        // طلب الصلاحيات عند فتح التطبيق
        checkAndRequestPermissions();
    }

    // تجربة الإشعار مباشرة
    private void testNotificationDirectly() {
        Toast.makeText(this, "🧪 جاري إرسال إشعار تجريبي...", Toast.LENGTH_SHORT).show();

        // إنشاء قناة الإشعارات
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "test_channel",
                    "قناة التجربة",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableVibration(true);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // صوت المنبه (وليس الإشعار العادي)
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        // بناء الإشعار
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "test_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("🧪 تجربة ناجحة!")
                .setContentText("الإشعارات تعمل بشكل صحيح")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSound(alarmSound)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE);

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(9999, builder.build());
            Toast.makeText(this, "✅ تم إرسال الإشعار!", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, "❌ لا يوجد إذن للإشعارات!\nالرجاء تفعيله من الإعدادات", Toast.LENGTH_LONG).show();
            // فتح إعدادات التطبيق
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            startActivity(intent);
        }
    }

    private void checkAndRequestPermissions() {
        // 1. طلب إذن الإشعارات (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                new AlertDialog.Builder(this)
                        .setTitle("🔔 إذن الإشعارات")
                        .setMessage("نحتاج إذن الإشعارات لإرسال تنبيهات الأدوية في موعدها")
                        .setPositiveButton("موافق", (dialog, which) -> {
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                                    NOTIFICATION_PERMISSION_CODE);
                        })
                        .setNegativeButton("لاحقاً", null)
                        .show();
            }
        }

        // 2. التحقق من إذن المنبهات الدقيقة (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                new AlertDialog.Builder(this)
                        .setTitle("⏰ إذن المنبهات")
                        .setMessage("نحتاج إذن المنبهات الدقيقة لضمان وصول التنبيهات في وقتها بالضبط")
                        .setPositiveButton("فتح الإعدادات", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        })
                        .setNegativeButton("لاحقاً", null)
                        .show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAlarms();
    }

    private void loadAlarms() {
        alarmList.clear();
        alarmList.addAll(dbHelper.getAllAlarms());
        adapter.notifyDataSetChanged();

        if (alarmList.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDelete(Alarm alarm) {
        alarmScheduler.cancelAlarm(alarm);
        dbHelper.deleteAlarm(alarm.getId());
        loadAlarms();
        Toast.makeText(this, "تم حذف التنبيه", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "✅ تم تفعيل الإشعارات", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "⚠️ لن تصلك التنبيهات بدون إذن الإشعارات",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}