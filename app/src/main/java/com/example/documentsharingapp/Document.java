package com.example.documentsharingapp;

public class Document {
    private String fileName;
    private String localPath;
    private long timestamp;

    // Required empty constructor for Firebase
    public Document() {
    }

    public Document(String fileName, String localPath, long timestamp) {
        this.fileName = fileName;
        this.localPath = localPath;
        this.timestamp = timestamp;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}