package com.example.finallesson;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class HistoryDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout llReportDetailsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        llReportDetailsContainer = findViewById(R.id.llReportDetailsContainer);

        String documentId = getIntent().getStringExtra("documentId");
        String collectionName = getIntent().getStringExtra("collectionName");

        if (documentId != null && collectionName != null) {
            fetchDetails(documentId, collectionName);
        } else {
            Toast.makeText(this, "Document ID or Collection Name is missing", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchDetails(String documentId, String collectionName) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("Users").document(userId).collection(collectionName).document(documentId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Map<String, Object> data = documentSnapshot.getData();
                            if (data != null) {
                                Log.d("HistoryDetailActivity", "Data fetched: " + data.toString());
                                if (collectionName.equals("Items")) {
                                    // Handle Item details
                                    inflateAndPopulateItemLayout(data);
                                } else if (collectionName.equals("Reports")) {
                                    // Handle Report details
                                    String reportType = (String) data.get("reportType");
                                    inflateAndPopulateReportLayout(reportType, data);
                                }
                            } else {
                                Toast.makeText(this, "No data found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Document does not exist", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error fetching details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("HistoryDetailActivity", "Error fetching details", e);
                    });
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void inflateAndPopulateItemLayout(Map<String, Object> itemData) {
        LayoutInflater inflater = getLayoutInflater();

        if ("Penambahan Barang".equals(itemData.get("action"))) {
            View itemLayout = inflater.inflate(R.layout.item_layout_history, llReportDetailsContainer, false);
            populateItemLayout(itemLayout, itemData);
            llReportDetailsContainer.addView(itemLayout);
        } else {
            Log.d("HistoryDetailActivity", "Action is not Penambahan Barang");
        }
    }

    private void inflateAndPopulateReportLayout(String reportType, Map<String, Object> reportData) {
        LayoutInflater inflater = getLayoutInflater();

        switch (reportType) {
            case "Laporan Kehilangan Barang":
                View losingLayout = inflater.inflate(R.layout.report_losing_layout, llReportDetailsContainer, false);
                populateLosingReport(losingLayout, reportData);
                llReportDetailsContainer.addView(losingLayout);
                break;
            case "Laporan Peminjaman Barang":
                View borrowingLayout = inflater.inflate(R.layout.report_borrowing_layout, llReportDetailsContainer, false);
                populateBorrowingReport(borrowingLayout, reportData);
                llReportDetailsContainer.addView(borrowingLayout);
                break;
            case "Laporan Perubahan Barang":
                View changingLayout = inflater.inflate(R.layout.report_changing_layout, llReportDetailsContainer, false);
                populateChangingReport(changingLayout, reportData);
                llReportDetailsContainer.addView(changingLayout);
                break;
            case "Laporan Penambahan Barang":
                View addingLayout = inflater.inflate(R.layout.report_adding_layout, llReportDetailsContainer, false);
                populateAddingReport(addingLayout, reportData);
                llReportDetailsContainer.addView(addingLayout);
                break;
            default:
                Log.d("HistoryDetailActivity", "Unknown report type: " + reportType);
        }
    }

    private void populateItemLayout(View layout, Map<String, Object> itemData) {
        TextView ihId = layout.findViewById(R.id.ihID);
        TextView ihNama = layout.findViewById(R.id.ihItemName);
        TextView ihKategori = layout.findViewById(R.id.ihItemCategory);
        TextView ihKondisi = layout.findViewById(R.id.ihItemCondition);
        TextView ihJumlah = layout.findViewById(R.id.ihItemQuantity);
        TextView ihTanggalMasuk = layout.findViewById(R.id.ihItemDateAdded);

        ihId.setText("ID: " + itemData.getOrDefault("id", ""));
        ihNama.setText("Nama: " + itemData.getOrDefault("name", ""));
        ihKategori.setText("Kategori: " + itemData.getOrDefault("category", ""));
        ihKondisi.setText("Kondisi: " + itemData.getOrDefault("condition", ""));
        ihJumlah.setText("Jumlah: " + itemData.getOrDefault("quantity", ""));
        ihTanggalMasuk.setText("Tanggal Masuk: " + itemData.getOrDefault("dateAdded", ""));
    }

    private void populateLosingReport(View layout, Map<String, Object> reportData) {
        TextView rlReportType = layout.findViewById(R.id.rlReportType);
        TextView rlItemId = layout.findViewById(R.id.rlItemId);
        TextView rlItemName = layout.findViewById(R.id.rlItemName);
        TextView rlJumlahBarangHilang = layout.findViewById(R.id.rlJumlahBarangHilang);
        TextView rlTanggalKehilangan = layout.findViewById(R.id.rlTanggalKehilangan);

        rlReportType.setText("Laporan Kehilangan Barang");
        rlItemId.setText("ID Barang: " + reportData.getOrDefault("ID Barang", ""));
        rlItemName.setText("Nama Barang: " + reportData.getOrDefault("Nama Barang", ""));
        rlJumlahBarangHilang.setText("Jumlah Barang yang hilang: " + reportData.getOrDefault("Jumlah Barang yang hilang", ""));
        rlTanggalKehilangan.setText("Tanggal Kehilangan: " + reportData.getOrDefault("date", ""));
    }

    private void populateBorrowingReport(View layout, Map<String, Object> reportData) {
        TextView rbReportType = layout.findViewById(R.id.rbReportType);
        TextView rbItemId = layout.findViewById(R.id.rbItemId);
        TextView rbItemName = layout.findViewById(R.id.rbItemName);
        TextView rbJumlahBarangDipinjam = layout.findViewById(R.id.rbJumlahBarangDipinjam);
        TextView rbNamaPeminjam = layout.findViewById(R.id.rbNamaPeminjam);
        TextView rbTanggalPeminjaman = layout.findViewById(R.id.rbTanggalPeminjaman);
        TextView rbWaktuPeminjaman = layout.findViewById(R.id.rbWaktuPeminjaman);

        rbReportType.setText("Laporan Peminjaman Barang");
        rbItemId.setText("ID Barang: " + reportData.getOrDefault("ID Barang", ""));
        rbItemName.setText("Nama Barang: " + reportData.getOrDefault("Nama Barang", ""));
        rbJumlahBarangDipinjam.setText("Jumlah Barang yang dipinjam: " + reportData.getOrDefault("Jumlah Barang yang dipinjam", ""));
        rbNamaPeminjam.setText("Nama Peminjam: " + reportData.getOrDefault("Nama Peminjam", ""));
        rbTanggalPeminjaman.setText("Tanggal Peminjaman: " + reportData.getOrDefault("date", ""));
        rbWaktuPeminjaman.setText("Waktu Peminjaman: " + reportData.getOrDefault("time", ""));
    }

    private void populateChangingReport(View layout, Map<String, Object> reportData) {
        TextView rcReportType = layout.findViewById(R.id.rcReportType);
        TextView rcItemId = layout.findViewById(R.id.rcItemId);
        TextView rcItemName = layout.findViewById(R.id.rcItemName);
        TextView rcJumlahBarangDiubah = layout.findViewById(R.id.rcJumlahBarangDiubah);
        TextView rcAlasanPerubahan = layout.findViewById(R.id.rcAlasanPerubahan);

        rcReportType.setText("Laporan Perubahan Barang");
        rcItemId.setText("ID Barang: " + reportData.getOrDefault("ID Barang", ""));
        rcItemName.setText("Nama Barang: " + reportData.getOrDefault("Nama Barang", ""));
        rcJumlahBarangDiubah.setText("Jumlah Barang yang diubah: " + reportData.getOrDefault("Jumlah Barang yang diubah", ""));
        rcAlasanPerubahan.setText("Alasan Perubahan: " + reportData.getOrDefault("Alasan Perubahan", ""));
    }

    private void populateAddingReport(View layout, Map<String, Object> reportData) {
        TextView raReportType = layout.findViewById(R.id.raReportType);
        TextView raItemId = layout.findViewById(R.id.raItemId);
        TextView raItemName = layout.findViewById(R.id.raItemName);
        TextView raJumlah = layout.findViewById(R.id.raJumlah);
        TextView raTanggalPenambahan = layout.findViewById(R.id.raTanggalPenambahan);

        raReportType.setText("Laporan Penambahan Barang");
        raItemId.setText("ID Barang: " + reportData.getOrDefault("ID Barang", ""));
        raItemName.setText("Nama Barang: " + reportData.getOrDefault("Nama Barang", ""));
        raJumlah.setText("Jumlah: " + reportData.getOrDefault("Jumlah", ""));
        raTanggalPenambahan.setText("Tanggal Penambahan: " + reportData.getOrDefault("date", ""));
    }
}