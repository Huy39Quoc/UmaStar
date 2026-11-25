package umastar.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import umastar.enums.*;

@Entity
@Table(name = "transport_orders", indexes = {
        @Index(name = "idx_order_customer", columnList = "customer_id"),
        @Index(name = "idx_order_status", columnList = "status")
})
@Getter @Setter @NoArgsConstructor
public class TransportOrder extends BaseEntity {

    @Column(name = "order_code", nullable = false, unique = true, length = 30)
    private String orderCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // ---- Điểm đi ----
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origin_country_id", nullable = false)
    private Country originCountry;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origin_city_id", nullable = false)
    private City originCity;

    /** Địa chỉ chi tiết (trường đua, trại ngựa, sân bay...). */
    @Column(name = "origin_address", length = 255)
    private String originAddress;

    // ---- Điểm đến ----
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dest_country_id", nullable = false)
    private Country destCountry;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dest_city_id", nullable = false)
    private City destCity;

    @Column(name = "dest_address", length = 255)
    private String destAddress;

    /**
     * Quy tắc (validate ở service):
     * DOMESTIC      -> originCountry = destCountry = country có homeCountry = true
     * INTERNATIONAL -> destCountry phải có partner = true và khác nước sở tại;
     *                  city phải thuộc đúng country đã chọn.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "transport_type", nullable = false, length = 20)
    private TransportType transportType;

    @Column(name = "requested_departure", nullable = false)
    private Instant requestedDeparture;

    @Column(name = "requested_arrival")
    private Instant requestedArrival;

    @Column(name = "horse_count", nullable = false)
    private int horseCount;

    @Column(name = "special_requirements", columnDefinition = "text")
    private String specialRequirements;

    @Column(name = "quoted_price", precision = 15, scale = 2)
    private BigDecimal quotedPrice;

    @Column(name = "actual_cost", precision = 15, scale = 2)
    private BigDecimal actualCost;

    @Column(length = 3)
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "reject_reason", columnDefinition = "text")
    private String rejectReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderHorse> orderHorses = new ArrayList<>();

    @Version
    private Long version;
}
