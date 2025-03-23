package com.example.documentsharingapp;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import de.hdodenhof.circleimageview.CircleImageView;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * EditProfileActivity allows users to edit their profile information
 * such as full name, phone number, and bio.
 */
public class EditProfileActivity extends AppCompatActivity {

    // Firebase instances
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private FirebaseUser currentUser;

    // UI Components
    private CircleImageView profileImage;
    private TextInputEditText fullNameInput;
    private TextInputEditText emailInput;
    private TextInputEditText phoneInput;
    private TextInputEditText bioInput;
    private ImageView backButton;
    private TextView saveButton;
    private View changePhotoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("users");
        currentUser = mAuth.getCurrentUser();

        // Initialize UI components
        initializeUiComponents();

        // Load user data
        loadUserData();

        // Set up click listeners
        setupClickListeners();
    }

    /**
     * Initializes all UI components from the edit profile layout.
     */
    private void initializeUiComponents() {
        profileImage = findViewById(R.id.profileImage);
        fullNameInput = findViewById(R.id.fullNameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        bioInput = findViewById(R.id.bioInput);
        backButton = findViewById(R.id.backButton);
        saveButton = findViewById(R.id.saveButton);
        changePhotoButton = findViewById(R.id.changePhotoButton);
    }

    /**
     * Loads user data from Firebase and populates the UI fields.
     */
    private void loadUserData() {
        if (currentUser != null) {
            // Set email (not editable)
            if (currentUser.getEmail() != null) {
                emailInput.setText(currentUser.getEmail());
            }

            // Load other user data from database
            dbRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Set full name
                        String fullName = snapshot.child("fullName").getValue(String.class);
                        if (fullName != null && !fullName.isEmpty()) {
                            fullNameInput.setText(fullName);
                        }

                        // Set phone number
                        String phone = snapshot.child("phone").getValue(String.class);
                        if (phone != null && !phone.isEmpty()) {
                            phoneInput.setText(phone);
                        }

                        // Set bio if exists
                        String bio = snapshot.child("bio").getValue(String.class);
                        if (bio != null && !bio.isEmpty()) {
                            bioInput.setText(bio);
                        }

                        // Set profile image
                        String localPath = snapshot.child("profilePicture").getValue(String.class);
                        if (localPath != null) {
                            File imageFile = new File(localPath);
                            if (imageFile.exists()) {
                                profileImage.setImageURI(Uri.fromFile(imageFile));
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(EditProfileActivity.this, "Error loading profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Sets up click listeners for buttons and interactive elements.
     */
    private void setupClickListeners() {
        // Back button
        backButton.setOnClickListener(v -> finish());

        // Save button
        saveButton.setOnClickListener(v -> saveProfile());

        // Change photo button
        changePhotoButton.setOnClickListener(v -> {
            // Launch image picker
            if (getParent() instanceof HomeActivity) {
                ((HomeActivity) getParent()).pickImage();
            } else {
                // If not launched from HomeActivity, create a new intent
                // This is a simplified approach - in a real app, you'd implement ActivityResultLauncher here
                Toast.makeText(this, "Change photo functionality requires HomeActivity", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Saves the updated profile information to Firebase.
     */
    private void saveProfile() {
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get input values
        String fullName = fullNameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String bio = bioInput.getText().toString().trim();

        // Validate inputs
        if (fullName.isEmpty()) {
            fullNameInput.setError("Name cannot be empty");
            return;
        }

        // Create profile data map
        Map<String, Object> profileUpdates = new HashMap<>();
        profileUpdates.put("fullName", fullName);
        profileUpdates.put("phone", phone);
        profileUpdates.put("bio", bio);

        // Update profile in Firebase
        dbRef.child(currentUser.getUid()).updateChildren(profileUpdates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Close activity and return to profile
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditProfileActivity.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}