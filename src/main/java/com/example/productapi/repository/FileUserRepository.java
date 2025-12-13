package com.example.productapi.repository;

import com.example.productapi.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class FileUserRepository {
    
    @Value("${user.storage.file:data/users.json}")
    private String storageFilePath;
    
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
    
    public FileUserRepository() {
        this.objectMapper = new ObjectMapper();
    }
    
    @PostConstruct
    public void init() {
        loadFromFile();
    }
    
    private void loadFromFile() {
        File file = new File(storageFilePath);
        
        // Create parent directory if it doesn't exist
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        if (file.exists() && file.length() > 0) {
            try {
                List<User> userList = objectMapper.readValue(file, new TypeReference<List<User>>() {});
                users.clear();
                
                for (User user : userList) {
                    users.put(user.getUsername(), user);
                }
                
                System.out.println("Loaded " + users.size() + " users from file: " + storageFilePath);
            } catch (IOException e) {
                System.err.println("Error loading users from file: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No existing users file found at: " + storageFilePath);
        }
    }
    
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }
    
    public List<User> findAll() {
        return List.copyOf(users.values());
    }
}
