package umastar.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

/**
 * Danh mục quốc gia.
 * - homeCountry = true  : nước sở tại (Việt Nam) -> dùng cho đơn NỘI ĐỊA
 * - partner     = true  : nước đã hợp tác        -> dùng cho đơn QUỐC TẾ
 */
@Entity
@Table(name = "countries",
        uniqueConstraints = @UniqueConstraint(name = "uk_countries_iso_code", columnNames = {"iso_code"}))
@Getter @Setter @NoArgsConstructor
public class Country extends BaseEntity {

    @Column(name = "iso_code", nullable = false, length = 2)
    private String isoCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "name_vi", length = 100)
    private String nameVi;

    @Column(name = "home_country", nullable = false)
    private boolean homeCountry = false;

    @Column(nullable = false)
    private boolean partner = false;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "country")
    private List<City> cities = new ArrayList<>();
}
