package umastar.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import umastar.enums.*;

@Entity
@Table(name = "legal_documents", indexes = {
        @Index(name = "idx_legaldoc_order", columnList = "order_id"),
        @Index(name = "idx_legaldoc_horse", columnList = "horse_id"),
        @Index(name = "idx_legaldoc_status", columnList = "approval_status")
})
@Getter @Setter @NoArgsConstructor
public class LegalDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private TransportOrder order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "horse_id", nullable = false)
    private Horse horse;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false, length = 30)
    private DocType docType;

    /** Bắt buộc nhập khi docType = OTHER. */
    @Column(name = "doc_type_other", length = 150)
    private String docTypeOther;

    @Column(name = "doc_number", length = 100)
    private String docNumber;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private User verifiedBy;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private IssuingAgency agency;

    @Column(name = "agency_ref_no", length = 100)
    private String agencyRefNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    private ApprovalStatus approvalStatus = ApprovalStatus.MISSING;

    @Column(name = "reject_reason", columnDefinition = "text")
    private String rejectReason;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Version
    private Long version;
}
