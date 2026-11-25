package umastar.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import umastar.enums.*;

@Entity
@Table(name = "trips", indexes = {
        @Index(name = "idx_trip_order", columnList = "order_id"),
        @Index(name = "idx_trip_vehicle", columnList = "vehicle_id"),
        @Index(name = "idx_trip_status", columnList = "status")
})
@Getter @Setter @NoArgsConstructor
public class Trip extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private TransportOrder order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    /** Thứ tự chặng nếu một đơn hàng gồm nhiều chuyến (nội địa -> bay -> nội địa). */
    @Column(name = "sequence_no", nullable = false)
    private int sequenceNo = 1;

    /** Mã chuyến bay (dùng cho chặng hàng không quốc tế). */
    @Column(name = "flight_number", length = 50)
    private String flightNumber;

    @Column(name = "planned_start")
    private Instant plannedStart;

    @Column(name = "planned_end")
    private Instant plannedEnd;

    @Column(name = "actual_start")
    private Instant actualStart;

    @Column(name = "actual_end")
    private Instant actualEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TripStatus status = TripStatus.PLANNED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planned_by")
    private User plannedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordinator_id")
    private User coordinator;

    @Column(name = "current_lat")
    private Double currentLat;

    @Column(name = "current_lng")
    private Double currentLng;

    @Column(name = "last_location_at")
    private Instant lastLocationAt;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceNo ASC")
    private List<TripStop> stops = new ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripAssignment> assignments = new ArrayList<>();
}
