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
| `umastar.config` | `class` | [`CorsConfig`](src/main/java/umastar/config/CorsConfig.java) | CORS cho phep frontend (trinh duyet) goi API. Chi cac origin liet ke trong CORS_ALLOWED_ORIGINS (cach nhau bang dau phay) moi duoc phep; de trong nghia la tu choi moi origin. Khong cho dung " " va khong bat allowCredentials: xac thuc bang header Authorization (Bearer), khong phu thuoc cookie nen khong can. |
| `umastar.config` | `class` | [`DataSeeder`](src/main/java/umastar/config/DataSeeder.java) | Tao tai khoan mac dinh cho tung Role tu .env khi he thong chua co user nao thuoc role do. - Moi role chi seed neu duoc cau hinh du email & password trong .env (de trong se tu bo qua). - Rieng role CUSTOMER se tao kem ho so Customer (1-1) de dat don/them ngua duoc ngay. - An toan khi khoi dong lai: da co user cua role do thi bo qua. Khong ghi mat khau ra log. - User va ho so Customer duoc luu trong CUNG MOT transaction: loi giua chung thi khong de lai user mo coi (neu khong, lan khoi dong sau thay da co role CUSTOMER se bo qua va khong bao gio tao lai ho so). |
| `umastar.config` | `class` | [`OpenApiConfig`](src/main/java/umastar/config/OpenApiConfig.java) | `@Configuration` |
| `umastar.config` | `class` | [`SecurityConfig`](src/main/java/umastar/config/SecurityConfig.java) | Cau hinh Spring Security (stateless JWT, mo Swagger/Auth, tra JSON cho 401/403). |
| `umastar.config` | `class` | [`SeedProperties`](src/main/java/umastar/config/SeedProperties.java) | Cau hinh danh sach tai khoan seed theo tung Role tu application.properties / .env. |
| `umastar.controller` | `class` | [`AdminCityController`](src/main/java/umastar/controller/AdminCityController.java) | API quản trị danh mục thành phố (chỉ dành cho Logistics Manager). |
| `umastar.controller` | `class` | [`AdminCountryController`](src/main/java/umastar/controller/AdminCountryController.java) | API quản trị danh mục quốc gia (chỉ dành cho Logistics Manager). |
| `umastar.controller` | `class` | [`AuthController`](src/main/java/umastar/controller/AuthController.java) | API xác thực & tài khoản. Chỉ nhận/trả DTO, mọi nghiệp vụ nằm ở {@link AuthService}. |
| `umastar.controller` | `class` | [`CountryController`](src/main/java/umastar/controller/CountryController.java) | API danh mục quốc gia và thành phố (mọi người dùng đã đăng nhập đều xem được). |
| `umastar.controller` | `class` | [`ProfileController`](src/main/java/umastar/controller/ProfileController.java) | API xem và cập nhật hồ sơ cá nhân của người dùng đang đăng nhập. |
| `umastar.controller` | `class` | [`UserController`](src/main/java/umastar/controller/UserController.java) | API quản lý tài khoản nhân viên & người dùng (chỉ dành cho Logistics Manager). |
| `umastar.controller` | `class` | [`VehicleController`](src/main/java/umastar/controller/VehicleController.java) | API quản lý phương tiện vận chuyển (chỉ dành cho Fleet Coordinator). |
| `umastar.dto.request` | `record` | [`ChangePasswordRequest`](src/main/java/umastar/dto/request/ChangePasswordRequest.java) | Yêu cầu đổi mật khẩu của người đang đăng nhập. Mật khẩu hiện tại chỉ kiểm tra "có nhập" (tài khoản cũ có thể có mật khẩu không theo chính sách mới); mật khẩu mới phải đạt chính sách chung. Cả hai giữ nguyên, không cắt khoảng trắng và không bao giờ được ghi ra log. |
| `umastar.dto.request` | `record` | [`CreateCityRequest`](src/main/java/umastar/dto/request/CreateCityRequest.java) | Yêu cầu tạo thành phố ({@code POST /api/v1/admin/cities}). {@code timezone} là múi giờ IANA (VD: Asia/Ho_Chi_Minh), được kiểm tra ở tầng service. |
| `umastar.dto.request` | `record` | [`CreateCountryRequest`](src/main/java/umastar/dto/request/CreateCountryRequest.java) | Yêu cầu tạo quốc gia ({@code POST /api/v1/admin/countries}). Mã quốc gia được cắt khoảng trắng và tự viết hoa trước khi kiểm tra ({@code vn} thành {@code VN}). {@code homeCountry} và {@code partner} mặc định {@code false} nếu không gửi. |
| `umastar.dto.request` | `record` | [`CreateUserRequest`](src/main/java/umastar/dto/request/CreateUserRequest.java) | Yêu cầu tạo tài khoản nhân viên do Logistics Manager thực hiện ({@code POST /api/v1/users}). |
| `umastar.dto.request` | `record` | [`CreateVehicleRequest`](src/main/java/umastar/dto/request/CreateVehicleRequest.java) | Yêu cầu tạo phương tiện ({@code POST /api/v1/vehicles}). Phương tiện mới luôn ở trạng thái {@code AVAILABLE}. Biển số / mã được cắt khoảng trắng và viết hoa để việc kiểm tra trùng không phụ thuộc hoa thường. |
| `umastar.dto.request` | `record` | [`ForgotPasswordRequest`](src/main/java/umastar/dto/request/ForgotPasswordRequest.java) | Yêu cầu gửi hướng dẫn đặt lại mật khẩu qua email. Email được cắt khoảng trắng và viết thường. |
| `umastar.dto.request` | `record` | [`LoginRequest`](src/main/java/umastar/dto/request/LoginRequest.java) | Yêu cầu đăng nhập. Chỉ kiểm tra "có nhập" và độ dài tối đa, KHÔNG kiểm tra chính sách mật khẩu hay định dạng email: tài khoản cũ có thể có mật khẩu không theo chính sách mới, và không muốn tiết lộ quy tắc cho kẻ dò mật khẩu. Email được cắt khoảng trắng và viết thường; mật khẩu giữ nguyên và không bao giờ ghi ra log. |
| `umastar.dto.request` | `record` | [`RefreshTokenRequest`](src/main/java/umastar/dto/request/RefreshTokenRequest.java) | Yêu cầu làm mới phiên đăng nhập bằng refresh token nhận được lúc đăng nhập (hoặc lần làm mới trước). Token được cắt khoảng trắng hai đầu (do sao chép/dán) và không bao giờ được ghi ra log. |
| `umastar.dto.request` | `record` | [`RegisterRequest`](src/main/java/umastar/dto/request/RegisterRequest.java) | Yêu cầu đăng ký tài khoản khách hàng (CLB hoặc cá nhân chủ ngựa). Dữ liệu được chuẩn hóa ngay khi tạo đối tượng: cắt khoảng trắng hai đầu, email viết thường, số điện thoại rỗng thành {@code null}. Riêng mật khẩu giữ nguyên, không bao giờ bị cắt hay ghi ra log. |
| `umastar.dto.request` | `record` | [`ResetPasswordRequest`](src/main/java/umastar/dto/request/ResetPasswordRequest.java) | Yêu cầu đặt lại mật khẩu bằng token nhận từ email quên mật khẩu. Token được cắt khoảng trắng hai đầu; mật khẩu mới giữ nguyên và bị che khi ghi log. |
| `umastar.dto.request` | `record` | [`UpdateCityRequest`](src/main/java/umastar/dto/request/UpdateCityRequest.java) | Yêu cầu cập nhật toàn bộ thông tin thành phố ({@code PUT /api/v1/admin/cities/{id}}). Không đổi được quốc gia của thành phố. {@code active} bắt buộc: gửi {@code true} để kích hoạt lại thành phố đã bị vô hiệu hóa (xóa mềm). |
| `umastar.dto.request` | `record` | [`UpdateCountryRequest`](src/main/java/umastar/dto/request/UpdateCountryRequest.java) | Yêu cầu cập nhật toàn bộ thông tin quốc gia ({@code PUT /api/v1/admin/countries/{id}}). {@code active} bắt buộc: gửi {@code true} để kích hoạt lại một quốc gia đã bị vô hiệu hóa (xóa mềm). |
| `umastar.dto.request` | `record` | [`UpdateProfileRequest`](src/main/java/umastar/dto/request/UpdateProfileRequest.java) | Yêu cầu cập nhật hồ sơ cá nhân ({@code PUT /api/v1/profile}). Không chứa {@code email} và {@code role} nên người dùng không thể tự thay đổi hai trường này. |
| `umastar.dto.request` | `record` | [`UpdateUserStatusRequest`](src/main/java/umastar/dto/request/UpdateUserStatusRequest.java) | Yêu cầu cập nhật trạng thái tài khoản nhân viên ({@code PATCH /api/v1/users/{id}/status}). |
| `umastar.dto.request` | `record` | [`UpdateVehicleRequest`](src/main/java/umastar/dto/request/UpdateVehicleRequest.java) | Yêu cầu cập nhật toàn bộ thông tin phương tiện ({@code PUT /api/v1/vehicles/{id}}). {@code status} chỉ được đổi qua lại giữa {@code AVAILABLE} và {@code MAINTENANCE}; {@code IN_USE} do hệ thống tự cập nhật theo chuyến nên không được đặt thủ công (kiểm tra ở tầng service). |
| `umastar.dto.response` | `record` | [`AuthResponse`](src/main/java/umastar/dto/response/AuthResponse.java) | Bộ token trả về sau khi đăng nhập (và sau này khi làm mới token). {@code expiresIn} là thời gian sống của accessToken tính bằng giây. refreshToken chỉ xuất hiện ĐÚNG MỘT LẦN ở đây: DB chỉ lưu hash nên hệ thống không thể cấp lại bản gốc. |
| `umastar.dto.response` | `record` | [`CityDetailResponse`](src/main/java/umastar/dto/response/CityDetailResponse.java) | Thông tin đầy đủ của thành phố dành cho màn hình quản trị (gồm quốc gia và trạng thái hoạt động). |
| `umastar.dto.response` | `record` | [`CityResponse`](src/main/java/umastar/dto/response/CityResponse.java) | Thông tin thành phố trả về cho FE khi chọn điểm đi/đến. {@code timezone} là múi giờ IANA (VD: Asia/Ho_Chi_Minh) để hiển thị giờ địa phương. |
| `umastar.dto.response` | `record` | [`CountryDetailResponse`](src/main/java/umastar/dto/response/CountryDetailResponse.java) | Thông tin đầy đủ của quốc gia dành cho màn hình quản trị (gồm cờ nội bộ và trạng thái hoạt động). |
| `umastar.dto.response` | `record` | [`CountryResponse`](src/main/java/umastar/dto/response/CountryResponse.java) | Thông tin quốc gia trả về cho FE khi chọn điểm đi/đến (không chứa danh sách thành phố hay cờ nội bộ). |
| `umastar.dto.response` | `record` | [`ErrorResponse`](src/main/java/umastar/dto/response/ErrorResponse.java) | - |
| `umastar.dto.response` | `record` | [`MessageResponse`](src/main/java/umastar/dto/response/MessageResponse.java) | Phản hồi thông báo văn bản thống nhất cho các thao tác không trả dữ liệu chi tiết. |
| `umastar.dto.response` | `record` | [`PageResponse`](src/main/java/umastar/dto/response/PageResponse.java) | - |
| `umastar.dto.response` | `record` | [`RegisterResponse`](src/main/java/umastar/dto/response/RegisterResponse.java) | Kết quả đăng ký thành công. Không chứa mật khẩu hay hash. Người dùng đăng nhập ở bước tiếp theo. |
| `umastar.dto.response` | `record` | [`UserResponse`](src/main/java/umastar/dto/response/UserResponse.java) | Thông tin người dùng trả ra cho API hồ sơ cá nhân và quản lý tài khoản (không chứa thông tin nhạy cảm). |
| `umastar.dto.response` | `record` | [`UserSummaryResponse`](src/main/java/umastar/dto/response/UserSummaryResponse.java) | Thông tin tóm tắt của người dùng trả về cho FE (không có mật khẩu hay trạng thái bảo mật). |
| `umastar.dto.response` | `record` | [`VehicleResponse`](src/main/java/umastar/dto/response/VehicleResponse.java) | Thông tin phương tiện trả về cho màn hình điều phối. |
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
| `umastar.mapper` | `class` | [`AuthMapper`](src/main/java/umastar/mapper/AuthMapper.java) | Chuyển đổi DTO ↔ Entity cho module xác thực & tài khoản ({@code auth}). |
| `umastar.mapper` | `class` | [`CityMapper`](src/main/java/umastar/mapper/CityMapper.java) | Chuyển đổi Entity ↔ DTO cho danh mục thành phố. |
| `umastar.mapper` | `class` | [`CountryMapper`](src/main/java/umastar/mapper/CountryMapper.java) | Chuyển đổi Entity ↔ DTO cho danh mục quốc gia. |
| `umastar.mapper` | `class` | [`UserMapper`](src/main/java/umastar/mapper/UserMapper.java) | Chuyển đổi DTO ↔ Entity cho hồ sơ cá nhân ({@code /profile}) và quản lý người dùng ({@code /users}). |
| `umastar.mapper` | `class` | [`VehicleMapper`](src/main/java/umastar/mapper/VehicleMapper.java) | Chuyển đổi Entity ↔ DTO cho phương tiện. |
| `umastar.repository` | `interface` | [`CityRepository`](src/main/java/umastar/repository/CityRepository.java) | - |
| `umastar.repository` | `interface` | [`CountryRepository`](src/main/java/umastar/repository/CountryRepository.java) | - |
| `umastar.repository` | `interface` | [`CustomerRepository`](src/main/java/umastar/repository/CustomerRepository.java) | - |
| `umastar.repository` | `interface` | [`RefreshTokenRepository`](src/main/java/umastar/repository/RefreshTokenRepository.java) | - |
| `umastar.repository` | `interface` | [`TripRepository`](src/main/java/umastar/repository/TripRepository.java) | Mới chỉ có các truy vấn cần cho quản lý phương tiện; sẽ bổ sung khi làm chức năng chuyến vận chuyển. |
| `umastar.repository` | `interface` | [`UserRepository`](src/main/java/umastar/repository/UserRepository.java) | - |
| `umastar.repository` | `interface` | [`VehicleRepository`](src/main/java/umastar/repository/VehicleRepository.java) | - |
| `umastar.security` | `record` | [`AccessTokenClaims`](src/main/java/umastar/security/AccessTokenClaims.java) | Noi dung da xac thuc cua access token. |
| `umastar.security` | `class` | [`CustomUserDetailsService`](src/main/java/umastar/security/CustomUserDetailsService.java) | Nap thong tin nguoi dung cho Spring Security va JwtAuthenticationFilter. |
| `umastar.security` | `class` | [`JwtAuthenticationFilter`](src/main/java/umastar/security/JwtAuthenticationFilter.java) | Doc Bearer access token tu header Authorization, xac thuc va gan UserPrincipal vao SecurityContext. - Khong co header Bearer: cho di tiep, Spring Security se tra 401 neu endpoint can dang nhap. - Co token nhung sai/het han/tai khoan bi khoa: tra loi JSON ngay tai day. |
| `umastar.security` | `class` | [`JwtService`](src/main/java/umastar/security/JwtService.java) | Tao va xac thuc access token (JWT, ky HS256). Token chua: sub = id nguoi dung, role, iss, iat, exp, jti. KHONG chua email hay mat khau. Refresh token KHONG phai JWT: la chuoi ngau nhien, xem SecureTokenGenerator / TokenHasher. |
| `umastar.security` | `class` | [`SecurityUtils`](src/main/java/umastar/security/SecurityUtils.java) | Helper lay thong tin nguoi dung dang dang nhap tu SecurityContext. |
| `umastar.security` | `class` | [`UserPrincipal`](src/main/java/umastar/security/UserPrincipal.java) | Nguoi dung dang dang nhap, dung trong SecurityContext. Chi giu cac truong can thiet (khong giu entity User de tranh lazy loading va lo du lieu). Quyen duoc dat dang ROLE_<TEN_ROLE> nen dung duoc @PreAuthorize("hasRole('LOGISTICS_MANAGER')"). |
| `umastar.service` | `interface` | [`AuthService`](src/main/java/umastar/service/AuthService.java) | Nghiệp vụ xác thực & tài khoản. |
| `umastar.service` | `interface` | [`CityService`](src/main/java/umastar/service/CityService.java) | Nghiệp vụ danh mục thành phố: đọc cho mọi người dùng và quản trị (CRUD) cho Logistics Manager. |
| `umastar.service` | `interface` | [`CountryService`](src/main/java/umastar/service/CountryService.java) | Nghiệp vụ danh mục quốc gia: đọc cho mọi người dùng và quản trị (CRUD) cho Logistics Manager. |
| `umastar.service` | `interface` | [`FileStorageService`](src/main/java/umastar/service/FileStorageService.java) | - |
| `umastar.service` | `interface` | [`MailService`](src/main/java/umastar/service/MailService.java) | Dịch vụ gửi email thông báo/xác thực. Có thể thay thế triển khai bằng SMTP thật mà không đổi tầng nghiệp vụ. |
| `umastar.service` | `interface` | [`UserService`](src/main/java/umastar/service/UserService.java) | Nghiệp vụ hồ sơ cá nhân (/profile) và quản lý người dùng (/users). |
| `umastar.service` | `interface` | [`VehicleService`](src/main/java/umastar/service/VehicleService.java) | Nghiệp vụ quản lý phương tiện vận chuyển (Fleet Coordinator). |
| `umastar.service.impl` | `class` | [`AuthServiceImpl`](src/main/java/umastar/service/impl/AuthServiceImpl.java) | `@Service` |
| `umastar.service.impl` | `class` | [`CityServiceImpl`](src/main/java/umastar/service/impl/CityServiceImpl.java) | `@Service` |
| `umastar.service.impl` | `class` | [`CountryServiceImpl`](src/main/java/umastar/service/impl/CountryServiceImpl.java) | `@Service` |
| `umastar.service.impl` | `class` | [`FileStorageServiceImpl`](src/main/java/umastar/service/impl/FileStorageServiceImpl.java) | Triển khai {@link FileStorageService} lưu file lên ổ đĩa local theo cấu trúc: {@code {UPLOAD_DIR}/{loai}/{nam}/{thang}/{uuid}.{duoi}}. Tên file gốc KHÔNG bao giờ được dùng để đặt tên trên đĩa, nên không thể ghi đè file khác hay thoát khỏi thư mục upload. |
| `umastar.service.impl` | `class` | [`MailServiceImpl`](src/main/java/umastar/service/impl/MailServiceImpl.java) | Triển khai {@link MailService} cho môi trường phát triển (ghi log thay vì gửi SMTP thật). |
| `umastar.service.impl` | `class` | [`UserServiceImpl`](src/main/java/umastar/service/impl/UserServiceImpl.java) | `@Service` |
| `umastar.service.impl` | `class` | [`VehicleServiceImpl`](src/main/java/umastar/service/impl/VehicleServiceImpl.java) | `@Service` |
| `umastar.storage` | `enum` | [`AllowedFileType`](src/main/java/umastar/storage/AllowedFileType.java) | Cac dinh dang file duoc phep. Kiem tra 3 lop: duoi file, MIME khai bao, va quan trong nhat la "chu ky" (magic bytes) o dau file, vi duoi va MIME do client tu khai bao nen co the gia mao. |
| `umastar.storage` | `enum` | [`FileCategory`](src/main/java/umastar/storage/FileCategory.java) | Loai file nghiep vu: quyet dinh thu muc luu, dung luong toi da va dinh dang cho phep. Gioi han o day KHONG duoc lon hon MAX_FILE_SIZE trong .env (Spring chan truoc khi toi day). |
| `umastar.storage` | `record` | [`StoredFile`](src/main/java/umastar/storage/StoredFile.java) | Ket qua luu file. @param path duong dan TUONG DOI so voi thu muc upload (vd documents/2026/11/<uuid>.pdf) - luu vao cot file_url @param originalName ten file goc nguoi dung tai len (chi de hien thi) @param contentType MIME da duoc xac thuc theo noi dung file @param size dung luong (byte) |
| `umastar.util` | `class` | [`InputNormalizer`](src/main/java/umastar/util/InputNormalizer.java) | Chuẩn hóa chuỗi đầu vào dùng chung cho các DTO request. |
| `umastar.util` | `class` | [`PageableFactory`](src/main/java/umastar/util/PageableFactory.java) | - |
| `umastar.util` | `class` | [`PasswordPolicy`](src/main/java/umastar/util/PasswordPolicy.java) | Chinh sach mat khau dung chung (seed Manager, dang ky, doi mat khau, dat lai mat khau): 8 ky tu tro len, co chu hoa, chu thuong, chu so, va toi da 72 byte (gioi han cua BCrypt). |
| `umastar.util` | `class` | [`SecureTokenGenerator`](src/main/java/umastar/util/SecureTokenGenerator.java) | - |
| `umastar.util` | `class` | [`SqlLike`](src/main/java/umastar/util/SqlLike.java) | Dựng mẫu tìm kiếm {@code LIKE '%từ khóa%'} an toàn: thoát các ký tự đặc biệt {@code % _ \} để người dùng không "chèn" wildcard. |
| `umastar.util` | `class` | [`TokenHasher`](src/main/java/umastar/util/TokenHasher.java) | - |
| `umastar.validation` | `class` | [`StrongPassword`](src/main/java/umastar/validation/StrongPassword.java) | `@Documented`, `@Constraint`, `@Target`, `@Retention` |
| `umastar.validation` | `class` | [`StrongPasswordValidator`](src/main/java/umastar/validation/StrongPasswordValidator.java) | Bộ kiểm tra của {@link StrongPassword}: ủy quyền hoàn toàn cho {@link PasswordPolicy} để chỉ có một nguồn quy tắc. |
| `umastar.validation` | `class` | [`ValidPhone`](src/main/java/umastar/validation/ValidPhone.java) | `@Documented`, `@Constraint`, `@Target`, `@Retention` |
| `umastar.validation` | `class` | [`ValidPhoneValidator`](src/main/java/umastar/validation/ValidPhoneValidator.java) | Bộ kiểm tra số điện thoại Việt Nam cho {@link ValidPhone}. |

