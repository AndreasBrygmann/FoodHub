package com.example.firebaselogin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Register extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword, editTextConfirmPass, editTextName, editTextCity,
            editTextPhoneNumber;
    Button btnReg;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView switchToLogin;
    private FirebaseFirestore db;


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editTextName = findViewById(R.id.tv_name);
        editTextEmail = findViewById(R.id.tv_email);
        editTextPassword = findViewById(R.id.tv_password);
        editTextConfirmPass = findViewById(R.id.tv_password_repeat);
        editTextCity = findViewById(R.id.tv_city);
        editTextPhoneNumber = findViewById(R.id.tv_phonenumber);
        btnReg = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progress_bar);
        switchToLogin = findViewById(R.id.btn_back);

        switchToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        btnReg.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String email, password, name, repeatPassword, city, phoneNumber;
            email = Objects.requireNonNull(editTextEmail.getText()).toString();
            password = Objects.requireNonNull(editTextPassword.getText()).toString();
            name = Objects.requireNonNull(editTextName.getText()).toString();
            repeatPassword = Objects.requireNonNull(editTextConfirmPass.getText()).toString();
            city = Objects.requireNonNull(editTextCity.getText()).toString();
            phoneNumber = Objects.requireNonNull(editTextPhoneNumber.getText()).toString();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(name)
                    || TextUtils.isEmpty(repeatPassword) || TextUtils.isEmpty(city)
                    || TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(Register.this, "Fill in all fields", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            if (!password.equals(repeatPassword)) {
                Toast.makeText(Register.this, "Passwords do not match", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Account created with UID
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String userId = user.getUid();

                                // Opprett en referanse til Firestore med brukerens UID som dokument-ID
                                DocumentReference userRef = db.collection("users").document(userId);

                                // Creating object for storage in hashmap
                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("name", name);
                                userMap.put("email", email);
                                userMap.put("city", city);
                                userMap.put("phoneNumber", phoneNumber);

                                // Save the extra data in FireStore database
                                userRef.set(userMap)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(Register.this, "Account created.", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {


                                                Toast.makeText(Register.this, "Account creation failed.",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(Register.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

    }
}