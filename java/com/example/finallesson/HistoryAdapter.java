package com.example.finallesson;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<HistoryItem> historyList;
    private OnHistoryItemClickListener historyItemClickListener;

    public HistoryAdapter(List<HistoryItem> historyList, OnHistoryItemClickListener historyItemClickListener) {
        this.historyList = historyList;
        this.historyItemClickListener = historyItemClickListener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem item = historyList.get(position);
        holder.tvAction.setText(item.getAction());
        holder.tvDate.setText(item.getDate());
        holder.itemView.setOnClickListener(v -> {
            if (historyItemClickListener != null) {
                historyItemClickListener.onHistoryItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public void updateList(List<HistoryItem> newList) {
        historyList = newList;
        notifyDataSetChanged();
    }

    public interface OnHistoryItemClickListener {
        void onHistoryItemClick(HistoryItem item);
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvAction;
        TextView tvDate;

        HistoryViewHolder(View itemView) {
            super(itemView);
            tvAction = itemView.findViewById(R.id.tvAction);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
