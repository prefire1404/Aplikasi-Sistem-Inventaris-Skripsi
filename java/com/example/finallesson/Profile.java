package com.example.finallesson;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Profile extends AppCompatActivity {

    private TextView usernameTextView, emailTextView, nameDetailTextView, usernameDetailTextView, emailDetailTextView;
    private Button editButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Views
        usernameTextView = findViewById(R.id.ProU);
        emailTextView = findViewById(R.id.ProE);
        editButton = findViewById(R.id.editButton);
        nameDetailTextView = findViewById(R.id.ProBNT);
        usernameDetailTextView = findViewById(R.id.ProBUT);
        emailDetailTextView = findViewById(R.id.ProBET);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        fetchUserData();

        editButton.setOnClickListener(view -> {
            String userId = mAuth.getCurrentUser().getUid();
            Intent editIntent = new Intent(Profile.this, EditProfile.class);
            editIntent.putExtra("userId", userId); // Pass userId to the edit activity
            startActivity(editIntent);
        });
    }

    private void fetchUserData() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            DocumentReference userRef = db.collection("Users").document(userId);

            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Retrieve user data
                        String name = document.getString("name");
                        String username = document.getString("username");
                        String email = document.getString("email");

                        // Update UI with fetched data
                        emailTextView.setText(email);
                        usernameTextView.setText(username);
                        nameDetailTextView.setText(name);
                        usernameDetailTextView.setText(username);
                        emailDetailTextView.setText(email);
                    } else {
                        Toast.makeText(Profile.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Profile.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}