### API Endpoints (Controllers)

| Controller | Method | Path | Handler | Mô tả |
|---|---|---|---|---|
| `AdminCityController` | `GET` | `/api/v1/admin/cities/{id}` | `getCity()` | - |
| `AdminCityController` | `POST` | `/api/v1/admin/cities` | `createCity()` | - |
| `AdminCityController` | `DELETE` | `/api/v1/admin/cities/{id}` | `deactivateCity()` | - |
| `AdminCountryController` | `GET` | `/api/v1/admin/countries/{id}` | `getCountry()` | - |
| `AdminCountryController` | `POST` | `/api/v1/admin/countries` | `createCountry()` | - |
| `AdminCountryController` | `DELETE` | `/api/v1/admin/countries/{id}` | `deactivateCountry()` | - |
| `AuthController` | `POST` | `/api/v1/auth/register` | `register()` | - |
| `AuthController` | `POST` | `/api/v1/auth/login` | `login()` | - |
| `AuthController` | `POST` | `/api/v1/auth/refresh` | `refresh()` | - |
| `AuthController` | `POST` | `/api/v1/auth/logout` | `logout()` | - |
| `AuthController` | `POST` | `/api/v1/auth/logout-all` | `logoutAll()` | - |
| `AuthController` | `POST` | `/api/v1/auth/change-password` | `changePassword()` | - |
| `AuthController` | `POST` | `/api/v1/auth/forgot-password` | `forgotPassword()` | - |
| `AuthController` | `POST` | `/api/v1/auth/reset-password` | `resetPassword()` | - |
| `CountryController` | `GET` | `/api/v1/countries/{id}/cities` | `listCities()` | - |
| `ProfileController` | `GET` | `/api/v1/profile` | `getProfile()` | - |
| `ProfileController` | `PUT` | `/api/v1/profile` | `updateProfile()` | - |
| `UserController` | `POST` | `/api/v1/users` | `createUser()` | - |
| `UserController` | `GET` | `/api/v1/users/{id}` | `getUser()` | - |
| `VehicleController` | `GET` | `/api/v1/vehicles/{id}` | `getVehicle()` | - |
| `VehicleController` | `POST` | `/api/v1/vehicles` | `createVehicle()` | - |
| `VehicleController` | `DELETE` | `/api/v1/vehicles/{id}` | `deleteVehicle()` | - |

