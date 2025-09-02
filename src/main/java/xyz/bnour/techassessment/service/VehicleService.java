package xyz.bnour.techassessment.service;

import xyz.bnour.techassessment.entity.Image;
import xyz.bnour.techassessment.entity.Vehicle;
import xyz.bnour.techassessment.repository.ImageRepository;
import xyz.bnour.techassessment.repository.VehicleRepository;
import xyz.bnour.techassessment.response.VehicleResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final ImageRepository imageRepository;
    
    @Value("${xyz.bnour.tech-assessment.max-num-of-images-per-vehicle}")
    private Integer maxNumberOfImagesPerVehicle;

    public List<VehicleResponse> getAll() {
        log.info("Fetching all vehicles");
        List<Vehicle> vehicles = vehicleRepository.findAll();
        log.debug("Found {} vehicles in database", vehicles.size());
        
        List<VehicleResponse> responses = new ArrayList<>();

        for (Vehicle vehicle : vehicles) {
            List<Image> images = getVehicleImages(vehicle.getId());
            responses.add(new VehicleResponse(vehicle, images));
        }

        log.info("Successfully retrieved {} vehicles with their images", responses.size());
        return responses;
    }

    public VehicleResponse getById(String id) {
        log.info("Fetching vehicle by ID: {}", id);
        Vehicle vehicle = findVehicleById(id);
        List<Image> images = getVehicleImages(vehicle.getId());
        log.info("Successfully retrieved vehicle: {} {} with {} images", vehicle.getName(), vehicle.getModel(), images.size());
        return new VehicleResponse(vehicle, images);
    }

    @Transactional
    public Vehicle create(Vehicle vehicle) {
        log.info("Creating new vehicle: {} {}", vehicle.getName(), vehicle.getModel());
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Successfully created vehicle with ID: {}", savedVehicle.getId());
        return savedVehicle;
    }

    @Transactional
    public Vehicle update(Vehicle updatedVehicle) {
        log.info("Updating vehicle with ID: {}", updatedVehicle.getId());
        Vehicle existingVehicle = vehicleRepository.findById(updatedVehicle.getId())
                .orElseThrow(() -> {
                    log.error("Vehicle not found for update with ID: {}", updatedVehicle.getId());
                    return new EntityNotFoundException("Vehicle not found with ID: " + updatedVehicle.getId());
                });

        updateVehicleFields(existingVehicle, updatedVehicle);
        Vehicle savedVehicle = vehicleRepository.save(existingVehicle);
        log.info("Successfully updated vehicle: {} {}", savedVehicle.getName(), savedVehicle.getModel());
        return savedVehicle;
    }

    @Transactional
    public void deleteById(String id) {
        log.info("Deleting vehicle with ID: {}", id);
        UUID vehicleId = UUID.fromString(id);
        if (!vehicleRepository.existsById(vehicleId)) {
            log.error("Vehicle not found for deletion with ID: {}", id);
            throw new EntityNotFoundException("Vehicle not found with ID: " + id);
        }
        vehicleRepository.deleteById(vehicleId);
        log.info("Successfully deleted vehicle with ID: {}", id);
    }

    private Vehicle findVehicleById(String id) {
        log.debug("Looking up vehicle by ID: {}", id);
        return vehicleRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> {
                    log.error("Vehicle not found with ID: {}", id);
                    return new EntityNotFoundException("Vehicle not found with ID: " + id);
                });
    }

    private List<Image> getVehicleImages(UUID vehicleId) {
        log.debug("Fetching images for vehicle ID: {}", vehicleId);
        List<Image> images = imageRepository.findAllByVehicleId(vehicleId);

        if (images.size() > maxNumberOfImagesPerVehicle) {
            log.error("Vehicle {} has {} images, exceeding limit of {}", vehicleId, images.size(), maxNumberOfImagesPerVehicle);
            throw new RuntimeException("Vehicle " + vehicleId + " has " + images.size() + 
                    " images, exceeding limit of " + maxNumberOfImagesPerVehicle);
        }
        
        log.debug("Found {} images for vehicle ID: {}", images.size(), vehicleId);
        return images;
    }

    private void updateVehicleFields(Vehicle existing, Vehicle updated) {
        existing.setName(updated.getName());
        existing.setModel(updated.getModel());
        existing.setHorsepower(updated.getHorsepower());
        existing.setTrunkCapacity(updated.getTrunkCapacity());
        existing.setModelYear(updated.getModelYear());
        existing.setTransmission(updated.getTransmission());
        existing.setDrivetrain(updated.getDrivetrain());
        existing.setPrice(updated.getPrice());
    }
}
