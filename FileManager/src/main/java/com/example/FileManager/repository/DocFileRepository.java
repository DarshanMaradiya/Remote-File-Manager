package com.example.FileManager.repository;

import com.example.FileManager.model.FileDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocFileRepository extends CrudRepository<FileDocument, Long> {

    FileDocument findByFileName(String fileName);
}
