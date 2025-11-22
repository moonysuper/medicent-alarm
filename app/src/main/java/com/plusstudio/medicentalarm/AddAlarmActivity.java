package com.plusstudio.medicentalarm;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.util.Locale;

public class AddAlarmActivity extends AppCompatActivity {

    private EditText etReason, etStartTime, etRepeatCount;
    private RadioGroup rgPeriod;
    private DatabaseHelper dbHelper;
    private int selectedHour = 0, selectedMinute = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        dbHelper = new DatabaseHelper(this);

        etReason = findViewById(R.id.etReason);
        etStartTime = findViewById(R.id.etStartTime);
        etRepeatCount = findViewById(R.id.etRepeatCount);
        rgPeriod = findViewById(R.id.rgPeriod);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnExit = findViewById(R.id.btnExit);

        etStartTime.setOnClickListener(v -> showTimePicker());
        etStartTime.setFocusable(false);

        btnSave.setOnClickListener(v -> saveAlarm());
        btnExit.setOnClickListener(v -> finish());
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
        if (startTime.isEmpty()) {
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

        Alarm alarm = new Alarm(0, reason, startTime, repeatCount, period);
        long result = dbHelper.addAlarm(alarm);

        if (result != -1) {
            Toast.makeText(this, "تم حفظ التنبيه بنجاح", Toast.LENGTH_SHORT).show();
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
    }
}
