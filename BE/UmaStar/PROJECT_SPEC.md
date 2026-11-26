# Hệ thống Quản lý Vận chuyển Ngựa đua Xuyên Quốc gia
**Cross-Border Racehorse Transport System (UmaStar)**

---

## Actors
1. **Logistics Manager** (Quản lý Điều hành Logistics)
2. **Transport Specialist** (Chuyên viên Thủ tục & Kiểm dịch)
3. **Fleet & Route Coordinator** (Điều phối viên Đội xe & Lộ trình)
4. **Vehicle Driver / Escort** — tách thành 2 role trong hệ thống: **Driver** (Tài xế) và **Escort** (Nhân viên đi kèm)
5. **Customer** (Khách hàng - CLB / Chủ ngựa)

---

## Main Features

### 1. Logistics Manager (Quản lý Điều hành Logistics)
- Tiếp nhận và phê duyệt các đơn hàng vận chuyển ngựa đua trong nước và quốc tế.
- Lập kế hoạch tổng thể cho chuyến vận chuyển (chọn phương tiện, hãng hàng không, tuyến đường, điểm trung chuyển).
- Phân công nhiệm vụ cho Chuyên viên thủ tục, Điều phối viên và Tài xế/Nhân viên đi kèm.
- Xử lý và phê duyệt các phương án thay đổi lộ trình hoặc chi phí phát sinh trong trường hợp khẩn cấp.
- Xem báo cáo doanh thu, chi phí vận hành và chỉ số hiệu suất hoàn thành chuyến đi (On-time delivery).

### 2. Transport Specialist (Chuyên viên Thủ tục & Kiểm dịch)
- Quản lý danh mục quy định y tế, kiểm dịch và hải quan của từng quốc gia/cửa khẩu.
- Khởi tạo và lưu trữ bộ hồ sơ vận chuyển số hóa cho từng con ngựa (Giấy chứng nhận tiêm phòng, Hộ chiếu ngựa, Giấy phép xuất nhập cảnh).
- Theo dõi tiến độ phê duyệt hồ sơ từ các cơ quan chức năng (Bộ Nông nghiệp, Hải quan, Đơn vị kiểm dịch).
- Gửi thông báo và hướng dẫn khách hàng bổ sung các giấy tờ pháp lý còn thiếu trước ngày khởi hành.

### 3. Fleet & Route Coordinator (Điều phối viên Đội xe & Lộ trình)
- Quản lý danh sách phương tiện chuyên dụng (xe tải chở ngựa, khoang máy bay, thùng vận chuyển - Stalls).
- Xây dựng lộ trình di chuyển tối ưu, xác định các điểm dừng nghỉ, trạm kiểm dịch và trạm tiếp nhiên liệu.
- Cập nhật thủ công hoặc theo dõi trạng thái tiến độ chuyến đi theo từng chặng (Khởi hành, Đến điểm dừng, Đã thông quan, Đã giao).
- Điều chỉnh lịch trình và gửi cảnh báo khi phát sinh sự cố giao thông, thời tiết xấu hoặc tắc nghẽn cửa khẩu.

### 4. Vehicle Driver / Escort (Tài xế / Nhân viên đi kèm)
- Xem lịch trình chuyến đi, danh sách ngựa phụ trách và danh sách điểm dừng nghỉ được phân công.
- Báo cáo trạng thái chuyến đi thủ công tại mỗi điểm mốc (Đã xuất phát, Đã tới điểm dừng, Đã hoàn thành thông quan).
- Ghi nhận nhật ký trạng thái của ngựa trong suốt chuyến đi (Sức khỏe ổn định, Bỏ ăn, Căng thẳng, Khí hậu thay đổi) bằng văn bản và hình ảnh.
- Gửi báo cáo sự cố khẩn cấp (Hỏng xe, Kẹt xe kéo dài, Sự cố y tế của ngựa) về trung tâm điều hành.

