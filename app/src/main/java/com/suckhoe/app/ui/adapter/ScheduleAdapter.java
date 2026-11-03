package com.suckhoe.app.ui.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.suckhoe.app.R;
import com.suckhoe.app.data.model.WorkoutSchedule;

import java.util.ArrayList;
import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {
    private List<WorkoutSchedule> items = new ArrayList<>();
    private final OnScheduleActionListener listener;

    public interface OnScheduleActionListener {
        void onToggleCompleted(WorkoutSchedule schedule, boolean isCompleted);
        void onDelete(WorkoutSchedule schedule);
    }

    public ScheduleAdapter(OnScheduleActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<WorkoutSchedule> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutSchedule item = items.get(position);
        holder.tvDay.setText(item.getDayOfWeek());
        holder.tvTime.setText(item.getTimeOfDay());
        holder.tvTitle.setText(item.getTitle());
        holder.tvMuscle.setText("Mục tiêu: " + (item.getTargetMuscle() != null ? item.getTargetMuscle() : "Toàn thân"));

        if (item.getNotes() != null && !item.getNotes().isEmpty()) {
            holder.tvNotes.setText(item.getNotes());
            holder.tvNotes.setVisibility(View.VISIBLE);
        } else {
            holder.tvNotes.setVisibility(View.GONE);
        }

        // Checkbox status & strike-through text
        holder.cbCompleted.setOnCheckedChangeListener(null);
        holder.cbCompleted.setChecked(item.isCompleted());
        if (item.isCompleted()) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setCompleted(isChecked);
            if (isChecked) {
                holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
            if (listener != null) {
                listener.onToggleCompleted(item, isChecked);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvTime, tvTitle, tvMuscle, tvNotes;
        CheckBox cbCompleted;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tv_schedule_day);
            tvTime = itemView.findViewById(R.id.tv_schedule_time);
            tvTitle = itemView.findViewById(R.id.tv_schedule_title);
            tvMuscle = itemView.findViewById(R.id.tv_schedule_muscle);
            tvNotes = itemView.findViewById(R.id.tv_schedule_notes);
            cbCompleted = itemView.findViewById(R.id.cb_schedule_completed);
            btnDelete = itemView.findViewById(R.id.btn_delete_schedule);
        }
    }
}

