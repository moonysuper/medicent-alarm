package com.plusstudio.medicentalarm;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.Calendar;
import java.util.Locale;

public class AddAlarmActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_CODE = 100;

    private EditText etReason, etStartTime, etRepeatCount;
    private RadioGroup rgPeriod;
    private CheckBox cbTestMode;
    private DatabaseHelper dbHelper;
    private AlarmScheduler alarmScheduler;
    private int selectedHour = 0, selectedMinute = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        dbHelper = new DatabaseHelper(this);
        alarmScheduler = new AlarmScheduler(this);

        initViews();
        requestNotificationPermission();
    }

    private void initViews() {
        etReason = findViewById(R.id.etReason);
        etStartTime = findViewById(R.id.etStartTime);
        etRepeatCount = findViewById(R.id.etRepeatCount);
        rgPeriod = findViewById(R.id.rgPeriod);
        cbTestMode = findViewById(R.id.cbTestMode);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnExit = findViewById(R.id.btnExit);

        etStartTime.setOnClickListener(v -> showTimePicker());
        etStartTime.setFocusable(false);

        btnSave.setOnClickListener(v -> saveAlarm());
        btnExit.setOnClickListener(v -> finish());
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        TimePickerDialog picker = new TimePickerDialog(this, (view, h, m) -> {
            selectedHour = h;
            selectedMinute = m;
            String time = String.format(Locale.getDefault(), "%02d:%02d", h, m);
            etStartTime.setText(time);
        }, hour, minute, true);
        picker.show();
    }

    private void saveAlarm() {
        String reason = etReason.getText().toString().trim();
        String startTime = etStartTime.getText().toString().trim();
        String repeatCountStr = etRepeatCount.getText().toString().trim();

        if (reason.isEmpty()) {
            etReason.setError("أدخل سبب التنبيه");
            return;
        }

        // في وضع التجربة، لا نحتاج وقت محدد
        boolean isTestMode = cbTestMode.isChecked();

        if (!isTestMode && startTime.isEmpty()) {
            Toast.makeText(this, "اختر وقت بداية التنبيه", Toast.LENGTH_SHORT).show();
            return;
        }

        if (repeatCountStr.isEmpty()) {
            etRepeatCount.setError("أدخل عدد مرات التنبيه");
            return;
        }

        int repeatCount = Integer.parseInt(repeatCountStr);
        int selectedPeriodId = rgPeriod.getCheckedRadioButtonId();

        if (selectedPeriodId == -1) {
            Toast.makeText(this, "اختر فترة التنبيه", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadio = findViewById(selectedPeriodId);
        String period = selectedRadio.getText().toString();

        // إذا كان وضع التجربة، استخدم الوقت الحالي
        if (isTestMode && startTime.isEmpty()) {
            Calendar now = Calendar.getInstance();
            startTime = String.format(Locale.getDefault(), "%02d:%02d",
                    now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
        }

        Alarm alarm = new Alarm(0, reason, startTime, repeatCount, period);
        long result = dbHelper.addAlarm(alarm);

        if (result != -1) {
            alarm.setId((int) result);

            if (isTestMode) {
                // وضع التجربة - تنبيه بعد دقيقة
                alarmScheduler.scheduleTestAlarm(alarm);
                Toast.makeText(this, "✅ تم حفظ التنبيه\n⏱ سيصلك تنبيه تجريبي بعد دقيقة!",
                        Toast.LENGTH_LONG).show();
            } else {
                // الوضع العادي
                alarmScheduler.scheduleAlarm(alarm);
                Toast.makeText(this, "تم حفظ وجدولة التنبيه بنجاح", Toast.LENGTH_SHORT).show();
            }

            clearFields();
        } else {
            Toast.makeText(this, "فشل في حفظ التنبيه", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        etReason.setText("");
        etStartTime.setText("");
        etRepeatCount.setText("");
        rgPeriod.clearCheck();
        ((RadioButton) findViewById(R.id.rbDaily)).setChecked(true);
        cbTestMode.setChecked(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "تم تفعيل الإشعارات", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "يرجى تفعيل الإشعارات لاستقبال التنبيهات",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}