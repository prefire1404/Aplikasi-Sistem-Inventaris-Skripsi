package com.example.finallesson;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ListBarang extends AppCompatActivity {

    private CardView listBarangCard, addCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list_item);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listBarangCard = findViewById(R.id.ListBarangCard);
        addCard = findViewById(R.id.AddCard);

        listBarangCard.setOnClickListener(view -> {
            Intent listBarangIntent = new Intent(ListBarang.this, ViewListBarang.class);
            startActivity(listBarangIntent);
        });


        addCard.setOnClickListener(view -> {
            Intent addIntent = new Intent(ListBarang.this, TambahBarang.class);
            startActivity(addIntent);
        });
    }
}