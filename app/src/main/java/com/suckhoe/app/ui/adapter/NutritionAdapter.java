package com.suckhoe.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.suckhoe.app.R;
import com.suckhoe.app.data.model.NutritionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NutritionAdapter extends RecyclerView.Adapter<NutritionAdapter.ViewHolder> {
    private List<NutritionItem> items = new ArrayList<>();
    private final OnItemDeleteListener deleteListener;

    public interface OnItemDeleteListener {
        void onDelete(NutritionItem item);
    }

    public NutritionAdapter(OnItemDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setItems(List<NutritionItem> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nutrition, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NutritionItem item = items.get(position);
        holder.tvFoodName.setText(item.getFoodName());
        holder.tvMealType.setText(item.getMealType());
        holder.tvCalories.setText(String.format(Locale.getDefault(), "%d kcal", item.getCalories()));
        holder.tvProtein.setText(String.format(Locale.getDefault(), "Đạm: %.1fg", item.getProtein()));
        holder.tvCarbs.setText(String.format(Locale.getDefault(), "Carb: %.1fg", item.getCarbs()));
        holder.tvFat.setText(String.format(Locale.getDefault(), "Béo: %.1fg", item.getFat()));

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
        TextView tvFoodName, tvMealType, tvCalories, tvProtein, tvCarbs, tvFat;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tv_item_food_name);
            tvMealType = itemView.findViewById(R.id.tv_item_meal_type);
            tvCalories = itemView.findViewById(R.id.tv_item_calories);
            tvProtein = itemView.findViewById(R.id.tv_item_protein);
            tvCarbs = itemView.findViewById(R.id.tv_item_carbs);
            tvFat = itemView.findViewById(R.id.tv_item_fat);
            btnDelete = itemView.findViewById(R.id.btn_delete_nutrition);
        }
    }
}
