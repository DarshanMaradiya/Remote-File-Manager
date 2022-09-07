package com.example.FileManager.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {
    Path fileStoragePath;

    /* Set the file storage path
     */
    public FileStorageService(@Value("${file.storage.location:temp}") String fileStorageLocation) {
        fileStoragePath = Paths.get(fileStorageLocation).toAbsolutePath().normalize();
        try {
            // Creating the storage folder at specified location
            Files.createDirectories(fileStoragePath);
        } catch (IOException e) {
            throw new RuntimeException("Issue in creating file Directory: " + e.getMessage());
        }
    }

    /* Store the file and return fileName
     */
    public String storeFile(MultipartFile file) {
        // Get FileName
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        // Create Path for the given file at file storage location
        Path filePath = Paths.get(fileStoragePath + "/" + fileName);
        try {
            // Copy the content of file at given filePath
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Issue in storing the file: " + e.getMessage());
        }
        // Return the filename
        return fileName;
    }

    public Resource downloadFile(String fileName) {
        Path path = fileStoragePath.toAbsolutePath().resolve(fileName);
        Resource resource;
        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Issue in reading the file: " + e.getMessage());
        }

        if(resource != null && resource.exists() && resource.isReadable())
            return resource;
        else
            throw new RuntimeException("File doesn't exists or not readable");
    }
}
