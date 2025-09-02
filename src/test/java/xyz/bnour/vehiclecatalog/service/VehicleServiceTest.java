package xyz.bnour.vehiclecatalog.service;

import xyz.bnour.vehiclecatalog.entity.Image;
import xyz.bnour.vehiclecatalog.entity.Vehicle;
import xyz.bnour.vehiclecatalog.repository.ImageRepository;
import xyz.bnour.vehiclecatalog.repository.VehicleRepository;
import xyz.bnour.vehiclecatalog.response.VehicleResponse;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private Vehicle testVehicle;
    private UUID testVehicleId;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(vehicleService, "maxNumberOfImagesPerVehicle", 2);
        
        testVehicleId = UUID.randomUUID();
        testVehicle = new Vehicle();
        testVehicle.setId(testVehicleId);
        testVehicle.setName("Polo");
        testVehicle.setModel("GTI");
        testVehicle.setModelYear(2024);
        testVehicle.setPrice(new BigDecimal("50000.00"));
        testVehicle.setHorsepower(200);
    }

    @Test
    void getAll_ShouldReturnVehicleResponseList() {
        List<Vehicle> vehicles = List.of(testVehicle);
        List<Image> images = List.of();
        
        when(vehicleRepository.findAll()).thenReturn(vehicles);
        when(imageRepository.findAllByVehicleId(testVehicleId)).thenReturn(images);

        List<VehicleResponse> result = vehicleService.getAll();

        assertEquals(1, result.size());
        assertEquals("Polo", result.get(0).getVehicle().getName());
        assertEquals(0, result.get(0).getImages().size());
        
        verify(vehicleRepository).findAll();
        verify(imageRepository).findAllByVehicleId(testVehicleId);
    }

    @Test
    void getById_WithValidId_ShouldReturnVehicleResponse() {
        String vehicleIdStr = testVehicleId.toString();
        List<Image> images = List.of();
        
        when(vehicleRepository.findById(testVehicleId)).thenReturn(Optional.of(testVehicle));
        when(imageRepository.findAllByVehicleId(testVehicleId)).thenReturn(images);

        VehicleResponse result = vehicleService.getById(vehicleIdStr);

        assertNotNull(result);
        assertEquals("Polo", result.getVehicle().getName());
        assertEquals("GTI", result.getVehicle().getModel());
    }

    @Test
    void getById_WithInvalidId_ShouldThrowException() {
        String invalidId = UUID.randomUUID().toString();
        when(vehicleRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> vehicleService.getById(invalidId)
        );
        
        assertTrue(exception.getMessage().contains("Vehicle not found"));
    }

    @Test
    void create_ShouldSaveAndReturnVehicle() {
        when(vehicleRepository.save(testVehicle)).thenReturn(testVehicle);

        Vehicle result = vehicleService.create(testVehicle);

        assertEquals(testVehicle, result);
        verify(vehicleRepository).save(testVehicle);
    }

    @Test
    void update_WithValidVehicle_ShouldUpdateAndReturn() {
        Vehicle updatedVehicle = new Vehicle();
        updatedVehicle.setId(testVehicleId);
        updatedVehicle.setName("Updated Polo");
        updatedVehicle.setModel("Updated GTI");
        updatedVehicle.setPrice(new BigDecimal("60000.00"));
        
        when(vehicleRepository.findById(testVehicleId)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(testVehicle);

        Vehicle result = vehicleService.update(updatedVehicle);

        verify(vehicleRepository).findById(testVehicleId);
        verify(vehicleRepository).save(testVehicle);
        assertEquals("Updated Polo", testVehicle.getName());
        assertEquals("Updated GTI", testVehicle.getModel());
    }

    @Test
    void deleteById_WithValidId_ShouldCallRepository() {
        String vehicleIdStr = testVehicleId.toString();
        when(vehicleRepository.existsById(testVehicleId)).thenReturn(true);

        vehicleService.deleteById(vehicleIdStr);

        verify(vehicleRepository).existsById(testVehicleId);
        verify(vehicleRepository).deleteById(testVehicleId);
    }

    @Test
    void deleteById_WithInvalidId_ShouldThrowException() {
        String invalidId = UUID.randomUUID().toString();
        when(vehicleRepository.existsById(any(UUID.class))).thenReturn(false);

        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> vehicleService.deleteById(invalidId)
        );
        
        assertTrue(exception.getMessage().contains("Vehicle not found"));
        verify(vehicleRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void getAll_WithImageLimitExceeded_ShouldThrowException() {
        List<Vehicle> vehicles = List.of(testVehicle);
        List<Image> tooManyImages = List.of(new Image(), new Image(), new Image());
        
        when(vehicleRepository.findAll()).thenReturn(vehicles);
        when(imageRepository.findAllByVehicleId(testVehicleId)).thenReturn(tooManyImages);

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> vehicleService.getAll()
        );
        
        assertTrue(exception.getMessage().contains("exceeding limit"));
    }
}
