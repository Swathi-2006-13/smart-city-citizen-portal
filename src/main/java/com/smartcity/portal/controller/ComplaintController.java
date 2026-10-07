package com.smartcity.portal.controller;

import com.smartcity.portal.entity.Complaint;
import com.smartcity.portal.entity.User;
import com.smartcity.portal.repository.ComplaintRepository;
import com.smartcity.portal.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;

    public ComplaintController(
            ComplaintRepository complaintRepository,
            UserRepository userRepository) {

        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> createComplaint(
            @RequestParam String phone,
            @RequestParam String place,
            @RequestParam String address,
            @RequestParam String area,
            @RequestParam String wardNumber,
            @RequestParam String category,
            @RequestParam String serviceRequired,
            @RequestParam String priority,
            @RequestParam String preferredContact,
            @RequestParam(required = false) String preferredDate,
            @RequestParam(required = false) String preferredTime,
            @RequestParam String description,
            @RequestParam(required = false) MultipartFile image,
            HttpSession session) {

        Object usernameObject = session.getAttribute("username");

        if (usernameObject == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please login first"));
        }

        String username = usernameObject.toString();

        User user = userRepository
                .findByUsername(username)
                .orElse(null);

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User not found"));
        }

        Complaint complaint = new Complaint();

        /*
         * Generate complaint ID such as:
         * SC10235
         */
        String complaintId = "SC" +
                String.format("%05d", System.currentTimeMillis() % 100000);

        complaint.setComplaintId(complaintId);
        complaint.setCitizen(user);

        complaint.setPhone(phone);
        complaint.setPlace(place);
        complaint.setAddress(address);
        complaint.setArea(area);
        complaint.setWardNumber(wardNumber);
        complaint.setCategory(category);
        complaint.setServiceRequired(serviceRequired);
        complaint.setPriority(priority);
        complaint.setPreferredContact(preferredContact);
        complaint.setDescription(description);

        if (preferredDate != null && !preferredDate.isBlank()) {
            complaint.setPreferredDate(
                    LocalDate.parse(preferredDate)
            );
        }

        if (preferredTime != null && !preferredTime.isBlank()) {
            complaint.setPreferredTime(
                    LocalTime.parse(preferredTime)
            );
        }

        /*
         * Save uploaded image
         */
        if (image != null && !image.isEmpty()) {

            try {

                Path uploadDirectory =
                        Paths.get("uploads");

                Files.createDirectories(uploadDirectory);

                String originalName =
                        image.getOriginalFilename();

                String extension = "";

                if (originalName != null &&
                        originalName.contains(".")) {

                    extension = originalName.substring(
                            originalName.lastIndexOf(".")
                    );
                }

                String fileName =
                        UUID.randomUUID() + extension;

                Path filePath =
                        uploadDirectory.resolve(fileName);

                Files.write(
                        filePath,
                        image.getBytes()
                );

                complaint.setImagePath(
                        "/uploads/" + fileName
                );

            } catch (IOException e) {

                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of(
                                "message",
                                "Unable to save uploaded image"
                        ));
            }
        }

        complaint.setStatus("RECEIVED");

        Complaint saved =
                complaintRepository.save(complaint);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Complaint submitted successfully",
                        "complaintId",
                        saved.getComplaintId()
                )
        );
    }

    @GetMapping("/track/{complaintId}")
    public ResponseEntity<?> trackComplaint(
            @PathVariable String complaintId) {

        Complaint complaint =
                complaintRepository
                        .findByComplaintId(complaintId)
                        .orElse(null);

        if (complaint == null) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "message",
                            "Complaint not found"
                    ));
        }

        return ResponseEntity.ok(
                convertToMap(complaint)
        );
    }

    @GetMapping("/mine")
    public ResponseEntity<?> myComplaints(
            HttpSession session) {

        Object usernameObject =
                session.getAttribute("username");

        if (usernameObject == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Please login first"
                    ));
        }

        String username =
                usernameObject.toString();

        return ResponseEntity.ok(
                complaintRepository
                        .findByCitizenUsernameOrderByCreatedAtDesc(
                                username
                        )
                        .stream()
                        .map(this::convertToMap)
                        .toList()
        );
    }

    private Map<String, Object> convertToMap(
            Complaint complaint) {

        Map<String, Object> data =
                new HashMap<>();

        data.put(
                "complaintId",
                complaint.getComplaintId()
        );

        data.put(
                "citizenName",
                complaint.getCitizen().getFullName()
        );

        data.put(
                "category",
                complaint.getCategory()
        );

        data.put(
                "location",
                complaint.getPlace()
        );

        data.put(
                "address",
                complaint.getAddress()
        );

        data.put(
                "area",
                complaint.getArea()
        );

        data.put(
                "wardNumber",
                complaint.getWardNumber()
        );

        data.put(
                "dateSubmitted",
                complaint.getCreatedAt()
        );

        data.put(
                "priority",
                complaint.getPriority()
        );

        data.put(
                "assignedOfficer",
                complaint.getAssignedOfficer()
        );

        data.put(
                "expectedCompletion",
                complaint.getExpectedCompletion()
        );

        data.put(
                "status",
                complaint.getStatus()
        );

        data.put(
                "description",
                complaint.getDescription()
        );

        data.put(
                "imagePath",
                complaint.getImagePath()
        );

        return data;
    }
}