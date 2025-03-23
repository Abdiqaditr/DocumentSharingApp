package com.example.documentsharingapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import de.hdodenhof.circleimageview.CircleImageView;
import java.io.File;

public class ProfileFragment extends Fragment {

    private CircleImageView profileImage;
    private TextView userName;
    private TextView userEmail;
    private TextView userBio;
    private TextView totalDocuments;
    private TextView totalStorage;
    private MaterialButton logoutButton;
    private LinearLayout editProfileButton;
    private LinearLayout changePasswordButton;
    private View editProfileImage;

    private HomeActivity homeActivity;
    private DatabaseReference userRef;
    private DatabaseReference docRef;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profilefragment, container, false);

        // Get references from parent activity
        homeActivity = (HomeActivity) getActivity();
        if (homeActivity != null) {
            userRef = homeActivity.getUserReference();
            docRef = homeActivity.getDocumentReference();
            currentUser = homeActivity.getCurrentUser();
        }

        // Initialize UI components
        initializeUiComponents(view);

        // Load user profile and document stats
        loadProfile();
        loadDocumentStats();

        // Set up click listeners
        setupClickListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload profile data when fragment becomes visible again
        loadProfile();
        loadDocumentStats();
    }

    private void initializeUiComponents(View view) {
        profileImage = view.findViewById(R.id.profileImage);
        userName = view.findViewById(R.id.userName);
        userEmail = view.findViewById(R.id.userEmail);
        userBio = view.findViewById(R.id.userBio);
        totalDocuments = view.findViewById(R.id.totalDocuments);
        totalStorage = view.findViewById(R.id.totalStorage);
        logoutButton = view.findViewById(R.id.logoutButton);
        editProfileButton = view.findViewById(R.id.editProfileButton);
        changePasswordButton = view.findViewById(R.id.changePasswordButton);
        editProfileImage = view.findViewById(R.id.editProfileImage);
    }

    public void loadProfile() {
        if (currentUser != null) {
            userRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Set user name
                        String fullName = snapshot.child("fullName").getValue(String.class);
                        if (fullName != null && !fullName.isEmpty()) {
                            userName.setText(fullName);
                        } else {
                            userName.setText("User");
                        }

                        // Set user email
                        String email = snapshot.child("email").getValue(String.class);
                        if (email != null && !email.isEmpty()) {
                            userEmail.setText(email);
                        } else if (currentUser.getEmail() != null) {
                            userEmail.setText(currentUser.getEmail());
                        }

                        // Set user bio
                        String bio = snapshot.child("bio").getValue(String.class);
                        if (bio != null && !bio.isEmpty()) {
                            userBio.setText(bio);
                        } else {
                            userBio.setText("No bio added yet");
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
                    // Handle error
                }
            });
        }
    }

    private void loadDocumentStats() {
        if (currentUser != null) {
            docRef.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int count = 0;
                    long totalSize = 0;

                    for (DataSnapshot docSnapshot : snapshot.getChildren()) {
                        count++;

                        // Calculate total storage (this is a simplified example)
                        String localPath = docSnapshot.child("localPath").getValue(String.class);
                        if (localPath != null) {
                            File docFile = new File(localPath);
                            if (docFile.exists()) {
                                totalSize += docFile.length();
                            }
                        }
                    }

                    // Update UI with document count and storage used
                    totalDocuments.setText(String.valueOf(count));

                    // Convert bytes to MB for display
                    double sizeMB = totalSize / (1024.0 * 1024.0);
                    totalStorage.setText(String.format("%.1f", sizeMB));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
        }
    }

    private void setupClickListeners() {
        // Logout button
        logoutButton.setOnClickListener(v -> {
            if (homeActivity != null) {
                homeActivity.signOut();
            }
        });

        // Edit profile button
        editProfileButton.setOnClickListener(v -> {
            // Launch EditProfileActivity
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        // Change password button
        changePasswordButton.setOnClickListener(v -> {
            // Handle change password functionality
            // This could open a dialog or start a new activity
        });

        // Edit profile image
        editProfileImage.setOnClickListener(v -> {
            if (homeActivity != null) {
                homeActivity.pickImage();
            }
        });
    }

    /**
     * Updates the profile image immediately without waiting for database refresh
     * @param imageUri The URI of the new profile image
     */
    public void updateProfileImage(Uri imageUri) {
        if (imageUri != null && profileImage != null) {
            profileImage.setImageURI(imageUri);
        }
    }
}

