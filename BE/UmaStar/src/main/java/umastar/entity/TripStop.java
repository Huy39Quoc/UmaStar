package umastar.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import umastar.enums.*;

@Entity
@Table(name = "trip_stops",
        uniqueConstraints = @UniqueConstraint(name = "uk_trip_stop_seq", columnNames = {"trip_id", "sequence_no"}))
@Getter @Setter @NoArgsConstructor
public class TripStop extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(name = "sequence_no", nullable = false)
    private int sequenceNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "stop_type", nullable = false, length = 20)
    private StopType stopType;

    /** Bắt buộc nhập khi stopType = OTHER (người dùng tự ghi loại điểm dừng). */
    @Column(name = "custom_type_name", length = 100)
    private String customTypeName;

    @Column(name = "location_name", nullable = false, length = 255)
    private String locationName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    private Double latitude;
    private Double longitude;

    @Column(name = "planned_arrival")
    private Instant plannedArrival;

    @Column(name = "actual_arrival")
    private Instant actualArrival;

    @Column(name = "planned_departure")
    private Instant plannedDeparture;

    @Column(name = "actual_departure")
    private Instant actualDeparture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StopStatus status = StopStatus.PENDING;
}
