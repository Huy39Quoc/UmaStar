package umastar.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import umastar.enums.*;

@Entity
@Table(name = "route_change_requests", indexes = {
        @Index(name = "idx_rcr_trip", columnList = "trip_id"),
        @Index(name = "idx_rcr_status", columnList = "status")
})
@Getter @Setter @NoArgsConstructor
public class RouteChangeRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id")
    private Incident incident;

    @Column(nullable = false, columnDefinition = "text")
    private String reason;

    @Column(name = "old_route_summary", columnDefinition = "text")
    private String oldRouteSummary;

    @Column(name = "new_route_summary", columnDefinition = "text")
    private String newRouteSummary;

    @Column(name = "extra_cost", precision = 15, scale = 2)
    private BigDecimal extraCost;

    @Column(name = "new_eta")
    private Instant newEta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChangeRequestStatus status = ChangeRequestStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "decision_note", columnDefinition = "text")
    private String decisionNote;
}
