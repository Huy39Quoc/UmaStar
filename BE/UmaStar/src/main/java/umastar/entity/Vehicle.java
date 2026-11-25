package umastar.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import umastar.enums.*;

@Entity
@Table(name = "vehicles")
@Getter @Setter @NoArgsConstructor
public class Vehicle extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 20)
    private VehicleType vehicleType;

    @Column(name = "code_or_plate", nullable = false, unique = true, length = 50)
    private String codeOrPlate;

    @Column(name = "capacity_horses", nullable = false)
    private int capacityHorses;

    @Column(name = "carrier_name", length = 200)
    private String carrierName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleStatus status = VehicleStatus.AVAILABLE;
}
