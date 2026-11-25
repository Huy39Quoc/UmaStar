package umastar.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity
@Table(name = "handovers")
@Getter @Setter @NoArgsConstructor
public class Handover extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false, unique = true)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private TransportOrder order;

    @Column(name = "receiver_name", nullable = false, length = 150)
    private String receiverName;

    @Column(name = "receiver_phone", length = 30)
    private String receiverPhone;

    @Column(name = "handover_location", length = 255)
    private String handoverLocation;

    @Column(name = "horse_condition_on_arrival", columnDefinition = "text")
    private String horseConditionOnArrival;

    @Column(name = "signature_url", length = 500)
    private String signatureUrl;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handed_over_by")
    private User handedOverBy;

    @Column(name = "handed_over_at")
    private Instant handedOverAt;

    @Column(name = "confirmed_by_customer", nullable = false)
    private boolean confirmedByCustomer = false;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    private Short rating;

    @Column(columnDefinition = "text")
    private String feedback;
}
