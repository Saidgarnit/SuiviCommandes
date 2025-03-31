package com.example.suivicommandes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private TextView userEmailTextView;
    private Button logoutButton;
    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private List<Item> itemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        Button ordersButton = findViewById(R.id.ordersButton);
        ordersButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, OrdersActivity.class);
            startActivity(intent);
        });

        // Check if user is authenticated
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            // Not logged in, redirect to login
            redirectToLogin();
            return;
        }

        // Find views
        userEmailTextView = findViewById(R.id.userEmailTextView);
        logoutButton = findViewById(R.id.logoutButton);
        recyclerView = findViewById(R.id.recyclerView);

        // Display user email
        userEmailTextView.setText("Signed in as: " + currentUser.getEmail());

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new ItemAdapter(this, itemList);
        recyclerView.setAdapter(itemAdapter);

        // Load items from Firestore
        fetchItems();

        // Set up logout button
        logoutButton.setOnClickListener(v -> signOut());
    }

    private void fetchItems() {
        db.collection("items").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    itemList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Item item = document.toObject(Item.class);
                        itemList.add(item);
                    }
                    itemAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(HomeActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void signOut() {
        auth.signOut();
        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


}
