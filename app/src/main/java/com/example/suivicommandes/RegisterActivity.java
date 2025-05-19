package com.example.suivicommandes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuthUserCollisionException; // Added import
import com.google.firebase.auth.FirebaseAuthWeakPasswordException; // Added import
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private EditText emailEditText, passwordEditText;
    private Button registerButton;
    private TextView signInTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);
        signInTextView = findViewById(R.id.signInTextView);

        registerButton.setOnClickListener(v -> registerUser());

        // Sign in text link
        signInTextView.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close register activity
        });
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate inputs
        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return;
        }

        // Regex: at least 8 characters, one uppercase, one lowercase, one digit
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";

        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return;
        } else if (password.length() < 8) {
            passwordEditText.setError("Password must be at least 8 characters");
            passwordEditText.requestFocus();
            return;
        } else if (!password.matches(passwordPattern)) {
            passwordEditText.setError("Password must include uppercase, lowercase, and a number.");
            passwordEditText.requestFocus();
            return;
        }

        // Disable button during registration
        registerButton.setEnabled(false);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    // Re-enable button
                    registerButton.setEnabled(true);

                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        Log.d(TAG, "Registration successful!");

                        if (user != null) {
                            // Save user data to Firestore
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("email", email);
                            userData.put("role", "user");
                            userData.put("createdAt", System.currentTimeMillis());

                            db.collection("users").document(user.getUid())
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User role saved to Firestore"))
                                    .addOnFailureListener(e -> Log.e(TAG, "Error saving user role", e));

                            // Send email verification
                            user.sendEmailVerification()
                                    .addOnCompleteListener(verificationTask -> {
                                        if (verificationTask.isSuccessful()) {
                                            Toast.makeText(RegisterActivity.this,
                                                    "Registration successful. Verification email sent. Please verify before logging in.",
                                                    Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(RegisterActivity.this,
                                                    "Failed to send verification email.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });

                            // Force sign out to prevent automatic login
                            auth.signOut();
                        }

                        // Navigate to login screen
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();

                    } else {
                        Exception exception = task.getException();
                        Log.e(TAG, "Registration failed", exception);
                        String errorMessage;
                        if (exception instanceof FirebaseAuthUserCollisionException) {
                            errorMessage = "This email address is already in use.";
                        } else if (exception instanceof FirebaseAuthWeakPasswordException) {
                            errorMessage = "The password is too weak. Please choose a stronger password.";
                        } else {
                            errorMessage = "Registration failed. Please try again later.";
                        }
                        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

}