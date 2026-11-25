package umastar.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import umastar.enums.*;

@Entity
@Table(name = "cities",
        uniqueConstraints = @UniqueConstraint(name = "uk_city_country_name", columnNames = {"country_id", "name"}),
        indexes = @Index(name = "idx_city_country", columnList = "country_id"))
@Getter @Setter @NoArgsConstructor
public class City extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "name_vi", length = 150)
    private String nameVi;

    /** Tỉnh / bang / vùng (tùy chọn). */
    @Column(length = 150)
    private String region;

    /** IANA timezone, vd: Asia/Ho_Chi_Minh, Asia/Tokyo. Dùng để hiển thị giờ địa phương. */
    @Column(nullable = false, length = 60)
    private String timezone;

    @Column(name = "has_airport", nullable = false)
    private boolean hasAirport = false;

    @Column(nullable = false)
    private boolean active = true;
}
