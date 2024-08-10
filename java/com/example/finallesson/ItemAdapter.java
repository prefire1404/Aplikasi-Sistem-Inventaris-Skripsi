package com.example.finallesson;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<Item> itemList;
    private Context context;
    private FirebaseFirestore db;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
    }

    public ItemAdapter(List<Item> itemList, Context context, OnItemClickListener listener) {
        this.itemList = itemList;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item currentItem = itemList.get(position);
        holder.tvId.setText("ID: " + currentItem.getId());
        holder.tvItemName.setText("Nama: " + currentItem.getName());
        holder.tvItemCategory.setText("Kategori: " + currentItem.getCategory());
        holder.tvItemCondition.setText("Kondisi: " + currentItem.getCondition());
        holder.tvItemQuantity.setText("Jumlah: " + currentItem.getQuantity());
        holder.tvItemDateAdded.setText("Tanggal Masuk: " + currentItem.getDateAdded());

        if (currentItem.getImageUrl() != null) {
            Glide.with(context)
                    .load(currentItem.getImageUrl())
                    .into(holder.ivItemImage);
        } else {
            holder.ivItemImage.setImageResource(R.drawable.ic_add_photo); // Set placeholder
        }

        holder.ivDeleteItem.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }


    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvItemName, tvItemCategory, tvItemCondition, tvItemQuantity, tvItemDateAdded;
        ImageView ivDeleteItem, ivItemImage;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvID);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemCategory = itemView.findViewById(R.id.tvItemCategory);
            tvItemCondition = itemView.findViewById(R.id.tvItemCondition);
            tvItemQuantity = itemView.findViewById(R.id.tvItemQuantity);
            tvItemDateAdded = itemView.findViewById(R.id.tvItemDateAdded);
            ivDeleteItem = itemView.findViewById(R.id.ivDeleteItem);
            ivItemImage = itemView.findViewById(R.id.tvImage);
        }
    }
}