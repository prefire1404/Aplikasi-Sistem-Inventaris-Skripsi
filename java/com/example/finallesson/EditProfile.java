package com.example.finallesson;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfile extends AppCompatActivity {

    private TextInputEditText nameEditText, usernameEditText, emailEditText;
    private Button editButton, cancelButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Views
        nameEditText = findViewById(R.id.EPETName);
        usernameEditText = findViewById(R.id.EPETUsername);
        emailEditText = findViewById(R.id.EPETEmail);
        editButton = findViewById(R.id.editButton);
        cancelButton = findViewById(R.id.cancelButton);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        userId = mAuth.getCurrentUser().getUid();

        // Fetch existing user data and populate EditTexts
        fetchUserData();

        // Handle Edit Button Click
        editButton.setOnClickListener(view -> {
            String newName = nameEditText.getText().toString();
            String newUsername = usernameEditText.getText().toString();
            String newEmail = emailEditText.getText().toString();

            // Update user data in Firestore
            updateUserInFirestore(newName, newUsername, newEmail);
        });

        // Handle Cancel Button Click
        cancelButton.setOnClickListener(view -> finish());
    }

    private void fetchUserData() {
        DocumentReference userRef = db.collection("Users").document(userId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String name = document.getString("name");
                    String username = document.getString("username");
                    String email = document.getString("email");

                    nameEditText.setText(name);
                    usernameEditText.setText(username);
                    emailEditText.setText(email);
                }
            } else {
                Toast.makeText(EditProfile.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserInFirestore(String newName, String newUsername, String newEmail) {
        DocumentReference userRef = db.collection("Users").document(userId);
        userRef.update(
                "name", newName,
                "username", newUsername,
                "email", newEmail
        ).addOnSuccessListener(aVoid -> {
            Toast.makeText(EditProfile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            finish(); // Return to Profile activity
        }).addOnFailureListener(e -> {
            Toast.makeText(EditProfile.this, "Error updating profile", Toast.LENGTH_SHORT).show();
        });
    }
}