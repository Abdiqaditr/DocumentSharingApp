package com.example.documentsharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * LoginActivity handles user authentication using email and password.
 * Users can log in or navigate to registration via the Sign Up link.
 */
public class LoginActivity extends AppCompatActivity {

    // Firebase Authentication instance
    private FirebaseAuth mAuth;

    // UI Elements
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private MaterialButton signInButton;
    private TextView signUpText;
    private TextView forgotPasswordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        initializeUiComponents();

        // Check if user is already logged in
        checkLoggedInUser();

        // Set up button and text click listeners
        setupClickListeners();
    }

    /**
     * Initializes all UI components from the login layout.
     */
    private void initializeUiComponents() {
        emailInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        signInButton = findViewById(R.id.loginButton);
        signUpText = findViewById(R.id.signUpText);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
    }

    /**
     * Checks if a user is already logged in and navigates to MainActivity if true.
     */
    private void checkLoggedInUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    /**
     * Sets up click listeners for the Sign In button and Sign Up text.
     */
    private void setupClickListeners() {
        signInButton.setOnClickListener(v -> handleSignIn());
        signUpText.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));
        // Note: Forgot Password is included in UI but not functional yet
    }

    /**
     * Handles the sign-in process with email and password.
     */
    private void handleSignIn() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate input fields
        if (!validateInputs(email, password)) {
            return;
        }

        // Attempt to sign in with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Sign-in Successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(),
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
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}