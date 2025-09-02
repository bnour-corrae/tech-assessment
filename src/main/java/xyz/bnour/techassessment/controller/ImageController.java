package xyz.bnour.techassessment.controller;

import xyz.bnour.techassessment.response.ImageResponse;
import xyz.bnour.techassessment.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
@Slf4j
public class ImageController {
    private final ImageService imageService;

    @GetMapping
    public ResponseEntity<List<ImageResponse>> getAll() {
        log.info("GET /images - Fetching all images");
        List<ImageResponse> images = imageService.getAll();
        log.info("GET /images - Returning {} images", images.size());
        return ResponseEntity.ok(images);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImageResponse> getById(@PathVariable String id) {
        log.info("GET /images/{} - Fetching image by ID", id);
        ImageResponse image = imageService.getById(id);
        log.info("GET /images/{} - Successfully retrieved image for vehicle: {}", id, image.getVehicleId());
        return ResponseEntity.ok(image);
    }

    @PostMapping("/vehicles/{vehicleId}")
    public ResponseEntity<ImageResponse> create(
            @PathVariable String vehicleId,
            @RequestParam("image") MultipartFile image) {
        log.info("POST /images/vehicles/{} - Uploading image: {}, size: {} bytes", 
                vehicleId, image.getOriginalFilename(), image.getSize());
        try {
            validateImageFile(image);
            ImageResponse createdImage = imageService.create(vehicleId, image);
            log.info("POST /images/vehicles/{} - Successfully uploaded image with ID: {}", 
                    vehicleId, createdImage.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdImage);
        } catch (IOException e) {
            log.error("POST /images/vehicles/{} - Failed to upload image: {}", vehicleId, e.getMessage());
            throw new RuntimeException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImageResponse> update(
            @PathVariable String id,
            @RequestParam("image") MultipartFile image) {
        log.info("PUT /images/{} - Updating image with file: {}, size: {} bytes", 
                id, image.getOriginalFilename(), image.getSize());
        try {
            validateImageFile(image);
            ImageResponse updatedImage = imageService.update(id, image);
            log.info("PUT /images/{} - Successfully updated image", id);
            return ResponseEntity.ok(updatedImage);
        } catch (IOException e) {
            log.error("PUT /images/{} - Failed to update image: {}", id, e.getMessage());
            throw new RuntimeException("Failed to update image: " + e.getMessage(), e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable String id) {
        log.info("DELETE /images/{} - Deleting image", id);
        imageService.deleteById(id);
        log.info("DELETE /images/{} - Successfully deleted image", id);
        return ResponseEntity.noContent().build();
    }

    private void validateImageFile(MultipartFile file) {
        log.debug("Validating image file: {}, type: {}, size: {} bytes", 
                file.getOriginalFilename(), file.getContentType(), file.getSize());
        
        if (file.isEmpty()) {
            log.warn("Image validation failed: File is empty");
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.warn("Image validation failed: Invalid content type: {}", contentType);
            throw new IllegalArgumentException("File must be an image");
        }
        
        log.debug("Image file validation passed");
    }
}
