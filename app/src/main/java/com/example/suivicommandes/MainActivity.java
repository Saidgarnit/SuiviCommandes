package com.example.suivicommandes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private EditText emailEditText, passwordEditText;
    private Button signInButton;
    private TextView registerTextView, forgotPasswordTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // First set the content view
            setContentView(R.layout.activity_welcome);
            Log.d(TAG, "Content view set successfully");

            // Initialize Firebase with careful error handling
            try {
                auth = FirebaseAuth.getInstance();
                db = FirebaseFirestore.getInstance();
                Log.d(TAG, "Firebase initialized successfully in MainActivity");
            } catch (Exception e) {
                Log.e(TAG, "Firebase initialization error", e);
                Toast.makeText(this, "Firebase error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            // Find and set up views with careful error handling
            try {
                // Find the views
                emailEditText = findViewById(R.id.emailEditText);
                passwordEditText = findViewById(R.id.passwordEditText);
                signInButton = findViewById(R.id.signInButton);
                registerTextView = findViewById(R.id.registerTextView);
                forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);

                // Check if any views are null
                if (emailEditText == null || passwordEditText == null ||
                        signInButton == null || registerTextView == null ||
                        forgotPasswordTextView == null) {
                    throw new NullPointerException("One or more views not found in layout");
                }

                Log.d(TAG, "Views found successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error finding views", e);
                Toast.makeText(this, "Layout error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            // Set click listeners
            signInButton.setOnClickListener(v -> {
                try {
                    signIn();
                } catch (Exception e) {
                    Log.e(TAG, "Error in sign in process", e);
                    Toast.makeText(MainActivity.this, "Sign in error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });

            registerTextView.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to register", e);
                    Toast.makeText(MainActivity.this, "Navigation error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });

            forgotPasswordTextView.setOnClickListener(v -> {
                try {
                    handleForgotPassword();
                } catch (Exception e) {
                    Log.e(TAG, "Error in forgot password", e);
                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });

            Log.d(TAG, "MainActivity setup completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "Fatal error in MainActivity", e);
            Toast.makeText(this, "Fatal error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void signIn() {
        try {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Validate inputs
            if (email.isEmpty()) {
                emailEditText.setError("Email is required");
                emailEditText.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                passwordEditText.setError("Password is required");
                passwordEditText.requestFocus();
                return;
            }

            // Show progress indicator
            signInButton.setEnabled(false);

            // Sign in with Firebase
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        signInButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            Log.d(TAG, "signIn: success");
                            FirebaseUser user = auth.getCurrentUser();
                            Toast.makeText(MainActivity.this, "Sign in successful", Toast.LENGTH_SHORT).show();

                            // Check if this is the admin account
                            if (user != null && user.getEmail() != null &&
                                    user.getEmail().equals("said@gmail.com")) {
                                Log.d(TAG, "Admin user detected, navigating to AdminActivity");
                                navigateToAdmin();
                            } else {
                                Log.d(TAG, "Regular user detected, navigating to HomeActivity");
                                navigateToHome();
                            }
                        } else {
                            Log.w(TAG, "signIn: failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed: " +
                                            (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in signIn method", e);
            signInButton.setEnabled(true);
            Toast.makeText(this, "Sign in error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkUserRoleAndNavigate(FirebaseUser user) {
        if (user == null) return;

        try {
            // Check if this is the admin account
            if ("said@gmail.com".equals(user.getEmail())) {
                navigateToAdmin();
            } else {
                navigateToHome();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking user role", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToAdmin() {
        try {
            Intent intent = new Intent(MainActivity.this, AdminActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to admin", e);
            Toast.makeText(this, "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToHome() {
        try {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to home", e);
            Toast.makeText(this, "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleForgotPassword() {
        try {
            String email = emailEditText.getText().toString().trim();

            if (email.isEmpty()) {
                emailEditText.setError("Enter your email to reset password");
                emailEditText.requestFocus();
                return;
            }

            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this,
                                    "Password reset email sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Failed to send reset email: " +
                                            (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in forgot password", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            // Check if user is already signed in
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                // Continue with navigation based on user role
                checkUserRoleAndNavigate(currentUser);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking current user", e);
        }
    }
}