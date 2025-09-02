package xyz.bnour.vehiclecatalog.response;

import xyz.bnour.vehiclecatalog.entity.Image;
import xyz.bnour.vehiclecatalog.entity.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponse {
    private Vehicle vehicle;
    private List<Image> images;
}