### Business Services

- **`CustomUserDetailsService`** (`src/main/java/umastar/security/CustomUserDetailsService.java`): Nap thong tin nguoi dung cho Spring Security va JwtAuthenticationFilter.
  - `loadUserByUsername(String email)` -> `UserDetails`
  - `findById(UUID id)` -> `Optional<UserPrincipal>`
- **`AuthServiceImpl`** (`src/main/java/umastar/service/impl/AuthServiceImpl.java`)
  - `register(RegisterRequest request)` -> `RegisterResponse`: Quy trình: kiểm tra email chưa dùng -> mã hóa mật khẩu (BCrypt) -> lưu User(CUSTOMER) -> lưu Customer liên kết 1-1. Hai lần đăng ký trùng email cùng lúc: bước kiểm tra không chặn được, nhưng ràng buộc {@code uk_users_email} của DB sẽ chặn và {@code GlobalExceptionHandler} đổi thành EMAIL_ALREADY_EXISTS.
  - `login(LoginRequest request, String userAgent, String ipAddress)` -> `AuthResponse`: Quy trình đăng nhập: 1. Tìm user theo email (không tìm thấy vẫn chạy BCrypt giả rồi báo INVALID_CREDENTIALS). 2. Chặn tài khoản đang bị khóa (UserStatus.LOCKED hoặc lockedUntil còn hiệu lực). 3. Nếu đã hết thời gian khóa tạm, reset bộ đếm lỗi; kiểm tra mật khẩu. 4. Sai mật khẩu -> tăng failedLoginAttempts, đạt 5 lần thì khóa 15 phút (noRollbackFor giữ được bộ đếm). 5. Đúng mật khẩu -> chặn nếu INACTIVE, ngược lại reset bộ đếm, ghi lastLoginAt, cấp access + refresh token.
  - `refresh(RefreshTokenRequest request, String userAgent, String ipAddress)` -> `AuthResponse`: Quy trình làm mới (rotation): 1. Băm token nhận được (SHA-256) rồi tìm và khóa dòng token; không có -> INVALID_REFRESH_TOKEN. 2. Token đã bị thu hồi mà vẫn được dùng -> nghi bị đánh cắp: thu hồi CẢ HỌ token, báo REFRESH_TOKEN_REVOKED. 3. Token hết hạn -> INVALID_REFRESH_TOKEN. 4. Tài khoản bị vô hiệu hóa/khóa -> ACCOUNT_DISABLED/ACCOUNT_LOCKED (không xoay vòng). 5. Hợp lệ -> lưu token mới (cùng familyId), thu hồi token cũ và ghi replacedByTokenId, trả cặp token mới.
  - `logout(RefreshTokenRequest request)` -> `void`: Đăng xuất một thiết bị (idempotent): thu hồi cả họ token của refresh token gửi lên.
  - `logoutAll(UUID userId)` -> `void`: Đăng xuất mọi thiết bị: thu hồi tất cả refresh token còn hiệu lực của người dùng.
  - `changePassword(UUID userId, ChangePasswordRequest request)` -> `void`: Đổi mật khẩu của người đang đăng nhập.
  - `forgotPassword(ForgotPasswordRequest request)` -> `MessageResponse`: Yêu cầu đặt lại mật khẩu: rate limit đồng nhất cho cả email có thật và không có thật để không lộ tài khoản, sinh token ngẫu nhiên lưu hash SHA-256 + hạn dùng + thời điểm yêu cầu trên {@code users}.
  - `resetPassword(ResetPasswordRequest request)` -> `void`: Đặt lại mật khẩu bằng mã reset từ email. {@code noRollbackFor = AppException}: xóa token đã hết hạn vẫn được lưu lại để tránh kiểm tra lặp.
