package xyz.bnour.techassessment.service;

import xyz.bnour.techassessment.entity.Image;
import xyz.bnour.techassessment.entity.Vehicle;
import xyz.bnour.techassessment.repository.ImageRepository;
import xyz.bnour.techassessment.repository.VehicleRepository;
import xyz.bnour.techassessment.response.ImageResponse;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private ImageService imageService;

    private Vehicle testVehicle;
    private Image testImage;
    private UUID testVehicleId;
    private UUID testImageId;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(imageService, "maxNumberOfImagesPerVehicle", 2);
        
        testVehicleId = UUID.randomUUID();
        testImageId = UUID.randomUUID();
        
        testVehicle = new Vehicle();
        testVehicle.setId(testVehicleId);
        testVehicle.setName("Polo");
        
        testImage = new Image();
        testImage.setId(testImageId);
        testImage.setS3Key("test-s3-key.jpg");
        testImage.setUrl("https://bucket.s3.amazonaws.com/test-s3-key.jpg");
        testImage.setVehicle(testVehicle);
    }

    @Test
    void getAll_ShouldReturnImageResponseList() {
        List<Image> images = List.of(testImage);
        when(imageRepository.findAll()).thenReturn(images);

        List<ImageResponse> result = imageService.getAll();

        assertEquals(1, result.size());
        assertEquals(testImageId, result.get(0).getId());
        assertEquals(testVehicleId, result.get(0).getVehicleId());
        assertEquals("test-s3-key.jpg", result.get(0).getS3Key());
        
        verify(imageRepository).findAll();
    }

    @Test
    void getById_WithValidId_ShouldReturnImageResponse() {
        String imageIdStr = testImageId.toString();
        when(imageRepository.findById(testImageId)).thenReturn(Optional.of(testImage));

        ImageResponse result = imageService.getById(imageIdStr);

        assertNotNull(result);
        assertEquals(testImageId, result.getId());
        assertEquals(testVehicleId, result.getVehicleId());
        assertEquals("test-s3-key.jpg", result.getS3Key());
    }

    @Test
    void getById_WithInvalidId_ShouldThrowException() {
        String invalidId = UUID.randomUUID().toString();
        when(imageRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> imageService.getById(invalidId)
        );
        
        assertTrue(exception.getMessage().contains("Image not found"));
    }

    @Test
    void create_WithValidFile_ShouldCreateAndReturnImageResponse() throws IOException {
        String vehicleIdStr = testVehicleId.toString();
        MockMultipartFile mockFile = new MockMultipartFile(
            "image", 
            "test.jpg", 
            "image/jpeg", 
            "test image content".getBytes()
        );
        
        when(vehicleRepository.findById(testVehicleId)).thenReturn(Optional.of(testVehicle));
        when(imageRepository.findAllByVehicleId(testVehicleId)).thenReturn(List.of()); // No existing images
        when(s3Service.getImageUrl(anyString())).thenReturn("https://bucket.s3.amazonaws.com/generated-key.jpg");
        when(imageRepository.save(any(Image.class))).thenReturn(testImage);

        ImageResponse result = imageService.create(vehicleIdStr, mockFile);

        assertNotNull(result);
        verify(vehicleRepository).findById(testVehicleId);
        verify(imageRepository).findAllByVehicleId(testVehicleId);
        verify(s3Service).uploadImage(any(), anyString(), any());
        verify(s3Service).getImageUrl(anyString());
        verify(imageRepository).save(any(Image.class));
    }

    @Test
    void create_WithInvalidVehicleId_ShouldThrowException() {
        String invalidVehicleId = UUID.randomUUID().toString();
        MockMultipartFile mockFile = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test".getBytes());
        
        when(vehicleRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> imageService.create(invalidVehicleId, mockFile)
        );
        
        assertTrue(exception.getMessage().contains("Vehicle not found"));
        verify(s3Service, never()).uploadImage(any(), anyString(), any());
    }

    @Test
    void create_WhenImageLimitReached_ShouldThrowException() {
        String vehicleIdStr = testVehicleId.toString();
        MockMultipartFile mockFile = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test".getBytes());

        List<Image> existingImages = List.of(new Image(), new Image());
        
        when(vehicleRepository.findById(testVehicleId)).thenReturn(Optional.of(testVehicle));
        when(imageRepository.findAllByVehicleId(testVehicleId)).thenReturn(existingImages);

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> imageService.create(vehicleIdStr, mockFile)
        );
        
        assertTrue(exception.getMessage().contains("already has 2 images"));
        verify(s3Service, never()).uploadImage(any(), anyString(), any());
    }

    @Test
    void update_WithValidFile_ShouldUpdateImageResponse() throws IOException {
        String imageIdStr = testImageId.toString();
        MockMultipartFile mockFile = new MockMultipartFile("image", "updated.jpg", "image/jpeg", "updated".getBytes());
        
        when(imageRepository.findById(testImageId)).thenReturn(Optional.of(testImage));
        when(imageRepository.save(testImage)).thenReturn(testImage);

        ImageResponse result = imageService.update(imageIdStr, mockFile);

        assertNotNull(result);
        verify(s3Service).uploadImage(any(), eq("test-s3-key.jpg"), any());
        verify(imageRepository).save(testImage);
    }

    @Test
    void deleteById_WithValidId_ShouldCallRepository() {
        String imageIdStr = testImageId.toString();
        when(imageRepository.existsById(testImageId)).thenReturn(true);

        imageService.deleteById(imageIdStr);

        verify(imageRepository).existsById(testImageId);
        verify(imageRepository).deleteById(testImageId);
    }

    @Test
    void deleteById_WithInvalidId_ShouldThrowException() {
        String invalidId = UUID.randomUUID().toString();
        when(imageRepository.existsById(any(UUID.class))).thenReturn(false);

        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> imageService.deleteById(invalidId)
        );
        
        assertTrue(exception.getMessage().contains("Image not found"));
        verify(imageRepository, never()).deleteById(any(UUID.class));
    }
}
