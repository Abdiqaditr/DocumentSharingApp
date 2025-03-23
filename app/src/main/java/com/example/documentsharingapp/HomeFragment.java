package com.example.documentsharingapp;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import de.hdodenhof.circleimageview.CircleImageView;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView userName;
    private TextView totalDocuments;
    private TextView recentDocuments;
    private TextView sharedDocuments;
    private CircleImageView profileImage;
    private RecyclerView recentDocumentsRecycler;
    private RecyclerView categoriesRecycler;
    private EditText searchInput;
    private TextView viewAllRecent;

    private HomeActivity homeActivity;
    private DatabaseReference docRef;
    private FirebaseUser currentUser;
    private List<Document> documentList = new ArrayList<>();
    private List<Document> filteredDocumentList = new ArrayList<>();
    private RecentDocumentAdapter recentAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.homefragment, container, false);

        // Get references from parent activity
        homeActivity = (HomeActivity) getActivity();
        if (homeActivity != null) {
            docRef = homeActivity.getDocumentReference();
            currentUser = homeActivity.getCurrentUser();
        }

        // Initialize UI components
        initializeUiComponents(view);

        // Set up recycler views
        setupRecyclerViews();

        // Load user data and documents
        loadUserData();
        loadDocuments();

        // Set up click listeners and search functionality
        setupListeners();

        return view;
    }

    private void initializeUiComponents(View view) {
        userName = view.findViewById(R.id.userName);
        totalDocuments = view.findViewById(R.id.totalDocuments);
        recentDocuments = view.findViewById(R.id.recentDocuments);
        sharedDocuments = view.findViewById(R.id.sharedDocuments);
        profileImage = view.findViewById(R.id.profileImage);
        recentDocumentsRecycler = view.findViewById(R.id.recentDocumentsRecycler);
        categoriesRecycler = view.findViewById(R.id.categoriesRecycler);
        searchInput = view.findViewById(R.id.searchInput);
        viewAllRecent = view.findViewById(R.id.viewAllRecent);
    }

    private void setupRecyclerViews() {
        // Set up recent documents recycler view
        recentDocumentsRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        filteredDocumentList.addAll(documentList); // Initialize filtered list with all documents
        recentAdapter = new RecentDocumentAdapter(filteredDocumentList);
        recentDocumentsRecycler.setAdapter(recentAdapter);

        // Set up categories recycler view (placeholder)
        categoriesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        // categoriesRecycler.setAdapter(new CategoryAdapter());
    }

    private void loadUserData() {
        if (currentUser != null) {
            homeActivity.getUserReference().child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String fullName = snapshot.child("fullName").getValue(String.class);
                        if (fullName != null && !fullName.isEmpty()) {
                            userName.setText(fullName);
                        } else {
                            userName.setText("User");
                        }

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

    private void loadDocuments() {
        if (currentUser != null) {
            docRef.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    documentList.clear();
                    int count = 0;
                    for (DataSnapshot docSnapshot : snapshot.getChildren()) {
                        Document doc = docSnapshot.getValue(Document.class);
                        if (doc != null) {
                            documentList.add(doc);
                            count++;
                        }
                    }

                    // Sort by timestamp (most recent first)
                    Collections.sort(documentList, (d1, d2) -> Long.compare(d2.getTimestamp(), d1.getTimestamp()));

                    // Update UI with document counts
                    totalDocuments.setText(String.valueOf(count));

                    // Show only the most recent 3 documents initially
                    List<Document> recentDocs = new ArrayList<>();
                    for (int i = 0; i < Math.min(3, documentList.size()); i++) {
                        recentDocs.add(documentList.get(i));
                    }

                    recentDocuments.setText(String.valueOf(recentDocs.size()));
                    sharedDocuments.setText("0"); // Implement shared count logic

                    // Update filtered list and recycler view
                    filteredDocumentList.clear();
                    filteredDocumentList.addAll(recentDocs);
                    filterDocuments(searchInput.getText().toString()); // Apply current search query
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
        }
    }

    private void setupListeners() {
        viewAllRecent.setOnClickListener(v -> {
            if (homeActivity != null) {
                homeActivity.navigateToTab(R.id.nav_documents);
            }
        });

        // Add search functionality
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDocuments(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Remove the old click listener that navigated to DocumentsFragment
        // searchInput.setOnClickListener(v -> { ... });
    }

    private void filterDocuments(String query) {
        filteredDocumentList.clear();
        if (query.isEmpty()) {
            // Show up to 3 most recent documents when query is empty
            for (int i = 0; i < Math.min(3, documentList.size()); i++) {
                filteredDocumentList.add(documentList.get(i));
            }
        } else {
            // Filter based on query
            for (Document doc : documentList) {
                if (doc.getFileName().toLowerCase().contains(query.toLowerCase())) {
                    filteredDocumentList.add(doc);
                }
            }
        }
        recentAdapter.notifyDataSetChanged();
        recentDocuments.setText(String.valueOf(filteredDocumentList.size()));
    }
}