### 5. Customer (Khách hàng - CLB / Chủ ngựa)
- Tạo yêu cầu đặt dịch vụ vận chuyển ngựa đua (Địa điểm đi/đến, Thời gian, Số lượng ngựa, Yêu cầu đặc biệt).
- Tải lên các giấy tờ lý lịch và hồ sơ y tế của ngựa theo yêu cầu của đơn vị vận chuyển.
- Theo dõi tiến độ vị trí chuyến đi và trạng thái cập nhật theo từng chặng thời gian thực.
- Nhận thông báo khi ngựa hoàn tất thông quan hoặc đã tới địa điểm bàn giao an toàn.

---

## Core Business Flows
- **Flow 1:** Luồng Tạo & Phê duyệt Yêu cầu Vận chuyển
- **Flow 2:** Luồng Quản lý Hồ sơ Pháp lý & Kiểm dịch Thông quan
- **Flow 3:** Luồng Lập Kế hoạch Lộ trình & Điều phối Phương tiện
- **Flow 4:** Luồng Cập nhật Trạng thái & Nhật ký Lộ trình
- **Flow 5:** Luồng Xử lý Sự cố & Điều chỉnh Lộ trình Khẩn cấp
- **Flow 6:** Luồng Bàn giao & Nghiệm thu Chuyến đi

---

## Toàn cảnh Backend hiện tại (Auto-generated)

<!-- AUTO-GENERATED-BACKEND:START -->
> Phần này được tự động sinh từ toàn bộ mã nguồn trong `src/main/java/umastar/**` và `src/main/resources/**` bằng `java scripts/SyncDocs.java`.

### Kiến trúc Package & File hiện có

