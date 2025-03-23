package com.example.documentsharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * MainActivity handles user authentication using email and password.
 * Users can log in or register, then proceed to HomeActivity.
 */
public class MainActivity extends AppCompatActivity {

    // Firebase Authentication instance
    private FirebaseAuth mAuth;

    // UI elements
    private TextInputEditText usernameInput;
    private TextInputEditText passwordInput;
    private MaterialButton loginButton;
    private TextView signUpText;
    private TextView forgotPasswordText;
    private CheckBox rememberMeCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login); // Ensure this references the correct layout file

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        initializeUiElements();

        // Check if user is already logged in
        checkUserLoggedIn();

        // Set up click listeners
        setupClickListeners();
    }

    /**
     * Initializes all UI elements from the login layout.
     */
    private void initializeUiElements() {
        usernameInput = findViewById(R.id.usernameInput); // Fixed incorrect ID reference
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        signUpText = findViewById(R.id.signUpText);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);
    }

    /**
     * Checks if a user is already logged in and navigates to HomeActivity if true.
     */
    private void checkUserLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }

        // Check if "Remember Me" was checked previously
        boolean rememberMe = getSharedPreferences("login_prefs", MODE_PRIVATE)
                .getBoolean("remember_me", false);

        if (rememberMe) {
            String savedEmail = getSharedPreferences("login_prefs", MODE_PRIVATE)
                    .getString("email", "");

            if (!savedEmail.isEmpty() && usernameInput != null) {
                usernameInput.setText(savedEmail);
                rememberMeCheckbox.setChecked(true);
            }
        }
    }

    /**
     * Sets up click listeners for the Sign In button and Sign Up text.
     */
    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> signInWithEmail());

        signUpText.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
        });

        forgotPasswordText.setOnClickListener(v -> {
            Toast.makeText(this, "Forgot password functionality coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Handles email/password login using Firebase Authentication.
     */
    private void signInWithEmail() {
        String email = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (!validateInputs(email, password)) {
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        if (rememberMeCheckbox.isChecked()) {
                            getSharedPreferences("login_prefs", MODE_PRIVATE)
                                    .edit()
                                    .putBoolean("remember_me", true)
                                    .putString("email", email)
                                    .apply();
                        } else {
                            getSharedPreferences("login_prefs", MODE_PRIVATE)
                                    .edit()
                                    .clear()
                                    .apply();
                        }

                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Validates email and password inputs.
     * @param email The email entered by the user.
     * @param password The password entered by the user.
     * @return True if inputs are valid, false otherwise.
     */
    private boolean validateInputs(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
