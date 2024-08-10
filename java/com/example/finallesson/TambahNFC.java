package com.example.finallesson;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class TambahNFC extends AppCompatActivity {

    TextInputEditText tnId, tnNama, tnQuantity;
    Spinner tnCategory, tnCondition, spGambarAset;
    Button btnAddDataToNfc;
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter[] writeTagFilters;
    String dataToWrite;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private long nfcStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tambah_nfc);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tnId = findViewById(R.id.tnId);
        tnNama = findViewById(R.id.tnNama);
        tnQuantity = findViewById(R.id.tnQuantity);
        tnCategory = findViewById(R.id.tnCategory);
        tnCondition = findViewById(R.id.tnCondition);
        btnAddDataToNfc = findViewById(R.id.btnAddDataToNfc);
        spGambarAset = findViewById(R.id.spGambarAset);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setupSpinners();
        setupImageSpinner();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "Device anda tidak mendukung NFC", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnAddDataToNfc.setOnClickListener(view -> {
            String id = tnId.getText().toString();
            String nama = tnNama.getText().toString();
            String category = tnCategory.getSelectedItem().toString();
            String condition = tnCondition.getSelectedItem().toString();
            String quantity = tnQuantity.getText().toString();
            String shortenedUrl = spGambarAset.getSelectedItem().toString();

            StringBuilder dataBuilder = new StringBuilder();
            dataBuilder.append("ID: ").append(id).append("\n");
            dataBuilder.append("Nama: ").append(nama).append("\n");
            dataBuilder.append("Kategori: ").append(category).append("\n");
            dataBuilder.append("Kondisi: ").append(condition).append("\n");
            dataBuilder.append("Jumlah: ").append(quantity).append("\n");
            dataBuilder.append("URL:  ").append(shortenedUrl);

            dataToWrite = dataBuilder.toString();

            enableWriteMode();
            Toast.makeText(TambahNFC.this, "Data Siap di kirim, Silahkan untuk dekatkan Device dengan NFC", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.kategori_array,
                android.R.layout.simple_spinner_item
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tnCategory.setAdapter(categoryAdapter);

        ArrayAdapter<CharSequence> conditionAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.kondisi_array,
                android.R.layout.simple_spinner_item
        );
        conditionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tnCondition.setAdapter(conditionAdapter);
    }

    private void setupImageSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.shortened_urls,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGambarAset.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            enableWriteMode();
            nfcStartTime = System.currentTimeMillis();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            disableWriteMode();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (dataToWrite != null) {
                try {
                    write(dataToWrite, detectedTag);
                    long nfcEndTime = System.currentTimeMillis();
                    long connectionTime = nfcEndTime - nfcStartTime;
                    storeNfcSpeed(connectionTime);
                    Toast.makeText(this, "Data tersimpan di Tag", Toast.LENGTH_SHORT).show();
                } catch (IOException | FormatException e) {
                    Toast.makeText(this, "Error writing NFC tag", Toast.LENGTH_SHORT).show();
                } finally {
                    dataToWrite = null;
                    disableWriteMode();
                }
            }
        }
    }

    private void enableWriteMode() {
        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        writeTagFilters = new IntentFilter[]{tagDetected};
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }

    private void disableWriteMode() {
        nfcAdapter.disableForegroundDispatch(this);
    }

    private void write(String text, Tag tag) throws IOException, FormatException {
        NdefRecord[] records = {createRecord(text)};
        NdefMessage message = new NdefMessage(records);
        Ndef ndef = Ndef.get(tag);
        ndef.connect();
        ndef.writeNdefMessage(message);
        ndef.close();
    }

    private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
        byte[] textBytes = text.getBytes(Charset.forName("UTF-8"));
        byte[] payload = new byte[1 + textBytes.length];
        payload[0] = (byte) textBytes.length;
        System.arraycopy(textBytes, 0, payload, 1, textBytes.length);
        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
    }

    private void storeNfcSpeed(long connectionTime) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("nfc_tag_transfer_speed", connectionTime);
            db.collection("Users").document(user.getUid()).collection("NFC")
                    .add(data)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Transfer Speed Stored", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error storing NFC speed", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}