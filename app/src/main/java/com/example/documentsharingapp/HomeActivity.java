package com.example.documentsharingapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import android.database.Cursor;

/**
 * HomeActivity is the main activity after login that manages navigation between
 * Home, Documents, and Profile fragments using bottom navigation.
 */
public class HomeActivity extends AppCompatActivity {

    // Firebase instances
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private DatabaseReference docRef;

    // UI Components
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabUpload;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> documentPickerLauncher;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // Changed from homefragment to activity_main

        // Initialize Firebase and database references
        initializeFirebase();

        // Initialize UI components
        initializeUiComponents();

        // Initialize activity result launchers for picking images and documents
        initializeLaunchers();

        // Set up bottom navigation
        setupBottomNavigation();

        // Set up FAB click listener
        setupFabListener();

        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }

    /**
     * Initializes Firebase Authentication and database references.
     */
    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("users");
        docRef = FirebaseDatabase.getInstance().getReference("documents");
    }

    /**
     * Initializes all UI components from the activity_home layout.
     */
    private void initializeUiComponents() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabUpload = findViewById(R.id.fab_upload);
    }

    /**
     * Sets up the bottom navigation with item selection listener.
     */
    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_documents) {
                selectedFragment = new DocumentsFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });
    }

    /**
     * Sets up the floating action button click listener for document upload.
     */
    private void setupFabListener() {
        fabUpload.setOnClickListener(v -> pickDocument());
    }

    /**
     * Initializes the ActivityResultLaunchers for picking images and documents.
     */
    private void initializeLaunchers() {
        documentPickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri docUri = result.getData().getData();
                        saveDocument(docUri);
                    }
                });

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        saveProfilePicture(imageUri);
                    }
                });
    }

    /**
     * Launches an intent to pick a document from the device.
     */
    public void pickDocument() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        documentPickerLauncher.launch(intent);
    }

    /**
     * Launches an intent to pick an image from the device.
     */
    public void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    /**
     * Saves the selected document to local storage and updates the database.
     * @param docUri The URI of the selected document.
     */
    private void saveDocument(Uri docUri) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        try {
            String fileName = getFileNameFromUri(docUri);
            if (fileName == null) {
                fileName = "unnamed_" + System.currentTimeMillis() + ".file";
            }

            File docDir = new File(getFilesDir(), "documents");
            if (!docDir.exists()) {
                docDir.mkdirs();
            }
            File docFile = new File(docDir, fileName);

            InputStream inputStream = getContentResolver().openInputStream(docUri);
            FileOutputStream outputStream = new FileOutputStream(docFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();

            String localPath = docFile.getAbsolutePath();
            saveDocumentToDatabase(fileName, localPath);

            // Show success message
            Toast.makeText(this, "Document uploaded successfully", Toast.LENGTH_SHORT).show();

            // Refresh the current fragment if it's DocumentsFragment
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof DocumentsFragment) {
                ((DocumentsFragment) currentFragment).loadDocuments();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Saves the selected profile picture to local storage and updates the database.
     * @param imageUri The URI of the selected image.
     */
    private void saveProfilePicture(Uri imageUri) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        try {
            File profileDir = new File(getFilesDir(), "profile_pictures");
            if (!profileDir.exists()) {
                profileDir.mkdirs();
            }
            String fileName = user.getUid() + ".jpg";
            File profileFile = new File(profileDir, fileName);

            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            FileOutputStream outputStream = new FileOutputStream(profileFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();

            String localPath = profileFile.getAbsolutePath();
            saveProfileToDatabase(localPath);

            // Show success message
            Toast.makeText(this, "Profile picture updated successfully", Toast.LENGTH_SHORT).show();

            // Update the profile image in the ProfileFragment immediately
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof ProfileFragment) {
                ((ProfileFragment) currentFragment).updateProfileImage(Uri.fromFile(profileFile));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * Retrieves the original filename from a Uri using ContentResolver.
     * @param uri The Uri of the file.
     * @return The display name of the file, or null if not found.
     */
    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (nameIndex != -1) {
                fileName = cursor.getString(nameIndex);
            }
            cursor.close();
        }
        return fileName;
    }

    /**
     * Saves the profile picture path to the Realtime Database, preserving existing data.
     * @param localPath The local file path of the profile picture.
     */
    private void saveProfileToDatabase(String localPath) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Fetch existing profile data to preserve phone number
            dbRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Map<String, Object> profile = new HashMap<>();
                    if (snapshot.exists()) {
                        // Preserve existing data
                        String fullName = snapshot.child("fullName").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        String phone = snapshot.child("phone").getValue(String.class);

                        profile.put("fullName", fullName != null ? fullName : "User " + user.getUid().substring(0, 5));
                        profile.put("email", email != null ? email : user.getEmail());
                        profile.put("phone", phone != null ? phone : "");
                    } else {
                        // If no data exists, set default values
                        profile.put("fullName", "User " + user.getUid().substring(0, 5));
                        profile.put("email", user.getEmail());
                        profile.put("phone", "");
                    }
                    // Add or update the profile picture
                    profile.put("profilePicture", localPath);

                    // Save the updated profile
                    dbRef.child(user.getUid()).setValue(profile)
                            .addOnFailureListener(e -> Toast.makeText(HomeActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(HomeActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Saves document metadata to the Realtime Database.
     * @param fileName The name of the document.
     * @param localPath The local file path of the document.
     */
    private void saveDocumentToDatabase(String fileName, String localPath) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("fileName", fileName);
            metadata.put("localPath", localPath);
            metadata.put("timestamp", System.currentTimeMillis());

            docRef.child(user.getUid()).push().setValue(metadata)
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Signs out the current user and returns to the login screen.
     */
    public void signOut() {
        mAuth.signOut();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /**
     * Provides access to the database reference for documents.
     * @return The DatabaseReference for documents.
     */
    public DatabaseReference getDocumentReference() {
        return docRef;
    }

    /**
     * Provides access to the database reference for users.
     * @return The DatabaseReference for users.
     */
    public DatabaseReference getUserReference() {
        return dbRef;
    }

    /**
     * Provides access to the current Firebase user.
     * @return The current FirebaseUser.
     */
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    /**
     * Navigates to a specific tab in the bottom navigation.
     * @param tabId The resource ID of the tab to navigate to.
     */
    public void navigateToTab(int tabId) {
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(tabId);
        }
    }
}