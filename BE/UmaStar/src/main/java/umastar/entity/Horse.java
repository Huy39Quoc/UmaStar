package umastar.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import umastar.enums.*;

@Entity
@Table(name = "horses", indexes = {
        @Index(name = "idx_horse_customer", columnList = "customer_id")
})
@Getter @Setter @NoArgsConstructor
public class Horse extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "microchip_no", nullable = false, unique = true, length = 50)
    private String microchipNo;

    @Column(name = "passport_no", length = 50)
    private String passportNo;

    @Column(length = 100)
    private String breed;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private HorseSex sex;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(length = 50)
    private String color;

    @Column(name = "weight_kg", precision = 7, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "health_notes", columnDefinition = "text")
    private String healthNotes;
}
