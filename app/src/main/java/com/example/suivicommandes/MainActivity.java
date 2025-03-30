package com.example.suivicommandes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private Button signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        signInButton = findViewById(R.id.signInButton);
        signInButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        handleSignInWithEmailLink();
    }

    private void handleSignInWithEmailLink() {
        Intent intent = getIntent();
        String emailLink = intent.getDataString();

        if (auth.isSignInWithEmailLink(emailLink)) {
            String email = getIntent().getStringExtra("email");
            if (email == null) {
                Log.e("Firebase", "Email not found.");
                return;
            }

            auth.signInWithEmailLink(email, emailLink)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();
                            Log.d("Firebase", "Successfully signed in with email link!");
                            Toast.makeText(MainActivity.this, "Successfully signed in with email link!", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("Firebase", "Error: " + task.getException().getMessage());
                            Toast.makeText(MainActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}