- **`CityServiceImpl`** (`src/main/java/umastar/service/impl/CityServiceImpl.java`)
  - `listCities(UUID countryId)` -> `List<CityResponse>`
  - `getCity(UUID cityId)` -> `CityDetailResponse`
  - `createCity(CreateCityRequest request)` -> `CityDetailResponse`: Quốc gia phải tồn tại (có thể đang bị vô hiệu hóa để chuẩn bị dữ liệu trước), múi giờ phải hợp lệ, tên không trùng trong cùng quốc gia (không phân biệt hoa thường, tính cả thành phố đã vô hiệu hóa). Trùng tên song song được ràng buộc {@code uk_city_country_name} của DB chặn lại.
  - `updateCity(UUID cityId, UpdateCityRequest request)` -> `CityDetailResponse`
  - `deactivateCity(UUID cityId)` -> `void`
- **`CountryServiceImpl`** (`src/main/java/umastar/service/impl/CountryServiceImpl.java`)
  - `listCountries(TransportType scope)` -> `List<CountryResponse>`
  - `getCountry(UUID countryId)` -> `CountryDetailResponse`
  - `createCountry(CreateCountryRequest request)` -> `CountryDetailResponse`: Mã quốc gia phải chưa dùng; chỉ được có MỘT nước sở tại trong toàn hệ thống (tính cả nước đã vô hiệu hóa). Hai yêu cầu tạo cùng mã chạy song song: ràng buộc {@code uk_countries_iso_code} của DB sẽ chặn và {@code GlobalExceptionHandler} đổi thành COUNTRY_CODE_ALREADY_EXISTS.
  - `updateCountry(UUID countryId, UpdateCountryRequest request)` -> `CountryDetailResponse`
  - `deactivateCountry(UUID countryId)` -> `void`
