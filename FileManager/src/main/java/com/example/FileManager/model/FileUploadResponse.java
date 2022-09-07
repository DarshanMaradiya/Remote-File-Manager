package com.example.FileManager.model;

public class FileUploadResponse {

    private String fileName;
    private String contentType;
    private String uri;

    public FileUploadResponse(String fileName, String contentType, String uri) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.uri = uri;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
