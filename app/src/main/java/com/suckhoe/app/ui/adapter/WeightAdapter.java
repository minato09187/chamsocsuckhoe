package com.suckhoe.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.suckhoe.app.R;
import com.suckhoe.app.data.model.WeightRecord;
import com.suckhoe.app.util.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WeightAdapter extends RecyclerView.Adapter<WeightAdapter.ViewHolder> {
    private List<WeightRecord> items = new ArrayList<>();
    private final OnItemDeleteListener deleteListener;

    public interface OnItemDeleteListener {
        void onDelete(WeightRecord record);
    }

    public WeightAdapter(OnItemDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setItems(List<WeightRecord> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weight, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeightRecord record = items.get(position);
        holder.tvDate.setText(DateUtils.formatDisplayDate(record.getLoggedDate()));
        holder.tvWeight.setText(String.format(Locale.getDefault(), "%.1f kg", record.getWeight()));
        holder.tvBmi.setText(String.format(Locale.getDefault(), "BMI: %.1f", record.getBmi()));
        holder.tvBmiCat.setText(String.format(Locale.getDefault(), "(%s)", record.getBmiCategory()));

        if (record.getNote() != null && !record.getNote().isEmpty()) {
            holder.tvNote.setText(record.getNote());
            holder.tvNote.setVisibility(View.VISIBLE);
        } else {
            holder.tvNote.setVisibility(View.GONE);
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(record);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvWeight, tvBmi, tvBmiCat, tvNote;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_item_weight_date);
            tvWeight = itemView.findViewById(R.id.tv_item_weight_val);
            tvBmi = itemView.findViewById(R.id.tv_item_bmi);
            tvBmiCat = itemView.findViewById(R.id.tv_item_bmi_cat);
            tvNote = itemView.findViewById(R.id.tv_item_weight_note);
            btnDelete = itemView.findViewById(R.id.btn_delete_weight);
        }
    }
}
