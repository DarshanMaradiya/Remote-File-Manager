package com.example.FileManager.controller;

import com.example.FileManager.model.FileUploadResponse;
import com.example.FileManager.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/file")
public class FileWithSystemController {
    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping String HomePage() {
        return "Welcome to File Manager With System API";
    }

    @PostMapping("upload/single")
    public FileUploadResponse SingleFileUpload(@RequestParam("file") MultipartFile file) {
        // Store the incoming file
        String fileName = fileStorageService.storeFile(file);
        // Generate download URL for given file
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("api/file/view/")
                .path(fileName)
                .toUriString();
        String contentType = file.getContentType();
        // Create a response containing fileName, contentType and download URL
        FileUploadResponse fileUploadResponse = new FileUploadResponse(fileName, contentType, url);
        // Send Response
        return fileUploadResponse;
    }

    @GetMapping("download/{fileName}")
    public ResponseEntity<Resource> downloadSingleFile(@PathVariable("fileName") String fileName, HttpServletRequest request) {
        Resource resource = fileStorageService.downloadFile(fileName);
//        MediaType contentType = MediaType.IMAGE_JPEG;
        String mimeType = "";
        try {
            mimeType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException e) {
            mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                // attachment will download the file
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename="+resource.getFilename())
                .body(resource);
    }

    @GetMapping("view/{fileName}")
    public ResponseEntity<Resource> viewSingleFile(@PathVariable("fileName") String fileName, HttpServletRequest request) {
        Resource resource = fileStorageService.downloadFile(fileName);
//        MediaType contentType = MediaType.IMAGE_JPEG;
        String mimeType = "";
        try {
            mimeType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException e) {
            mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                // inline will preview the file
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline;filename="+resource.getFilename())
                .body(resource);
    }

    @PostMapping("upload/multiple")
    public List<FileUploadResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        if(files.length > 7) {
            throw new RuntimeException("Too Many Files");
        }
        List<FileUploadResponse> fileUploadResponses = new ArrayList<>();
        Arrays.asList(files).stream().forEach(file -> {
            String fileName = fileStorageService.storeFile(file);
            String contentType = file.getContentType();
            String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("api/file/view/")
                    .path(fileName)
                    .toUriString();
            FileUploadResponse fileUploadResponse = new FileUploadResponse(fileName, contentType, url);
            fileUploadResponses.add(fileUploadResponse);
        });

        return fileUploadResponses;
    }

    @GetMapping("download/zip")
    void zipDownload(@RequestParam("fileName") String[] files, HttpServletResponse response) throws IOException {
        response.setStatus(200);
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=PackedFiles.zip");
        // ZipOutputStream: List of zipEntries objects
        try(ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            Arrays.asList(files)
                    .stream()
                    .forEach(file -> {
                        Resource resource = fileStorageService.downloadFile(file);
                        ZipEntry zipEntry = new ZipEntry(resource.getFilename());
                        try {
                            zipEntry.setSize(resource.contentLength());
                            zos.putNextEntry(zipEntry);
                            StreamUtils.copy(resource.getInputStream(), zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new RuntimeException("Some Exception While zipping");
                        }
                    });
            zos.finish();
        }
        //search in browser: localhost:8081/api/file/download/zip?fileNames=photo1.png&photo2.png
        // It will download the files as in one zip
    }
}
