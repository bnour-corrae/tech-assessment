package xyz.bnour.techassessment.controller;

import xyz.bnour.techassessment.entity.Vehicle;
import xyz.bnour.techassessment.response.VehicleResponse;
import xyz.bnour.techassessment.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
@Slf4j
public class VehicleController {
    private final VehicleService vehicleService;

    @GetMapping
    public ResponseEntity<List<VehicleResponse>> getAll() {
        log.info("GET /vehicles - Fetching all vehicles");
        List<VehicleResponse> vehicles = vehicleService.getAll();
        log.info("GET /vehicles - Returning {} vehicles", vehicles.size());
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getById(@PathVariable String id) {
        log.info("GET /vehicles/{} - Fetching vehicle by ID", id);
        VehicleResponse vehicle = vehicleService.getById(id);
        log.info("GET /vehicles/{} - Successfully retrieved vehicle: {} {}", id, 
                vehicle.getVehicle().getName(), vehicle.getVehicle().getModel());
        return ResponseEntity.ok(vehicle);
    }

    @PostMapping
    public ResponseEntity<Vehicle> create(@Valid @RequestBody Vehicle vehicle) {
        log.info("POST /vehicles - Creating new vehicle: {} {}", vehicle.getName(), vehicle.getModel());
        Vehicle createdVehicle = vehicleService.create(vehicle);
        log.info("POST /vehicles - Successfully created vehicle with ID: {}", createdVehicle.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVehicle);
    }

    @PutMapping
    public ResponseEntity<Vehicle> update(@Valid @RequestBody Vehicle vehicle) {
        log.info("PUT /vehicles - Updating vehicle with ID: {}", vehicle.getId());
        Vehicle updatedVehicle = vehicleService.update(vehicle);
        log.info("PUT /vehicles - Successfully updated vehicle: {} {}", 
                updatedVehicle.getName(), updatedVehicle.getModel());
        return ResponseEntity.ok(updatedVehicle);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable String id) {
        log.info("DELETE /vehicles/{} - Deleting vehicle", id);
        vehicleService.deleteById(id);
        log.info("DELETE /vehicles/{} - Successfully deleted vehicle", id);
        return ResponseEntity.noContent().build();
    }
}
