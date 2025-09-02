package xyz.bnour.vehiclecatalog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {
    
    private final S3Client s3Client;
    
    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String bucketRegion;

    public void uploadImage(InputStream inputStream, String key, Long imageSize) {
        validateUploadParameters(inputStream, key, imageSize);
        
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("image/jpeg")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, imageSize));
            log.info("Successfully uploaded image with key: {}", key);
            
        } catch (S3Exception e) {
            log.error("Failed to upload image with key: {}. Error: {}", key, e.getMessage());
            throw new RuntimeException("Failed to upload image to S3: " + e.getMessage(), e);
        }
    }

    public String getImageUrl(String s3Key) {
        if (s3Key == null || s3Key.trim().isEmpty()) {
            throw new IllegalArgumentException("S3 key cannot be null or empty");
        }
        
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, bucketRegion, s3Key);
    }
    
    private void validateUploadParameters(InputStream inputStream, String key, Long imageSize) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream cannot be null");
        }
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("S3 key cannot be null or empty");
        }
        if (imageSize == null || imageSize <= 0) {
            throw new IllegalArgumentException("Image size must be positive");
        }
    }
}
