package com.example.documentsharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

/**
 * SignUpActivity allows users to register with email, password, and phone.
 * After successful registration, the user's phone number and basic profile data are saved to Firebase.
 * Users are then navigated back to LoginActivity.
 */
public class SignUpActivity extends AppCompatActivity {

    // Firebase Authentication and Database instances
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    // UI Components
    private TextInputEditText fullNameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText phoneInput;
    private MaterialButton signUpButton;
    private TextView signInText;
    private MaterialCardView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        // Initialize Firebase Authentication and Database
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("users");

        // Initialize UI components
        initializeUiComponents();

        // Set up click listeners
        setupClickListeners();
    }

    /**
     * Initializes all UI components from the signup layout.
     */
    private void initializeUiComponents() {
        fullNameInput = findViewById(R.id.fullNameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        phoneInput = findViewById(R.id.phoneInput);
        signUpButton = findViewById(R.id.signUpButton);
        signInText = findViewById(R.id.signInText);
        backButton = findViewById(R.id.backButton);
    }

    /**
     * Sets up click listeners for the buttons and text views.
     */
    private void setupClickListeners() {
        signUpButton.setOnClickListener(v -> handleSignUp());
        backButton.setOnClickListener(v -> finish());
        signInText.setOnClickListener(v -> finish());
    }

    /**
     * Handles the sign-up process with email and password.
     */
    private void handleSignUp() {
        String fullName = fullNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(fullName, email, password, phone)) {
            return;
        }

        // Register user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registration successful, save user profile to database
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserProfile(user, fullName, email, phone);
                        }

                        Toast.makeText(SignUpActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();

                        // Navigate to HomeActivity
                        startActivity(new Intent(SignUpActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(SignUpActivity.this, "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Saves the user's profile data to Firebase Realtime Database.
     * @param user The FirebaseUser object of the newly registered user.
     * @param fullName The user's full name.
     * @param email The user's email.
     * @param phone The user's phone number.
     */
    private void saveUserProfile(FirebaseUser user, String fullName, String email, String phone) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("fullName", fullName);
        profile.put("email", email);
        profile.put("phone", phone);

        dbRef.child(user.getUid()).setValue(profile)
                .addOnSuccessListener(aVoid -> {
                    // Profile saved successfully
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Validates the input fields for registration.
     * @param fullName The full name entered by the user.
     * @param email The email entered by the user.
     * @param password The password entered by the user.
     * @param phone The phone number entered by the user.
     * @return True if all inputs are valid, false otherwise.
     */
    private boolean validateInputs(String fullName, String email, String password, String phone) {
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}