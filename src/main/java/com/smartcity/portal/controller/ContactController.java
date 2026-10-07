package com.smartcity.portal.controller;

import com.smartcity.portal.entity.ContactMessage;
import com.smartcity.portal.repository.ContactMessageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/contact")
public class ContactController {
    private final ContactMessageRepository repo;
    public ContactController(ContactMessageRepository repo){this.repo=repo;}

    @PostMapping
    public ResponseEntity<?> send(@RequestBody ContactMessage message){
        repo.save(message);
        return ResponseEntity.ok(Map.of("message","Thank you for contacting Smart City. We will reply soon."));
    }
}
