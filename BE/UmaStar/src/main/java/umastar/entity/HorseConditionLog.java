package umastar.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.*;
import umastar.enums.*;

@Entity
@Table(name = "horse_condition_logs", indexes = {
        @Index(name = "idx_hcl_trip", columnList = "trip_id"),
        @Index(name = "idx_hcl_horse", columnList = "horse_id")
})
@Getter @Setter @NoArgsConstructor
public class HorseConditionLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "horse_id", nullable = false)
    private Horse horse;

    @Enumerated(EnumType.STRING)
    @Column(name = "horse_condition", nullable = false, length = 20)
    private HorseCondition condition;

    @Column(columnDefinition = "text")
    private String note;

    /** Danh sách ảnh tình trạng ngựa (tối đa 5 ảnh). */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "horse_condition_photos",
            joinColumns = @JoinColumn(name = "log_id"))
    @Column(name = "photo_url", length = 500)
    private List<String> photoUrls = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recorded_by", nullable = false)
    private User recordedBy;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt = Instant.now();
}
