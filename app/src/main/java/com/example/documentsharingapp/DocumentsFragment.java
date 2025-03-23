package com.example.documentsharingapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class DocumentsFragment extends Fragment {

    private RecyclerView documentsRecycler;
    private ChipGroup filterChipGroup;
    private LinearLayout emptyState;
    private EditText searchInput;

    private HomeActivity homeActivity;
    private DatabaseReference docRef;
    private FirebaseUser currentUser;
    private List<Document> documentList = new ArrayList<>();
    private List<Document> filteredList = new ArrayList<>();
    private DocumentAdapter documentAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.documentsfragment, container, false);

        // Get references from parent activity
        homeActivity = (HomeActivity) getActivity();
        if (homeActivity != null) {
            docRef = homeActivity.getDocumentReference();
            currentUser = homeActivity.getCurrentUser();
        }

        // Initialize UI components
        initializeUiComponents(view);

        // Set up recycler view
        setupRecyclerView();

        // Load documents
        loadDocuments();

        // Set up listeners
        setupListeners();

        return view;
    }

    private void initializeUiComponents(View view) {
        documentsRecycler = view.findViewById(R.id.documentsRecycler);
        filterChipGroup = view.findViewById(R.id.filterChipGroup);
        emptyState = view.findViewById(R.id.emptyState);
        searchInput = view.findViewById(R.id.searchInput);
    }

    private void setupRecyclerView() {
        documentsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        documentAdapter = new DocumentAdapter(filteredList);
        documentsRecycler.setAdapter(documentAdapter);
    }

    public void loadDocuments() {
        if (currentUser != null) {
            docRef.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    documentList.clear();
                    for (DataSnapshot docSnapshot : snapshot.getChildren()) {
                        Document doc = docSnapshot.getValue(Document.class);
                        if (doc != null) {
                            documentList.add(doc);
                        }
                    }

                    // Update UI based on document count
                    if (documentList.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                        documentsRecycler.setVisibility(View.GONE);
                    } else {
                        emptyState.setVisibility(View.GONE);
                        documentsRecycler.setVisibility(View.VISIBLE);
                        filterDocuments(searchInput.getText().toString(), getSelectedFileType());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
        }
    }

    private void setupListeners() {
        // Search input listener
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDocuments(s.toString(), getSelectedFileType());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Chip group listener
        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            filterDocuments(searchInput.getText().toString(), getSelectedFileType());
        });
    }

    private String getSelectedFileType() {
        int checkedId = filterChipGroup.getCheckedChipId();
        if (checkedId == R.id.pdfChip) {
            return "pdf";
        } else if (checkedId == R.id.docChip) {
            return "doc";
        } else if (checkedId == R.id.xlsChip) {
            return "xls";
        } else if (checkedId == R.id.pptChip) {
            return "ppt";
        } else if (checkedId == R.id.imgChip) {
            return "img";
        } else if (checkedId == R.id.allChip) {
            return "all";
        } else {
            return "all"; // Default to all
        }
    }

    private void filterDocuments(String query, String fileType) {
        filteredList.clear();

        for (Document doc : documentList) {
            String fileName = doc.getFileName().toLowerCase();
            boolean matchesQuery = query.isEmpty() || fileName.contains(query.toLowerCase());
            boolean matchesType = false;

            if (fileType.equals("all")) {
                matchesType = true;
            } else if (fileType.equals("pdf")) {
                matchesType = fileName.endsWith(".pdf");
            } else if (fileType.equals("doc")) {
                matchesType = fileName.endsWith(".doc") || fileName.endsWith(".docx");
            } else if (fileType.equals("xls")) {
                matchesType = fileName.endsWith(".xls") || fileName.endsWith(".xlsx");
            } else if (fileType.equals("ppt")) {
                matchesType = fileName.endsWith(".ppt") || fileName.endsWith(".pptx");
            } else if (fileType.equals("img")) {
                matchesType = fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
                        fileName.endsWith(".png") || fileName.endsWith(".gif");
            }

            if (matchesQuery && matchesType) {
                filteredList.add(doc);
            }
        }

        documentAdapter.notifyDataSetChanged();
    }
}

