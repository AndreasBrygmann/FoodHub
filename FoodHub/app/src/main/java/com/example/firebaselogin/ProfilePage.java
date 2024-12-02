package com.example.firebaselogin;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfilePage extends AppCompatActivity {


    FirebaseAuth auth;
    Button btnDelete, btnLogout, btnSave;
    FirebaseUser user;
    FirebaseFirestore db;
    BottomNavigationView bottomNavigationView;
    EditText edt_name, edt_location, edt_email, edt_phone;

    String var_name, var_location, var_phone;

    String userId;

    TextWatcher textWatcher;

    Spinner spinner;

    public static final String[] Languages = {"Select language", "English", "Norwegian"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        db = FirebaseFirestore.getInstance();

        spinner = findViewById(R.id.spinner);

        auth = FirebaseAuth.getInstance();
        btnSave = findViewById(R.id.profileSaveButton);
        btnDelete = findViewById(R.id.btn_delete);
        btnLogout = findViewById(R.id.btn_logout);
        user = auth.getCurrentUser();
        edt_name = findViewById(R.id.profileName);
        edt_location = findViewById(R.id.profileLocation);

        edt_email = findViewById(R.id.profileEmail);
        edt_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ProfilePage.this, "Email is not editable!", Toast.LENGTH_SHORT).show();
            }
        });
        edt_email.setFocusable(false);
        edt_email.setFocusableInTouchMode(false);
        edt_email.setCursorVisible(false);

        edt_phone = findViewById(R.id.profilePhone);
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!edt_name.getText().toString().equals(var_name) || !edt_location.getText().toString().equals(var_location) || !edt_phone.getText().toString().equals(var_phone)) {
                    btnSave.setText("Save");
                    btnSave.setBackgroundColor(getResources().getColor(R.color.red));
                    btnSave.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onUpdateProfileInformation();
                        }
                    });
                } else {
                    btnSave.setText("Saved");
                    btnSave.setBackgroundColor(getResources().getColor(R.color.green));
                    btnSave.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(ProfilePage.this, "Profile info already registered!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        };
        edt_name.addTextChangedListener(textWatcher);
        edt_location.addTextChangedListener(textWatcher);
        edt_phone.addTextChangedListener(textWatcher);

        userId = auth.getCurrentUser().getUid();

        DocumentReference documentReference = db.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                String userName = value.getString("name");
                String userLocation = value.getString("city");
                String userEmail = value.getString("email");
                String userNumber = value.getString("phoneNumber");

                var_name = userName;
                var_location = userLocation;
                var_phone = userNumber;

                edt_name.setText(userName);
                edt_location.setText(userLocation);
                edt_email.setText(userEmail);
                edt_phone.setText(userNumber);

            }
        });
        // LYTTER FOR NAVBAR
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        // RESETTER BOBBLA RUNDT ELEMENT I NAVBAR TIL NÅVÆRENDE FANE
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });
        // Button to delete the user account and signs the user out
        btnDelete.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Warning")
                    .setMessage("Are you sure you want to delete your account?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            deleteUserAccount(user);
                        } else {
                            Toast.makeText(getApplicationContext(), "Could not delete account", Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // Cancels the operation if No is selected
                        dialog.cancel();
                    })
                    .show();
        });

        // Spinner for language selection based on tutorial: https://www.youtube.com/watch?v=PSPjuXnyTfI
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Languages);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(languageAdapter);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLanguage = parent.getItemAtPosition(position).toString();
                if(selectedLanguage.equals("English")){
                    setLocal(ProfilePage.this, "en");
                    finish();
                    startActivity(getIntent());
                }
                else if(selectedLanguage.equals("Norwegian")){
                    setLocal(ProfilePage.this, "no");
                    finish();
                    startActivity(getIntent());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void setLocal(Activity activity, String langcode){
        Locale locale = new Locale(langcode);
        locale.setDefault(locale);
        Resources resources = activity.getResources();
        Configuration config = resources.getConfiguration();
        config.locale = locale;
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    public void onUpdateProfileInformation() {

        String userName = edt_name.getText().toString();
        String userLocation = edt_location.getText().toString();
        String userEmail = edt_email.getText().toString();
        String userNumber = edt_phone.getText().toString();

        Map<String, Object> userDetail = new HashMap<>();
        userDetail.put("city", userLocation);
        userDetail.put("name", userName);
        userDetail.put("phoneNumber", userNumber);

        db.collection("users").whereEqualTo("email", userEmail).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                    String documentID = documentSnapshot.getId();

                    db.collection("users").document(documentID).update(userDetail).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(ProfilePage.this, "Profile information updated!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProfilePage.this, "Failure in editing profile information!", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });

    }

    private void deleteUserAccount(FirebaseUser user) {
        user.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Getting user id to delete data in authentication and firestore
                String uid = user.getUid();
                DocumentReference userRef = db.collection("users").document(uid);

                //Deletes the userdata in firestore
                userRef.delete().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(getApplicationContext(), "Account deleted", Toast.LENGTH_SHORT).show();

                        // Signs out and the intent takes the user back to login
                        Intent intent = new Intent(getApplicationContext(), Login.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Could not delete account", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "Could not delete account", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // LYTTER FOR BOTTOM NAVIGATION
    private final BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();

                    if (itemId == R.id.nav_market) {
                        Intent marketIntent = new Intent(ProfilePage.this, MainActivity.class);
                        startActivity(marketIntent);
                        finish();
                        return true;
                    }

                    // ENDRE TIL ALERT-CLASS NÅR DEN ER LAGET:
                    else if (itemId == R.id.nav_alerts) {
                        Intent alertIntent = new Intent(ProfilePage.this, Notification.class);
                        startActivity(alertIntent);
                        finish();
                        return true;
                    }


                    else if (itemId == R.id.nav_profile) {
                        return true;
                    }
                    return false;
                }
            };

}