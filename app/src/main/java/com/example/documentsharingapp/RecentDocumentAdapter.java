package com.example.documentsharingapp;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.List;

public class RecentDocumentAdapter extends RecyclerView.Adapter<RecentDocumentAdapter.ViewHolder> {

    private List<Document> documents;

    public RecentDocumentAdapter(List<Document> documents) {
        this.documents = documents;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_document, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Document document = documents.get(position);
        holder.bind(document);
    }

    @Override
    public int getItemCount() {
        return documents.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView docIcon;
        private TextView docName;
        private TextView docDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            docIcon = itemView.findViewById(R.id.docIcon);
            docName = itemView.findViewById(R.id.docName);
            docDate = itemView.findViewById(R.id.docDate);
        }

        public void bind(Document document) {
            // Set document name
            docName.setText(document.getFileName());

            // Set document date
            long timestamp = document.getTimestamp();
            String dateStr = new java.text.SimpleDateFormat("MMM dd, yyyy").format(new java.util.Date(timestamp));
            docDate.setText(dateStr);

            // Set document icon based on file type
            String fileName = document.getFileName().toLowerCase();
            if (fileName.endsWith(".pdf")) {
                docIcon.setImageResource(R.drawable.ic_pdf);
            } else if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
                docIcon.setImageResource(R.drawable.ic_doc);
            } else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
                docIcon.setImageResource(R.drawable.ic_xls);
            } else if (fileName.endsWith(".ppt") || fileName.endsWith(".pptx")) {
                docIcon.setImageResource(R.drawable.ic_ppt);
            } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
                    fileName.endsWith(".png") || fileName.endsWith(".gif")) {
                docIcon.setImageResource(R.drawable.ic_img);
            } else {
                docIcon.setImageResource(R.drawable.ic_file);
            }

            // Set click listener to open the file
            itemView.setOnClickListener(v -> openFile(document));
        }

        private void openFile(Document document) {
            File file = new File(document.getLocalPath());
            if (!file.exists()) {
                // Handle case where file doesn't exist
                return;
            }

            // Get URI using FileProvider
            Uri fileUri = FileProvider.getUriForFile(
                    itemView.getContext(),
                    "com.example.documentsharingapp.fileprovider",
                    file
            );

            // Determine MIME type based on file extension
            String mimeType = getMimeType(document.getFileName().toLowerCase());

            // Create intent to open the file
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                itemView.getContext().startActivity(intent);
            } catch (Exception e) {
                // Handle case where no app can open the file
                e.printStackTrace();
            }
        }

        private String getMimeType(String fileName) {
            if (fileName.endsWith(".pdf")) {
                return "application/pdf";
            } else if (fileName.endsWith(".doc")) {
                return "application/msword";
            } else if (fileName.endsWith(".docx")) {
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            } else if (fileName.endsWith(".xls")) {
                return "application/vnd.ms-excel";
            } else if (fileName.endsWith(".xlsx")) {
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            } else if (fileName.endsWith(".ppt")) {
                return "application/vnd.ms-powerpoint";
            } else if (fileName.endsWith(".pptx")) {
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                return "image/jpeg";
            } else if (fileName.endsWith(".png")) {
                return "image/png";
            } else if (fileName.endsWith(".gif")) {
                return "image/gif";
            } else {
                return "*/*"; // Fallback MIME type
            }
        }
    }
}