| Package | Loại | Tên | Mô tả |
|---|---|---|---|
| `umastar` | `class` | [`UmaStarApplication`](src/main/java/umastar/UmaStarApplication.java) | `@SpringBootApplication` |
| `umastar.config` | `class` | [`OpenApiConfig`](src/main/java/umastar/config/OpenApiConfig.java) | `@Configuration` |
| `umastar.config` | `class` | [`SecurityConfig`](src/main/java/umastar/config/SecurityConfig.java) | Cau hinh Spring Security co ban (stateless JWT, mo Swagger/Auth, tra JSON cho 401/403). |
| `umastar.dto.response` | `record` | [`ErrorResponse`](src/main/java/umastar/dto/response/ErrorResponse.java) | - |
| `umastar.dto.response` | `record` | [`PageResponse`](src/main/java/umastar/dto/response/PageResponse.java) | - |
| `umastar.entity` | `class` | [`BaseEntity`](src/main/java/umastar/entity/BaseEntity.java) | `@MappedSuperclass`, `@Getter`, `@Setter` |
| `umastar.entity` | `class` | [`City`](src/main/java/umastar/entity/City.java) | `@Entity`, `@Table`, `@Getter @Setter @NoArgsConstructor` |
| `umastar.entity` | `class` | [`Country`](src/main/java/umastar/entity/Country.java) | `@Entity`, `@Table`, `@Getter @Setter @NoArgsConstructor` |
| `umastar.entity` | `class` | [`Customer`](src/main/java/umastar/entity/Customer.java) | `@Entity`, `@Table`, `@Getter @Setter @NoArgsConstructor` |
| `umastar.entity` | `class` | [`Handover`](src/main/java/umastar/entity/Handover.java) | `@Entity`, `@Table`, `@Getter @Setter @NoArgsConstructor` |
| `umastar.entity` | `class` | [`Horse`](src/main/java/umastar/entity/Horse.java) | `@Entity`, `@Table`, `@Index`, `@Getter @Setter @NoArgsConstructor` |
| `umastar.entity` | `class` | [`HorseConditionLog`](src/main/java/umastar/entity/HorseConditionLog.java) | `@Entity`, `@Table`, `@Index`, `@Index`, `@Getter @Setter @NoArgsConstructor` |
| `umastar.entity` | `class` | [`Incident`](src/main/java/umastar/entity/Incident.java) | `@Entity`, `@Table`, `@Index`, `@Index`, `@Getter @Setter @NoArgsConstructor` |
| `umastar.entity` | `class` | [`LegalDocument`](src/main/java/umastar/entity/LegalDocument.java) | `@Entity`, `@Table`, `@Index`, `@Index`, `@Index`, `@Getter @Setter @NoArgsConstructor` |
| `umastar.entity` | `class` | [`Notification`](src/main/java/umastar/entity/Notification.java) | `@Entity`, `@Table`, `@Index`, `@Getter @Setter @NoArgsConstructor` |
| `umastar.entity` | `class` | [`OrderHorse`](src/main/java/umastar/entity/OrderHorse.java) | `@Entity`, `@Table`, `@Getter @Setter @NoArgsConstructor` |
| `umastar.entity` | `class` | [`RefreshToken`](src/main/java/umastar/entity/RefreshToken.java) | `@Entity`, `@Table`, `@Index`, `@Index`, `@Index`, `@Getter @Setter @NoArgsConstructor` |
| `umastar.entity` | `class` | [`Regulation`](src/main/java/umastar/entity/Regulation.java) | `@Entity`, `@Table`, `@Index`, `@Getter @Setter @NoArgsConstructor` |
| `umastar.entity` | `class` | [`RouteChangeRequest`](src/main/java/umastar/entity/RouteChangeRequest.java) | `@Entity`, `@Table`, `@Index`, `@Index`, `@Getter @Setter @NoArgsConstructor` |
| `umastar.entity` | `class` | [`TransportOrder`](src/main/java/umastar/entity/TransportOrder.java) | `@Entity`, `@Table`, `@Index`, `@Index`, `@Getter @Setter @NoArgsConstructor` |
| `umastar.entity` | `class` | [`Trip`](src/main/java/umastar/entity/Trip.java) | `@Entity`, `@Table`, `@Index`, `@Index`, `@Index`, `@Getter @Setter @NoArgsConstructor` |
| `umastar.entity` | `class` | [`TripAssignment`](src/main/java/umastar/entity/TripAssignment.java) | `@Entity`, `@Table`, `@Getter @Setter @NoArgsConstructor` |
| `umastar.entity` | `class` | [`TripStatusLog`](src/main/java/umastar/entity/TripStatusLog.java) | `@Entity`, `@Table`, `@Index`, `@Getter @Setter @NoArgsConstructor` |
| `umastar.entity` | `class` | [`TripStop`](src/main/java/umastar/entity/TripStop.java) | `@Entity`, `@Table`, `@Getter @Setter @NoArgsConstructor` |
| `umastar.entity` | `class` | [`User`](src/main/java/umastar/entity/User.java) | `@Entity`, `@Table`, `@UniqueConstraint`, `@UniqueConstraint`, `@Getter @Setter @NoArgsConstructor` |
| `umastar.entity` | `class` | [`Vehicle`](src/main/java/umastar/entity/Vehicle.java) | `@Entity`, `@Table`, `@Getter @Setter @NoArgsConstructor` |
| `umastar.enums` | `enum` | [`ApprovalStatus`](src/main/java/umastar/enums/ApprovalStatus.java) | - |
| `umastar.enums` | `enum` | [`AssignmentRole`](src/main/java/umastar/enums/AssignmentRole.java) | - |
| `umastar.enums` | `enum` | [`ChangeRequestStatus`](src/main/java/umastar/enums/ChangeRequestStatus.java) | - |
| `umastar.enums` | `enum` | [`CustomerType`](src/main/java/umastar/enums/CustomerType.java) | - |
| `umastar.enums` | `enum` | [`DocType`](src/main/java/umastar/enums/DocType.java) | - |
| `umastar.enums` | `enum` | [`HorseCondition`](src/main/java/umastar/enums/HorseCondition.java) | - |
| `umastar.enums` | `enum` | [`HorseSex`](src/main/java/umastar/enums/HorseSex.java) | - |
| `umastar.enums` | `enum` | [`IncidentStatus`](src/main/java/umastar/enums/IncidentStatus.java) | - |
| `umastar.enums` | `enum` | [`IncidentType`](src/main/java/umastar/enums/IncidentType.java) | - |
| `umastar.enums` | `enum` | [`IssuingAgency`](src/main/java/umastar/enums/IssuingAgency.java) | - |
| `umastar.enums` | `enum` | [`NotificationType`](src/main/java/umastar/enums/NotificationType.java) | - |
| `umastar.enums` | `enum` | [`OrderStatus`](src/main/java/umastar/enums/OrderStatus.java) | - |
| `umastar.enums` | `enum` | [`RequirementType`](src/main/java/umastar/enums/RequirementType.java) | - |
| `umastar.enums` | `enum` | [`Role`](src/main/java/umastar/enums/Role.java) | - |
| `umastar.enums` | `enum` | [`Severity`](src/main/java/umastar/enums/Severity.java) | - |
| `umastar.enums` | `enum` | [`StopStatus`](src/main/java/umastar/enums/StopStatus.java) | - |
| `umastar.enums` | `enum` | [`StopType`](src/main/java/umastar/enums/StopType.java) | - |
| `umastar.enums` | `enum` | [`TransportType`](src/main/java/umastar/enums/TransportType.java) | - |
| `umastar.enums` | `enum` | [`TripLogStatus`](src/main/java/umastar/enums/TripLogStatus.java) | - |
| `umastar.enums` | `enum` | [`TripStatus`](src/main/java/umastar/enums/TripStatus.java) | - |
| `umastar.enums` | `enum` | [`UserStatus`](src/main/java/umastar/enums/UserStatus.java) | - |
| `umastar.enums` | `enum` | [`VehicleStatus`](src/main/java/umastar/enums/VehicleStatus.java) | - |
| `umastar.enums` | `enum` | [`VehicleType`](src/main/java/umastar/enums/VehicleType.java) | - |
| `umastar.exception` | `class` | [`AppException`](src/main/java/umastar/exception/AppException.java) | Loi nghiep vu co kiem soat. Dung: throw new AppException(ErrorCode.HORSE_NOT_FOUND); throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, "Ngày khởi hành phải sau hôm nay ít nhất 14 ngày"); Ban thong bao rieng chi dung khi can chi tiet dong, con lai dung message mac dinh cua ErrorCode. |
| `umastar.exception` | `enum` | [`ErrorCode`](src/main/java/umastar/exception/ErrorCode.java) | Ma loi thong nhat. Moi loi gom: ma so (code), thong bao hien thi cho nguoi dung, HTTP status. QUY UOC DAI MA (moi nhom 100 so, khong de trung): 100 AUTH \| 200 TOKEN \| 300 USER \| 400 PASSWORD \| 500 FILE \| 600 SYSTEM 700 MASTER DATA (Country, City) \| 800 VEHICLE \| 900 REGULATION 1000 CUSTOMER \| 1100 HORSE \| 1200 NOTIFICATION (du kien) 1300 ORDER \| 1400 LEGAL_DOCUMENT \| 1500 TRIP \| 1600 TRIP_STOP, ASSIGNMENT 1700 INCIDENT, ROUTE_CHANGE \| 1800 HANDOVER \| 1900 REPORT Them loi moi: chon so tiep theo trong dung nhom. ErrorCodeTest se bao loi neu bi trung so. |
| `umastar.exception` | `class` | [`GlobalExceptionHandler`](src/main/java/umastar/exception/GlobalExceptionHandler.java) | Bo hung loi tap trung. Moi loi -> ErrorResponse thong nhat. Quy tac: loi cua client (4xx) khong log stack trace; loi he thong (5xx) luon log day du. |
| `umastar.util` | `class` | [`PageableFactory`](src/main/java/umastar/util/PageableFactory.java) | - |
| `umastar.util` | `class` | [`SecureTokenGenerator`](src/main/java/umastar/util/SecureTokenGenerator.java) | - |
| `umastar.util` | `class` | [`TokenHasher`](src/main/java/umastar/util/TokenHasher.java) | - |

### API Endpoints (Controllers)

_Chưa có Controller nào trong mã nguồn._

### Business Services

_Chưa có Service nào trong mã nguồn._

### Repositories

_Chưa có Repository nào trong mã nguồn._

### Resources & Cấu hình

- `src/main/resources/application.properties`
<!-- AUTO-GENERATED-BACKEND:END -->

