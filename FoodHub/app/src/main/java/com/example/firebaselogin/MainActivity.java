package com.example.firebaselogin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements RecycleViewInterface {

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore db;
    CollectionReference collectionReference;
    BottomNavigationView bottomNavigationView;

    RecyclerView recyclerView;
    MyRecycleViewAdapter myRecycleViewAdapter;
    ArrayList<Listing> list;

    Button newListingButton;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        collectionReference = db.collection("listings");

        recyclerView = findViewById(R.id.recycleView_id);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        myRecycleViewAdapter = new MyRecycleViewAdapter(this, list, this);
        recyclerView.setAdapter(myRecycleViewAdapter);

        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                myRecycleViewAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                myRecycleViewAdapter.getFilter().filter(newText);
                return false;
            }
        });

        collectionReference.get().addOnSuccessListener(queryDocumentSnapshots -> {
            list.clear(); // Clear the current list before adding new data
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                Listing listing = documentSnapshot.toObject(Listing.class);
                listing.setDocumentID(documentSnapshot.getId());
                list.add(listing);
            }
            myRecycleViewAdapter.notifyDataSetChanged(); // Notify adapter about data change
            myRecycleViewAdapter.updateListFull(new ArrayList<>(list)); // Update the listFull in the adapter
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
        bottomNavigationView.setSelectedItemId(R.id.nav_market);

        if (user == null) {
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        }
    }

    // LYTTER FOR BOTTOM NAVIGATION
    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_market) {
                return true;
            }

            // ENDRE TIL ALERT-CLASS NÅR DEN ER LAGET:
            else if (itemId == R.id.nav_alerts) {
                Intent alertIntent = new Intent(MainActivity.this, Notification.class);
                startActivity(alertIntent);
                finish();
                return true;
            }

            else if (itemId == R.id.nav_profile) {
                Intent profileIntent = new Intent(MainActivity.this, ProfilePage.class);
                startActivity(profileIntent);
                finish();
                return true;
            }
            return false;
        }
    };


    @Override
    public void onItemClicked(Listing listing) {
        Intent intent = new Intent(MainActivity.this, ProductPage.class);
        intent.putExtra("docId", listing.getDocumentID());
        startActivity(intent);
    }
}
