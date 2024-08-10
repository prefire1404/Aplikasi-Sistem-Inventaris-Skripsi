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
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;

public class NFC extends AppCompatActivity {

    TextView etNfcData;
    Button btnAddNfc, btnDeleteNfc;
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter[] readTagFilters;
    boolean isDeleteMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nfcactivity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etNfcData = findViewById(R.id.etNfcData);
        btnAddNfc = findViewById(R.id.btnAddNfc);
        btnDeleteNfc = findViewById(R.id.btnDeleteNfc);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not available on this device", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        readTagFilters = new IntentFilter[]{tagDetected};

        btnAddNfc.setOnClickListener(view -> {
            Intent intent = new Intent(NFC.this, TambahNFC.class);
            startActivity(intent);
        });

        btnDeleteNfc.setOnClickListener(view -> {
            isDeleteMode = !isDeleteMode;
            if (isDeleteMode) {
                enableDeleteMode();
            } else {
                disableDeleteMode();
                etNfcData.setText("");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled()) {
                showWirelessSettings();
            }
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, readTagFilters, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (isDeleteMode) {
                overwriteNfcData(detectedTag);
                isDeleteMode = false;
                disableDeleteMode();
                etNfcData.setText("");
            } else {
                readFromTag(detectedTag);
            }
        }
    }

    private void readFromTag(Tag tag) {
        Ndef ndef = Ndef.get(tag);
        if (ndef != null) {
            try {
                ndef.connect();
                NdefMessage ndefMessage = ndef.getNdefMessage();
                if (ndefMessage != null) {
                    String message = new String(ndefMessage.getRecords()[0].getPayload());
                    etNfcData.setText(message);
                } else {
                    Toast.makeText(this, "No NDEF message found", Toast.LENGTH_SHORT).show();
                }
                ndef.close();
            } catch (Exception e) {
                Toast.makeText(this, "Error reading NFC tag", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "NDEF is not supported on this tag", Toast.LENGTH_SHORT).show();
        }
    }

    private void showWirelessSettings() {
        Toast.makeText(this, "You need to enable NFC", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        startActivity(intent);
    }

    private void enableDeleteMode() {
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, readTagFilters, null);
        Toast.makeText(this, "Dekatkan NFC Tag untuk menghapus Data", Toast.LENGTH_SHORT).show();
    }

    private void disableDeleteMode() {
        nfcAdapter.disableForegroundDispatch(this);
    }

    private void overwriteNfcData(Tag tag) {
        Ndef ndef = Ndef.get(tag);
        if (ndef != null) {
            try {
                ndef.connect();
                NdefRecord[] records = {NdefRecord.createTextRecord(null, "")}; // Empty records to overwrite
                NdefMessage message = new NdefMessage(records);
                ndef.writeNdefMessage(message);
                ndef.close();
                Toast.makeText(this, "NFC tag data overwritten", Toast.LENGTH_SHORT).show();
            } catch (IOException | FormatException e) {
                Toast.makeText(this, "Error overwriting NFC tag data", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "NDEF is not supported on this tag", Toast.LENGTH_SHORT).show();
        }
    }
}