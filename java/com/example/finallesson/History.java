package com.example.finallesson;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class History extends AppCompatActivity {

    Spinner spinnerCategory;
    RecyclerView rvHistory;
    private Map<String, List<HistoryItem>> historyData = new HashMap<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);
        setupWindowInsets();

        spinnerCategory = findViewById(R.id.spinnerCategory);
        rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        setupSpinner();
        fetchHistoryData();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupSpinner() {
        String[] categories = {
                "Semua", "Penambahan Barang",
                "Laporan Kehilangan Barang", "Laporan Peminjaman Barang",
                "Laporan Perubahan Barang", "Laporan Penambahan Barang"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = categories[position];
                filterHistory(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void fetchHistoryData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            historyData.clear();

            fetchItems(userId);
            fetchReports(userId);
        }
    }

    private void fetchItems(String userId) {db.collection("Users").document(userId).collection("Items").get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<HistoryItem> items = new ArrayList<>();
                int totalItems = queryDocumentSnapshots.size();
                if (totalItems == 0) {
                    // Handle the case where there are no items
                    historyData.put("Penambahan Barang", items);
                    updateRecyclerView("Semua");
                    return;
                }

                final int[] fetchedItems = {0}; // Use an array to modify the value within the listener

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    String action = "Penambahan Barang";
                    String date = document.getString("dateAdded");
                    String itemId = document.getId();

                    String id = document.getString("id");
                    String name = document.getString("name");
                    String category = document.getString("category");
                    String condition = document.getString("condition");
                    String quantity = document.getString("quantity");
                    String dateAdded = document.getString("dateAdded");

                    items.add(new HistoryItem(action, date, itemId, "Items", id, name, category, condition, quantity, dateAdded));
                    fetchedItems[0]++;

                    if (fetchedItems[0] == totalItems) {
                        historyData.put("Penambahan Barang", items);
                        updateRecyclerView("Semua");
                    }
                }
            });
    }

    private void fetchReports(String userId) {
        db.collection("Users").document(userId).collection("Reports").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String reportType = document.getString("reportType");
                        String date = document.getString("date");
                        if (reportType != null) {
                            List<HistoryItem> reports = historyData.getOrDefault(reportType, new ArrayList<>());
                            reports.add(new HistoryItem(reportType, date, document.getId(), "Reports"));
                            historyData.put(reportType, reports);
                        }
                    }
                    updateRecyclerView("Semua");
                });
    }

    private void filterHistory(String category) {
        updateRecyclerView(category);
    }

    private void updateRecyclerView(String category) {
        List<HistoryItem> filteredList = new ArrayList<>();
        if (category.equals("Semua")) {
            for (List<HistoryItem> list : historyData.values()) {
                filteredList.addAll(list);
            }
        } else {
            List<HistoryItem> categoryList = historyData.get(category);
            if (categoryList != null) {
                filteredList.addAll(categoryList);
            }
        }
        HistoryAdapter historyAdapter = new HistoryAdapter(filteredList, this::openHistoryDetail);
        rvHistory.setAdapter(historyAdapter);
    }

    private void openHistoryDetail(HistoryItem item) {
        Intent intent = new Intent(this, HistoryDetailActivity.class);
        intent.putExtra("documentId", item.getDocumentId());
        intent.putExtra("collectionName", item.getCollectionName());
        startActivity(intent);
    }
}