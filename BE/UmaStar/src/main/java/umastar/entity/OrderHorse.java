package umastar.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

/**
 * Chi tiết từng con ngựa tham gia trong một đơn vận chuyển (bảng trung gian N-N giữa {@link TransportOrder} và {@link Horse}).
 * Lưu thông tin riêng của con ngựa trong phạm vi đơn đó: số chuồng, ảnh minh chứng khỏe mạnh trước vận chuyển và ghi chú.
 */
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

    /** Ghi chú chăm sóc đặc biệt dành riêng cho con ngựa này trong đơn (khẩu phần ăn, tính khí...). */
    @Column(name = "special_note", columnDefinition = "text")
    private String specialNote;

    /** Ghi chú tình trạng sức khỏe trước khi vận chuyển do khách hàng khai báo lúc tạo đơn. */
    @Column(name = "pre_transport_note", columnDefinition = "text")
    private String preTransportNote;

    /** Danh sách URL ảnh chụp (Cloudinary) chứng minh ngựa khỏe mạnh trước vận chuyển, tối đa 5 ảnh/con. */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "order_horse_photos",
            joinColumns = @JoinColumn(name = "order_horse_id"))
    @Column(name = "photo_url", nullable = false, length = 500)
    private List<String> preTransportPhotoUrls = new ArrayList<>();
}
