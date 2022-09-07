package com.example.FileManager.controller;

import com.example.FileManager.model.FileDocument;
import com.example.FileManager.model.FileUploadResponse;
import com.example.FileManager.repository.DocFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping("/api/fileDb")
public class FileWithDatabaseController {
    @Autowired
    private DocFileRepository docFileRepository;

    @GetMapping
    String HomePage() {
        return "Welcome to File Manager With Database API";
    }

    @PostMapping("upload/single")
    public ResponseEntity<FileUploadResponse> SingleFileUpload(@RequestParam("file") MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        // Store the incoming file in DB
        FileDocument fileDocument = docFileRepository.findByFileName(fileName);

        if(fileDocument == null)
            fileDocument = new FileDocument();

        fileDocument.setFileName(fileName);
        fileDocument.setDocFile(file.getBytes());
        docFileRepository.save(fileDocument);

        // Generate download URL for given file
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("api/fileDb/view/")
                .path(fileName)
                .toUriString();

        String contentType = file.getContentType();
        // Create a response containing fileName, contentType and download URL
        FileUploadResponse fileUploadResponse = new FileUploadResponse(fileName, contentType, url);
        // Send Response
        return new ResponseEntity<>(fileUploadResponse, HttpStatus.OK);
    }

    @GetMapping("download/{fileName}")
    public ResponseEntity<byte[]> downloadSingleFile(@PathVariable("fileName") String fileName, HttpServletRequest request) {
        FileDocument fileDocument = docFileRepository.findByFileName(fileName);

        String mimeType = request.getServletContext().getMimeType(fileDocument.getFileName());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                // attachment will download the file
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename="+fileDocument.getFileName())
                .body(fileDocument.getDocFile());
    }

    @GetMapping("view/{fileName}")
    public ResponseEntity<byte[]> viewSingleFile(@PathVariable("fileName") String fileName, HttpServletRequest request) {
        FileDocument fileDocument = docFileRepository.findByFileName(fileName);

        String mimeType = request.getServletContext().getMimeType(fileDocument.getFileName());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                // inline will view the file
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline;filename="+fileDocument.getFileName())
                .body(fileDocument.getDocFile());
    }

}
