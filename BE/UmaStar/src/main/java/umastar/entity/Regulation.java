package umastar.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
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

    /** Danh sách loại giấy tờ bắt buộc, lưu gọn trong 1 cột của bảng regulations. */
    @Convert(converter = DocTypeSetConverter.class)
    @Column(name = "required_doc_types", columnDefinition = "text")
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

    @Converter
    static class DocTypeSetConverter implements AttributeConverter<Set<DocType>, String> {

        @Override
        public String convertToDatabaseColumn(Set<DocType> attribute) {
            if (attribute == null || attribute.isEmpty()) {
                return null;
            }
            return attribute.stream()
                    .map(Enum::name)
                    .sorted()
                    .collect(Collectors.joining(","));
        }

        @Override
        public Set<DocType> convertToEntityAttribute(String dbData) {
            if (dbData == null || dbData.isBlank()) {
                return new HashSet<>();
            }
            return Arrays.stream(dbData.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(DocType::valueOf)
                    .collect(Collectors.toCollection(HashSet::new));
        }
    }
}
