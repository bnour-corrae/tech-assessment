package xyz.bnour.vehiclecatalog.service;

import xyz.bnour.vehiclecatalog.entity.Image;
import xyz.bnour.vehiclecatalog.entity.Vehicle;
import xyz.bnour.vehiclecatalog.repository.ImageRepository;
import xyz.bnour.vehiclecatalog.repository.VehicleRepository;
import xyz.bnour.vehiclecatalog.response.ImageResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ImageService {
    private final VehicleRepository vehicleRepository;
    private final ImageRepository imageRepository;
    private final S3Service s3Service;
    
    @Value("${xyz.bnour.vehicle-catalog.max-num-of-images-per-vehicle}")
    private Integer maxNumberOfImagesPerVehicle;

    public List<ImageResponse> getAll() {
        log.info("Fetching all images");
        List<ImageResponse> images = imageRepository.findAll()
                .stream()
                .map(ImageResponse::new)
                .toList();
        log.info("Successfully retrieved {} images", images.size());
        return images;
    }

    public ImageResponse getById(String id) {
        log.info("Fetching image by ID: {}", id);
        Image image = imageRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> {
                    log.error("Image not found with ID: {}", id);
                    return new EntityNotFoundException("Image not found with ID: " + id);
                });
        log.info("Successfully retrieved image: {} for vehicle: {}", image.getS3Key(), image.getVehicle().getId());
        return new ImageResponse(image);
    }

    @Transactional
    public ImageResponse create(String vehicleId, MultipartFile image) throws IOException {
        log.info("Creating new image for vehicle ID: {}, file: {}, size: {} bytes", 
                vehicleId, image.getOriginalFilename(), image.getSize());
        
        Vehicle vehicle = findVehicleById(vehicleId);

        validateImageLimit(vehicle.getId());
        
        String s3Key = generateS3Key(image);
        log.debug("Generated S3 key: {} for image upload", s3Key);
        
        s3Service.uploadImage(image.getInputStream(), s3Key, image.getSize());
        
        Image vehicleImage = new Image();
        vehicleImage.setS3Key(s3Key);
        vehicleImage.setVehicle(vehicle);
        vehicleImage.setUrl(s3Service.getImageUrl(s3Key));

        Image savedImage = imageRepository.save(vehicleImage);
        log.info("Successfully created image with ID: {} for vehicle: {}", savedImage.getId(), vehicleId);
        return new ImageResponse(savedImage);
    }

    @Transactional
    public ImageResponse update(String id, MultipartFile newImage) throws IOException {
        log.info("Updating image ID: {} with new file: {}, size: {} bytes", 
                id, newImage.getOriginalFilename(), newImage.getSize());
        
        Image existingImage = imageRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> {
                    log.error("Image not found for update with ID: {}", id);
                    return new EntityNotFoundException("Image not found with ID: " + id);
                });

        log.debug("Updating S3 object with key: {}", existingImage.getS3Key());
        s3Service.uploadImage(newImage.getInputStream(), existingImage.getS3Key(), newImage.getSize());
        
        Image updatedImage = imageRepository.save(existingImage);
        log.info("Successfully updated image with ID: {}", id);
        return new ImageResponse(updatedImage);
    }

    @Transactional
    public void deleteById(String id) {
        log.info("Deleting image with ID: {}", id);
        UUID imageId = UUID.fromString(id);
        if (!imageRepository.existsById(imageId)) {
            log.error("Image not found for deletion with ID: {}", id);
            throw new EntityNotFoundException("Image not found with ID: " + id);
        }
        imageRepository.deleteById(imageId);
        log.info("Successfully deleted image with ID: {}", id);
    }

    private Vehicle findVehicleById(String vehicleId) {
        log.debug("Looking up vehicle by ID: {}", vehicleId);
        return vehicleRepository.findById(UUID.fromString(vehicleId))
                .orElseThrow(() -> {
                    log.error("Vehicle not found with ID: {}", vehicleId);
                    return new EntityNotFoundException("Vehicle not found with ID: " + vehicleId);
                });
    }
    
    private void validateImageLimit(UUID vehicleId) {
        log.debug("Validating image limit for vehicle ID: {}", vehicleId);
        List<Image> existingImages = imageRepository.findAllByVehicleId(vehicleId);
        log.debug("Vehicle {} currently has {} images (limit: {})", vehicleId, existingImages.size(), maxNumberOfImagesPerVehicle);
        
        if (existingImages.size() >= maxNumberOfImagesPerVehicle) {
            log.error("Vehicle {} already has {} images, exceeding limit of {}", vehicleId, existingImages.size(), maxNumberOfImagesPerVehicle);
            throw new RuntimeException("Vehicle " + vehicleId + " already has " + 
                    existingImages.size() + " images (limit: " + maxNumberOfImagesPerVehicle + ")");
        }
    }
    
    private String generateS3Key(MultipartFile image) {
        String fileExtension = getFileExtension(image.getOriginalFilename());
        String s3Key = UUID.randomUUID() + fileExtension;
        log.debug("Generated secure S3 key: {} from original filename: {}", s3Key, image.getOriginalFilename());
        return s3Key;
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new RuntimeException("The image filename must contain an extension and not be null!");
        }

        return filename.substring(filename.lastIndexOf("."));
    }
}
