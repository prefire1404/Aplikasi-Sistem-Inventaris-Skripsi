package com.example.finallesson;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    private TextInputEditText RUsername, RName, REmail, RPassword;
    private Button RRegisterButton;
    private TextView loginRedirectText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar);

        RUsername = findViewById(R.id.RUsername);
        RName = findViewById(R.id.RName);
        REmail = findViewById(R.id.REmail);
        RPassword = findViewById(R.id.RPassword);
        RRegisterButton = findViewById(R.id.RRegisterButton);
        loginRedirectText = findViewById(R.id.loginRedirectText);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        RRegisterButton.setOnClickListener(view ->
                createUser());

        loginRedirectText.setOnClickListener(view -> {
            startActivity(new Intent(Register.this, Login
                    .class));
            finish();
        });

    }

    private void createUser() {
        String username = RUsername.getText().toString().trim();
        String name = RName.getText().toString().trim();
        String email = REmail.getText().toString().trim();
        String password = RPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            REmail.setError("Email cannot be Empty");
            REmail.requestFocus();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            REmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            RPassword.setError("Password cannot be Empty");
            RPassword.requestFocus();
            return;
        }
        if (password.length() < 8) {
            RPassword.setError("Password must be at least 8 characters long");
            RPassword.requestFocus();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                String userid = firebaseUser.getUid();
                Map<String, Object> user = new HashMap<>();
                user.put("name", name);
                user.put("username", username);
                user.put("email", email);
                db.collection("Users")
                        .document(userid)
                        .set(user)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(Register.this, "User Created Successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Register.this, Login.class));
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(Register.this, " Registration Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            } else {
                Toast.makeText(Register.this, "Registration Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}