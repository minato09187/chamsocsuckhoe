package com.suckhoe.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.suckhoe.app.R;
import com.suckhoe.app.data.model.WorkoutExercise;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.ViewHolder> {
    private List<WorkoutExercise> items = new ArrayList<>();
    private final OnItemDeleteListener deleteListener;

    public interface OnItemDeleteListener {
        void onDelete(WorkoutExercise item);
    }

    public WorkoutAdapter(OnItemDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setItems(List<WorkoutExercise> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutExercise item = items.get(position);
        holder.tvName.setText(item.getExerciseName());
        holder.tvMuscle.setText(item.getMuscleGroup());
        holder.tvCalories.setText(String.format(Locale.getDefault(), "-%d kcal", item.getCaloriesBurned()));

        String weightStr = item.getWeight() > 0 ? String.format(Locale.getDefault(), "  •  %.1f kg", item.getWeight()) : "";
        holder.tvSpecs.setText(String.format(Locale.getDefault(), "%d hiệp × %d lần%s  •  %d phút",
                item.getSets(), item.getReps(), weightStr, item.getDurationMinutes()));

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMuscle, tvCalories, tvSpecs;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_item_workout_name);
            tvMuscle = itemView.findViewById(R.id.tv_item_muscle);
            tvCalories = itemView.findViewById(R.id.tv_item_workout_calories);
            tvSpecs = itemView.findViewById(R.id.tv_item_workout_specs);
            btnDelete = itemView.findViewById(R.id.btn_delete_workout);
        }
    }
}

