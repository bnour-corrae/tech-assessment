CREATE TABLE image
(
    id         UUID         NOT NULL,
    s3_key     VARCHAR(255) NOT NULL,
    vehicle_id UUID         NOT NULL,
    url        VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_image PRIMARY KEY (id)
);

CREATE TABLE vehicle
(
    id             UUID           NOT NULL,
    name           VARCHAR(255)   NOT NULL,
    model          VARCHAR(255)   NOT NULL,
    horsepower     INTEGER,
    trunk_capacity INTEGER,
    model_year     INTEGER        NOT NULL,
    transmission   VARCHAR(255),
    drivetrain     VARCHAR(255),
    price          DECIMAL(10, 2) NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_vehicle PRIMARY KEY (id)
);

ALTER TABLE image
    ADD CONSTRAINT FK_IMAGE_ON_VEHICLE FOREIGN KEY (vehicle_id) REFERENCES vehicle (id);
