package umastar.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.*;
import umastar.enums.*;

@Entity
@Table(name = "regulations", indexes = {
        @Index(name = "idx_regulation_country", columnList = "country_id")
})
@Getter @Setter @NoArgsConstructor
public class Regulation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    /** Thành phố / cửa khẩu / sân bay áp dụng quy định (tùy chọn, null nếu áp dụng toàn quốc). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @Column(name = "checkpoint_name", length = 200)
    private String checkpointName;

    @Enumerated(EnumType.STRING)
    @Column(name = "requirement_type", nullable = false, length = 20)
    private RequirementType requirementType;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "regulation_required_docs",
            joinColumns = @JoinColumn(name = "regulation_id"))
    @Column(name = "doc_type", length = 30)
    private Set<DocType> requiredDocTypes = new HashSet<>();

    @Column(name = "quarantine_days")
    private Integer quarantineDays;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;
}