- **`FileStorageServiceImpl`** (`src/main/java/umastar/service/impl/FileStorageServiceImpl.java`): Triển khai {@link FileStorageService} lưu file lên ổ đĩa local theo cấu trúc: {@code {UPLOAD_DIR}/{loai}/{nam}/{thang}/{uuid}.{duoi}}. Tên file gốc KHÔNG bao giờ được dùng để đặt tên trên đĩa, nên không thể ghi đè file khác hay thoát khỏi thư mục upload.
  - `store(MultipartFile file, FileCategory category)` -> `StoredFile`
  - `load(String path)` -> `Resource`
  - `delete(String path)` -> `void`
- **`MailServiceImpl`** (`src/main/java/umastar/service/impl/MailServiceImpl.java`): Triển khai {@link MailService} cho môi trường phát triển (ghi log thay vì gửi SMTP thật).
  - `sendPasswordResetEmail(String toEmail, String fullName, String rawToken, long expiresInMinutes)` -> `void`
- **`UserServiceImpl`** (`src/main/java/umastar/service/impl/UserServiceImpl.java`)
  - `getProfile(UUID userId)` -> `UserResponse`
  - `updateProfile(UUID userId, UpdateProfileRequest request)` -> `UserResponse`
  - `createUser(CreateUserRequest request)` -> `UserResponse`
  - `getUser(UUID userId)` -> `UserResponse`
  - `updateUserStatus(UUID currentUserId, UUID targetUserId, UpdateUserStatusRequest request)` -> `UserResponse`
