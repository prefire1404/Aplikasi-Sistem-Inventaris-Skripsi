package com.example.finallesson;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class ItemDetailActivity extends AppCompatActivity {

    private TextView tvItemId, tvItemName, tvItemCategory, tvItemCondition, tvItemQuantity, tvItemDateAdded;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvItemId = findViewById(R.id.tvID);
        tvItemName = findViewById(R.id.tvItemName);
        tvItemCategory = findViewById(R.id.tvItemCategory);
        tvItemCondition = findViewById(R.id.tvItemCondition);
        tvItemQuantity = findViewById(R.id.tvItemQuantity);
        tvItemDateAdded = findViewById(R.id.tvItemDateAdded);

        db = FirebaseFirestore.getInstance();

        String itemId = getIntent().getStringExtra("itemId");
        if (itemId != null) {
            fetchItemDetails(itemId);
        }
    }

    private void fetchItemDetails(String itemId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("Users").document(userId).collection("Items").document(itemId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            populateItemDetails(documentSnapshot.getData());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error fetching item details", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void populateItemDetails(Map<String, Object> itemData) {
        tvItemId.setText("ID: " + itemData.getOrDefault("itemId", ""));
        tvItemName.setText("Nama: " + itemData.getOrDefault("itemName", ""));
        tvItemCategory.setText("Kategori: " + itemData.getOrDefault("itemCategory", ""));
        tvItemCondition.setText("Kondisi: " + itemData.getOrDefault("itemCondition", ""));
        tvItemQuantity.setText("Jumlah: " + itemData.getOrDefault("itemQuantity", ""));
        tvItemDateAdded.setText("Tanggal Masuk: " + itemData.getOrDefault("dateAdded", ""));
    }
}