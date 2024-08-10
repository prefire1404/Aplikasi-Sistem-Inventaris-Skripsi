package com.example.finallesson;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ViewListBarang extends AppCompatActivity {

    private RecyclerView rvItems;
    private ItemAdapter itemAdapter;
    private List<Item> itemList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Button addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_list_barang);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addButton = findViewById(R.id.VLaddButton);
        rvItems = findViewById(R.id.rvItems);
        rvItems.setLayoutManager(new LinearLayoutManager(this));

        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(itemList, this, new ItemAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(int position) {
                deleteItemFromFirestore(position);
            }
        });
        rvItems.setAdapter(itemAdapter);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        fetchItemsFromFirestore();

        addButton.setOnClickListener(view -> {
            Intent addIntent = new Intent(ViewListBarang.this, TambahBarang.class);
            startActivity(addIntent);
        });

    }

    private void deleteItemFromFirestore(int position) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Item itemToDelete = itemList.get(position);
            String documentId = itemToDelete.getDocumentId();
            db.collection("Users").document(user.getUid()).collection("Items")
                    .document(documentId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        itemList.remove(position);
                        itemAdapter.notifyItemRemoved(position);
                        Toast.makeText(ViewListBarang.this, "Data Terhapus", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ViewListBarang.this, "Error deleting item", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void fetchItemsFromFirestore() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("Users").document(user.getUid()).collection("Items").get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        itemList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Item item = document.toObject(Item.class);
                            item.setDocumentId(document.getId());
                            itemList.add(item);
                        }

                        // Sort itemList based on id converted to integer
                        Collections.sort(itemList, (item1, item2) -> {
                            try {
                                int id1 = Integer.parseInt(item1.getId());
                                int id2 = Integer.parseInt(item2.getId());
                                return Integer.compare(id1, id2);
                            } catch (NumberFormatException e) {
                                return item1.getId().compareTo(item2.getId()); // Fallback to string comparison
                            }
                        });

                        itemAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {Toast.makeText(ViewListBarang.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}