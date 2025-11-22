package com.plusstudio.medicentalarm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AlarmAdapter.OnAlarmDeleteListener {

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
        // إلغاء المنبه المجدول
        alarmScheduler.cancelAlarm(alarm);
        // حذف من قاعدة البيانات
        dbHelper.deleteAlarm(alarm.getId());
        // إعادة تحميل القائمة
        loadAlarms();
    }
}