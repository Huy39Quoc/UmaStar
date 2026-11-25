package umastar.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import umastar.enums.*;

@Entity
@Table(name = "order_horses",
        uniqueConstraints = @UniqueConstraint(name = "uk_order_horse", columnNames = {"order_id", "horse_id"}))
@Getter @Setter @NoArgsConstructor
public class OrderHorse extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private TransportOrder order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "horse_id", nullable = false)
    private Horse horse;

    @Column(name = "stall_number", length = 20)
    private String stallNumber;

    @Column(name = "special_note", columnDefinition = "text")
    private String specialNote;
}