- **`VehicleServiceImpl`** (`src/main/java/umastar/service/impl/VehicleServiceImpl.java`)
  - `getVehicle(UUID vehicleId)` -> `VehicleResponse`
  - `createVehicle(CreateVehicleRequest request)` -> `VehicleResponse`: Hai yêu cầu tạo cùng biển số chạy song song: ràng buộc {@code uk_vehicles_code_or_plate} của DB sẽ chặn và {@code GlobalExceptionHandler} đổi thành VEHICLE_CODE_ALREADY_EXISTS.
  - `updateVehicle(UUID vehicleId, UpdateVehicleRequest request)` -> `VehicleResponse`: Mọi kiểm tra chạy TRƯỚC khi gán dữ liệu, nên khi báo lỗi thì phương tiện không bị đổi dở.
  - `deleteVehicle(UUID vehicleId)` -> `void`

### Repositories

- **`CityRepository`** (`src/main/java/umastar/repository/CityRepository.java`)
- **`CountryRepository`** (`src/main/java/umastar/repository/CountryRepository.java`)
- **`CustomerRepository`** (`src/main/java/umastar/repository/CustomerRepository.java`)
- **`RefreshTokenRepository`** (`src/main/java/umastar/repository/RefreshTokenRepository.java`)
- **`TripRepository`** (`src/main/java/umastar/repository/TripRepository.java`)
- **`UserRepository`** (`src/main/java/umastar/repository/UserRepository.java`)
- **`VehicleRepository`** (`src/main/java/umastar/repository/VehicleRepository.java`)

### Resources & Cấu hình

- `src/main/resources/application.properties`
<!-- AUTO-GENERATED-BACKEND:END -->

