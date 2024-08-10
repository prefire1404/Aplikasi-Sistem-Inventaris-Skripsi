package com.example.finallesson;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private ActionBarDrawerToggle toggle;
    private CardView profileCard, listCard, nfcCard, ocrCard, reportCard, historyCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Views
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        profileCard = findViewById(R.id.ProfileCard);
        listCard = findViewById(R.id.ListCard);
        nfcCard = findViewById(R.id.NFCCard);
        ocrCard = findViewById(R.id.OCRCard);
        reportCard = findViewById(R.id.ReportCard);
        historyCard = findViewById(R.id.HistoryCard);

        // Set up Toolbar
        setSupportActionBar(findViewById(R.id.toolbar));

        // Set up Navigation Drawer toggle
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.NOpen, R.string.NClose);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Handle Navigation Item Clicks
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {// Handle menu item clicks here
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    // Handle home item click
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(MainActivity.this, Profile.class));
                } else if (itemId == R.id.nav_list) {
                    startActivity(new Intent(MainActivity.this, ListBarang.class));
                } else if (itemId == R.id.nav_scan_nfc) {
                    startActivity(new Intent(MainActivity.this, NFC.class));
                } else if (itemId == R.id.nav_scan_ocr) {
                    startActivity(new Intent(MainActivity.this, OCR.class));
                } else if (itemId == R.id.nav_report) {
                    startActivity(new Intent(MainActivity.this, Report.class));
                } else if (itemId == R.id.nav_logout) {
                    startActivity(new Intent(MainActivity.this, Login.class));
                    finish();
                }

                drawerLayout.closeDrawers(); // Close the drawer after handling the click
                return true;
            }
        });

        // Handle Card Clicks
        profileCard.setOnClickListener(view -> {
            Intent p = new Intent(MainActivity.this, Profile.class);
            startActivity(p);
        });

        listCard.setOnClickListener(view -> {
            Intent l = new Intent(MainActivity.this, ListBarang.class);
            startActivity(l);
        });

        nfcCard.setOnClickListener(view -> {
            Intent sn = new Intent(MainActivity.this, NFC.class);
            startActivity(sn);
        });

        ocrCard.setOnClickListener(view -> {
            Intent so = new Intent(MainActivity.this, OCR.class);
            startActivity(so);
        });

        reportCard.setOnClickListener(view -> {
            Intent r = new Intent(MainActivity.this, Report.class);
            startActivity(r);
        });

        historyCard.setOnClickListener(view -> {
            Intent h = new Intent(MainActivity.this, History.class);
            startActivity(h);
        });
    }

    // Handle the Up button in the Toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}