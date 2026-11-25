package umastar.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import umastar.enums.*;

@Entity
@Table(name = "trip_assignments",
        uniqueConstraints = @UniqueConstraint(name = "uk_trip_user_role", columnNames = {"trip_id", "user_id", "assignment_role"}))
@Getter @Setter @NoArgsConstructor
public class TripAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_role", nullable = false, length = 20)
    private AssignmentRole assignmentRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt = Instant.now();
}
