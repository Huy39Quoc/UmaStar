package umastar.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import umastar.enums.*;

@Entity
@Table(name = "trip_status_logs", indexes = {
        @Index(name = "idx_tsl_trip", columnList = "trip_id, reported_at")
})
@Getter @Setter @NoArgsConstructor
public class TripStatusLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stop_id")
    private TripStop stop;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TripLogStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reported_by", nullable = false)
    private User reportedBy;

    @Column(name = "reported_at", nullable = false)
    private Instant reportedAt = Instant.now();

    private Double latitude;
    private Double longitude;

    @Column(name = "is_manual", nullable = false)
    private boolean manual = true;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(columnDefinition = "text")
    private String note;
}
