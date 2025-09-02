package xyz.bnour.vehiclecatalog.response;

import xyz.bnour.vehiclecatalog.entity.Image;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponse {
    private UUID id;
    private String s3Key;
    private UUID vehicleId;
    private String url;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ImageResponse(Image image) {
        this.id = image.getId();
        this.s3Key = image.getS3Key();
        this.vehicleId = image.getVehicle().getId();
        this.url = image.getUrl();
        this.createdAt = image.getCreatedAt();
        this.updatedAt = image.getUpdatedAt();
    }
}
