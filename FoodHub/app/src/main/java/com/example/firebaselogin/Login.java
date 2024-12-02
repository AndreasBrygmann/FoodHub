package com.example.firebaselogin;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class Login extends AppCompatActivity {
    TextInputEditText editTextEmail, editTextPassword;
    Button btnLogin;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView switchToRegister, txtSignUp;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.tv_email);
        editTextPassword = findViewById(R.id.tv_password);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);

        // Create a string with a span, clicking on that span will start an intent
        SpannableString string = new SpannableString("Don't have an account yet? Sign up!");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
                finish();
            }
        };

        // Making the span clickable by setting the span, finding the text and setting the text
        string.setSpan(clickableSpan, 27, 35, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        TextView textView =findViewById(R.id.txt_register);
        textView.setText(string);
        textView.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());

        btnLogin.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String email, password;
            email = Objects.requireNonNull(editTextEmail.getText()).toString();
            password = Objects.requireNonNull(editTextPassword.getText()).toString();

            if(TextUtils.isEmpty(email)){
                Toast.makeText(Login.this, "Enter email", Toast.LENGTH_LONG).show();
                return;
            }

            if(TextUtils.isEmpty(password)){
                Toast.makeText(Login.this, "Enter password", Toast.LENGTH_LONG).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(Login.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

        });

    }
}