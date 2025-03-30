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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.HashMap;
import java.util.Map;

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
            Log.d(TAG, "onCreate: About to set content view");
            setContentView(R.layout.activity_welcome);
            Log.d(TAG, "onCreate: Content view set successfully");

            // Initialize Firebase Auth and Firestore
            auth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            Log.d(TAG, "onCreate: Firebase Auth and Firestore initialized");

            // Find views
            emailEditText = findViewById(R.id.emailEditText);
            passwordEditText = findViewById(R.id.passwordEditText);
            signInButton = findViewById(R.id.signInButton);
            registerTextView = findViewById(R.id.registerTextView);
            forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
            Log.d(TAG, "onCreate: Views found successfully");

            // Set up click listeners
            signInButton.setOnClickListener(v -> {
                Log.d(TAG, "Sign in button clicked");
                signIn();
            });

            registerTextView.setOnClickListener(v -> {
                Log.d(TAG, "Register text clicked");
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            });

            forgotPasswordTextView.setOnClickListener(v -> {
                Log.d(TAG, "Forgot password clicked");
                handleForgotPassword();
            });

            Log.d(TAG, "onCreate: Setup completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error starting app: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already signed in
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            checkUserRoleAndNavigate(currentUser);
        }
    }

// Inside MainActivity class, modify the signIn method and checkUserRoleAndNavigate method:

// Inside MainActivity class, modify the signIn method and checkUserRoleAndNavigate method:

    private void signIn() {
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
                        Toast.makeText(MainActivity.this, "Sign in successful",
                                Toast.LENGTH_SHORT).show();

                        // Check if this is the admin account
                        if (user != null && "said@gmail.com".equals(user.getEmail())) {
                            navigateToAdmin();
                        } else {
                            navigateToHome();
                        }
                    } else {
                        Log.w(TAG, "signIn: failure", task.getException());
                        Toast.makeText(MainActivity.this, "Authentication failed: " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // We can simplify the checkUserRoleAndNavigate method too
    private void checkUserRoleAndNavigate(FirebaseUser user) {
        if (user == null) return;

        // Check if this is the admin account
        if ("said@gmail.com".equals(user.getEmail())) {
            navigateToAdmin();
        } else {
            navigateToHome();
        }
    }

    private void navigateToAdmin() {
        Intent intent = new Intent(MainActivity.this, AdminActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToHome() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void handleForgotPassword() {
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
                                "Failed to send reset email: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}