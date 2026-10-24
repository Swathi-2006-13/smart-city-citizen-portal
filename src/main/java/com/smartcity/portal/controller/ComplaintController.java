
package com.smartcity.portal.controller;

import com.smartcity.portal.entity.Complaint;
import com.smartcity.portal.entity.User;
import com.smartcity.portal.repository.ComplaintRepository;
import com.smartcity.portal.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
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

    // ========================================
    // CREATE COMPLAINT
    // ========================================

    @PostMapping
    public ResponseEntity<?> createComplaint(

            @RequestParam("phone") String phone,
            @RequestParam("place") String place,
            @RequestParam("address") String address,
            @RequestParam("area") String area,
            @RequestParam("wardNumber") String wardNumber,
            @RequestParam("category") String category,
            @RequestParam("serviceRequired") String serviceRequired,
            @RequestParam("priority") String priority,
            @RequestParam("preferredContact") String preferredContact,

            @RequestParam(value = "preferredDate", required = false)
            String preferredDate,

            @RequestParam(value = "preferredTime", required = false)
            String preferredTime,

            @RequestParam("description") String description,

            @RequestParam(value = "image", required = false)
            MultipartFile image,

            HttpSession session) {

        try {

            // Check login session
            Object usernameObject = session.getAttribute("username");

            if (usernameObject == null) {
                return errorResponse(
                        HttpStatus.UNAUTHORIZED,
                        "Please login first."
                );
            }

            String username = usernameObject.toString();

            User user = userRepository
                    .findByUsername(username)
                    .orElse(null);

            if (user == null) {
                return errorResponse(
                        HttpStatus.UNAUTHORIZED,
                        "Logged-in user was not found."
                );
            }

            // Validate required fields
            if (phone.isBlank() || place.isBlank()
                    || address.isBlank() || area.isBlank()
                    || wardNumber.isBlank() || category.isBlank()
                    || serviceRequired.isBlank()
                    || priority.isBlank()
                    || preferredContact.isBlank()
                    || description.isBlank()) {

                return errorResponse(
                        HttpStatus.BAD_REQUEST,
                        "Please fill in all required fields."
                );
            }

            // Create complaint
            Complaint complaint = new Complaint();

            // Generate unique complaint ID
            String complaintId =
                    "SC" + UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .substring(0, 10)
                            .toUpperCase();

            complaint.setComplaintId(complaintId);

            // Connect complaint to logged-in citizen
            complaint.setCitizen(user);

            complaint.setPhone(phone.trim());
            complaint.setPlace(place.trim());
            complaint.setAddress(address.trim());
            complaint.setArea(area.trim());
            complaint.setWardNumber(wardNumber.trim());
            complaint.setCategory(category);
            complaint.setServiceRequired(serviceRequired);
            complaint.setPriority(priority);
            complaint.setPreferredContact(preferredContact);
            complaint.setDescription(description.trim());

            // Date
            if (preferredDate != null && !preferredDate.isBlank()) {
                complaint.setPreferredDate(
                        LocalDate.parse(preferredDate)
                );
            }

            // Time
            if (preferredTime != null && !preferredTime.isBlank()) {
                complaint.setPreferredTime(
                        LocalTime.parse(preferredTime)
                );
            }

            // Upload image
            if (image != null && !image.isEmpty()) {

                String contentType = image.getContentType();

                if (contentType == null ||
                        !contentType.startsWith("image/")) {

                    return errorResponse(
                            HttpStatus.BAD_REQUEST,
                            "Please upload a valid image."
                    );
                }

                Path uploadDirectory = Paths.get("uploads");

                Files.createDirectories(uploadDirectory);

                String extension = "";

                String originalName = image.getOriginalFilename();

                if (originalName != null &&
                        originalName.contains(".")) {

                    extension = originalName.substring(
                            originalName.lastIndexOf(".")
                    );
                }

                String fileName =
                        UUID.randomUUID() + extension;

                Path filePath =
                        uploadDirectory.resolve(fileName).normalize();

                if (!filePath.startsWith(uploadDirectory.normalize())) {
                    return errorResponse(
                            HttpStatus.BAD_REQUEST,
                            "Invalid image filename."
                    );
                }

                Files.copy(image.getInputStream(), filePath);

                complaint.setImagePath(
                        "/uploads/" + fileName
                );
            }

            complaint.setStatus("RECEIVED");

            // Set creation time
            complaint.setCreatedAt(LocalDateTime.now());

            // Save complaint in MySQL
            Complaint savedComplaint =
                    complaintRepository.saveAndFlush(complaint);

            // Return saved complaint ID
            Map<String, Object> result = new HashMap<>();

            result.put(
                    "message",
                    "Complaint submitted successfully."
            );

            result.put(
                    "complaintId",
                    savedComplaint.getComplaintId()
            );

            result.put(
                    "status",
                    savedComplaint.getStatus()
            );

            return ResponseEntity.ok(result);

        } catch (DateTimeParseException e) {

            return errorResponse(
                    HttpStatus.BAD_REQUEST,
                    "Invalid preferred date or time."
            );

        } catch (IOException e) {

            return errorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to save uploaded image."
            );

        } catch (DataAccessException e) {

            e.printStackTrace();

            return errorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Database error. Complaint could not be saved."
            );

        } catch (Exception e) {

            e.printStackTrace();

            return errorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to submit complaint: " + e.getMessage()
            );
        }
    }

    // ========================================
    // TRACK COMPLAINT
    // ========================================

    @GetMapping("/track/{complaintId}")
    public ResponseEntity<?> trackComplaint(
            @PathVariable String complaintId) {

        Complaint complaint = complaintRepository
                .findByComplaintId(complaintId)
                .orElse(null);

        if (complaint == null) {

            return errorResponse(
                    HttpStatus.NOT_FOUND,
                    "Complaint not found. Please check your Complaint ID."
            );
        }

        return ResponseEntity.ok(
                convertToMap(complaint)
        );
    }

    // ========================================
    // VIEW MY COMPLAINTS
    // ========================================

    @GetMapping("/mine")
    public ResponseEntity<?> myComplaints(HttpSession session) {

        Object usernameObject =
                session.getAttribute("username");

        if (usernameObject == null) {

            return errorResponse(
                    HttpStatus.UNAUTHORIZED,
                    "Please login first."
            );
        }

        String username = usernameObject.toString();

        return ResponseEntity.ok(
                complaintRepository
                        .findByCitizenUsernameOrderByCreatedAtDesc(username)
                        .stream()
                        .map(this::convertToMap)
                        .toList()
        );
    }

    // ========================================
    // CONVERT COMPLAINT TO JSON
    // ========================================

    private Map<String, Object> convertToMap(
            Complaint complaint) {

        Map<String, Object> data = new HashMap<>();

        data.put("id", complaint.getId());
        data.put("complaintId", complaint.getComplaintId());

        data.put(
                "citizenName",
                complaint.getCitizen() != null
                        ? complaint.getCitizen().getFullName()
                        : null
        );

        data.put("category", complaint.getCategory());
        data.put("location", complaint.getPlace());
        data.put("address", complaint.getAddress());
        data.put("area", complaint.getArea());
        data.put("wardNumber", complaint.getWardNumber());
        data.put("phone", complaint.getPhone());
        data.put("serviceRequired", complaint.getServiceRequired());
        data.put("dateSubmitted", complaint.getCreatedAt());
        data.put("priority", complaint.getPriority());
        data.put("preferredContact", complaint.getPreferredContact());
        data.put("preferredDate", complaint.getPreferredDate());
        data.put("preferredTime", complaint.getPreferredTime());
        data.put("assignedOfficer", complaint.getAssignedOfficer());
        data.put("expectedCompletion", complaint.getExpectedCompletion());
        data.put("status", complaint.getStatus());
        data.put("description", complaint.getDescription());
        data.put("imagePath", complaint.getImagePath());

        return data;
    }

    // ========================================
    // ERROR RESPONSE
    // ========================================

    private ResponseEntity<Map<String, String>> errorResponse(
            HttpStatus status,
            String message) {

        Map<String, String> error = new HashMap<>();

        error.put("message", message);

        return ResponseEntity
                .status(status)
                .body(error);
    }
}
