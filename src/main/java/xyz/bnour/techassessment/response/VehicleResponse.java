package xyz.bnour.techassessment.response;

import xyz.bnour.techassessment.entity.Image;
import xyz.bnour.techassessment.entity.Vehicle;
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
