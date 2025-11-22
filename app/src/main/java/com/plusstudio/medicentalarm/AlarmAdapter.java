package com.plusstudio.medicentalarm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private List<Alarm> alarmList;
    private OnAlarmDeleteListener deleteListener;

    public interface OnAlarmDeleteListener {
        void onDelete(Alarm alarm);
    }

    public AlarmAdapter(List<Alarm> alarmList, OnAlarmDeleteListener listener) {
        this.alarmList = alarmList;
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        Alarm alarm = alarmList.get(position);

        holder.tvReason.setText(alarm.getReason());
        holder.tvTime.setText(alarm.getStartTime());
        holder.tvRepeat.setText(alarm.getRepeatCount() + " مرات يومياً");
        holder.tvPeriod.setText(alarm.getPeriod());

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("حذف التنبيه")
                    .setMessage("هل أنت متأكد من حذف هذا التنبيه؟")
                    .setPositiveButton("نعم", (dialog, which) -> {
                        if (deleteListener != null) {
                            deleteListener.onDelete(alarm);
                        }
                    })
                    .setNegativeButton("لا", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView tvReason, tvTime, tvRepeat, tvPeriod;
        ImageButton btnDelete;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvRepeat = itemView.findViewById(R.id.tvRepeat);
            tvPeriod = itemView.findViewById(R.id.tvPeriod);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
