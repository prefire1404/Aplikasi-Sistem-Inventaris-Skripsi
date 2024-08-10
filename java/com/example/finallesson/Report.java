package com.example.finallesson;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Report extends AppCompatActivity {

    private Spinner spinnerReportType;
    private LinearLayout llReportFields;
    private Button btnSubmitReport;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spinnerReportType = findViewById(R.id.spinnerReportType);
        llReportFields = findViewById(R.id.llReportFields);
        btnSubmitReport = findViewById(R.id.btnSubmitReport);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupSpinner();
        setupSubmitButtonListener();
    }

    private void setupSpinner() {
        String[] reportTypes = {"Pilih Tipe Laporan", "Laporan Kehilangan Barang", "Laporan Peminjaman Barang", "Laporan Perubahan Barang","Laporan Penambahan Barang"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, reportTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReportType.setAdapter(adapter);

        spinnerReportType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    populateReportFields(reportTypes[position]);
                } else {
                    llReportFields.removeAllViews();
                }
            }

            private void populateReportFields(String reportType) {
                llReportFields.removeAllViews();

                switch (reportType) {
                    case "Laporan Kehilangan Barang":
                        addLosingItemFields();
                        break;
                    case "Laporan Peminjaman Barang":
                        addBorrowingItemFields();
                        break;
                    case "Laporan Perubahan Barang":
                        addChangingItemFields();
                        break;
                    case "Laporan Penambahan Barang":
                        addAddingItemFields();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void addLosingItemFields() {
        addEditText("ID Barang");
        addEditText("Nama Barang");
        addEditText("Jumlah Barang yang hilang");
        addTextView("Tanggal Kehilangan:");
        llReportFields.addView(new DatePicker(this));
    }

    private void addBorrowingItemFields() {
        addEditText("ID Barang");
        addEditText("Nama Barang");
        addEditText("Jumlah Barang yang dipinjam");
        addEditText("Nama Peminjam");
        addTextView("Tanggal Peminjaman:");
        llReportFields.addView(new DatePicker(this));
        addTextView("Waktu Peminjaman:");
        llReportFields.addView(new TimePicker(this));
    }

    private void addChangingItemFields() {
        addEditText("ID Barang");
        addEditText("Nama Barang");
        addEditText("Jumlah Barang yang diubah");
        addEditText("Alasan Perubahan");
    }

    private void addAddingItemFields() {
        addEditText("ID Barang");
        addEditText("Nama Barang");
        addEditText("Jumlah");
        addTextView("Tanggal Penambahan:");
        llReportFields.addView(new DatePicker(this));
    }

    private void addEditText(String hint) {
        EditText editText = new EditText(this);editText.setHint(hint);
        llReportFields.addView(editText);
    }

    private void addTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        llReportFields.addView(textView);
    }

    private void setupSubmitButtonListener() {
        btnSubmitReport.setOnClickListener(view -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();

                Map<String, Object> reportData = new HashMap<>();
                String reportType = spinnerReportType.getSelectedItem().toString();
                reportData.put("reportType", reportType);

                extractReportData(reportData);

                db.collection("Users").document(userId).collection("Reports")
                        .add(reportData)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(Report.this, "Laporan terkirim", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(Report.this, "Failed to submit: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(Report.this, "Please log in to submit a report", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void extractReportData(Map<String, Object> reportData) {
        for (int i = 0; i < llReportFields.getChildCount(); i++) {
            View child = llReportFields.getChildAt(i);
            if (child instanceof EditText) {
                EditText editText = (EditText) child;
                reportData.put(editText.getHint().toString(), editText.getText().toString());
            } else if (child instanceof DatePicker) {
                DatePicker datePicker = (DatePicker) child;
                int year = datePicker.getYear();
                int month = datePicker.getMonth();
                int day = datePicker.getDayOfMonth();
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String dateValue = dateFormat.format(calendar.getTime());
                reportData.put("date", dateValue);
            } else if (child instanceof TimePicker) {
                TimePicker timePicker = (TimePicker) child;
                int hour = timePicker.getCurrentHour();
                int minute = timePicker.getCurrentMinute();
                String timeValue = String.format("%02d:%02d", hour, minute);
                reportData.put("time", timeValue);
            }
        }
    }
}