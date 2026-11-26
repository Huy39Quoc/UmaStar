# Hướng dẫn dự án UmaStar (AI / Contributor Rules)

## 1. Kiến trúc code (Layered Architecture)
Mọi mã nguồn nằm trong package gốc `umastar`, tổ chức theo tầng:
- `umastar.config`: Cấu hình Spring (`SecurityConfig`, `OpenApiConfig`, ...).
- `umastar.controller`: REST Controller với prefix `/api/v1`, chỉ nhận/trả DTO, không chứa logic nghiệp vụ.
- `umastar.service` & `umastar.service.impl`: Interface nghiệp vụ đặt trong `umastar.service` (`<Domain>Service`), class triển khai đặt trong `umastar.service.impl` (`<Domain>ServiceImpl` hoặc `<Provider><Domain>Service`), chứa `@Transactional`, kiểm tra quyền sở hữu và gọi State Machine.
- `umastar.repository`: Spring Data JPA Repository (`*Repository`), dùng `@EntityGraph` hoặc `JOIN FETCH` cho truy vấn danh sách.
- `umastar.dto.request` & `umastar.dto.response`: Tách riêng DTO đầu vào (`*Request`) và đầu ra (`*Response` dùng `record` nếu bất biến). Không trả Entity trực tiếp ra API.
- `umastar.entity` & `umastar.enums`: Thực thể JPA kế thừa `BaseEntity` và các enum trạng thái/phân loại.
- `umastar.exception`: `ErrorCode`, `AppException`, `GlobalExceptionHandler` (`@RestControllerAdvice`).
- `umastar.security`: `JwtService`, `JwtAuthenticationFilter`, `UserDetailsService`, `SecurityUtils`.
- `umastar.util`: Helper tĩnh dùng chung (`PageableFactory`, `SecureTokenGenerator`, `TokenHasher`, ...).

## 2. Quy ước đặt tên & thiết kế chuẩn
- **Class / Record**:
  - Controller: `<Domain>Controller`; Service interface: `<Domain>Service` (trong `umastar.service`); Service impl: `<Domain>ServiceImpl` (trong `umastar.service.impl`); Repository: `<Entity>Repository`.
  - Request DTO: `<Action><Domain>Request` (VD: `CreateOrderRequest`, `LoginRequest`); Response DTO: `<Domain>Response` / `<Domain>DetailResponse`.
  - Handler trong `GlobalExceptionHandler`: bắt đầu bằng `handle*` (VD: `handleAppException`, `handleValidation`).
- **Method trong Service**: dùng động từ rõ nghĩa: `get*` (lấy 1 bản ghi, ném `AppException` nếu không thấy), `list*` / `search*` (trả `PageResponse<...>`), `create*`, `update*`, `delete*` / `deactivate*`, `approve*`, `reject*`, `cancel*`.
- **ErrorCode & Constraint DB**:
  - `ErrorCode` dùng dải mã 100 số theo module; đặt tên nhất quán dạng `*_NOT_FOUND`, `*_ALREADY_EXISTS`, `*_INVALID`, `*_IN_USE`.
  - Tên Unique Constraint trên `@Table(uniqueConstraints = ...)` đặt theo chuẩn `uk_<tên_bảng>_<tên_cột>` (viết thường) và ánh xạ vào `GlobalExceptionHandler.CONSTRAINT_ERRORS`.
- **Entity & JPA**:
  - Mọi quan hệ dùng `FetchType.LAZY`; không dùng `@Data` trên `@Entity` (dùng `@Getter @Setter @NoArgsConstructor`).
  - Các entity có tranh chấp cập nhật đồng thời gắn `@Version`.
  - Mọi thời điểm dùng `Instant` (UTC); không cấu hình `spring.jpa.open-in-view=true`.
- **Cấu hình không hardcode**: Mọi biến môi trường khai báo trong `.env` và `.env.example` (giá trị trống `KEY=` trong `.env.example`), tham chiếu qua `${...}` trong `src/main/resources/application.properties`.

## 3. Tự động kiểm tra, sửa lỗi & đồng bộ tài liệu
Mỗi khi thêm, sửa hoặc xóa bất kỳ file nào trong `src/main/java/umastar/**` hoặc `src/main/resources/**`:

1. **Tự kiểm tra & sửa lỗi trước khi kết thúc**:
   - Chạy `.\mvnw.cmd test` (đã gồm `ErrorCodeTest` kiểm tra trùng mã lỗi và `UmaStarApplicationTests` kiểm tra Hibernate/PostgreSQL/Security/Swagger). Tránh dùng `clean` khi không cần thiết để không xóa `target/` làm Java Language Server trong IDE báo đỏ giả (chỉ dùng `clean` khi xóa/đổi tên file hoặc sửa `pom.xml`).
   - Nếu phát hiện sai tên file vs `public class`, sai package, `EAGER` fetch, `@Data` trên entity, hardcode cấu hình, lệch `uk_*` constraint, hoặc lỗi biên dịch/test -> **tự sửa ngay** rồi kiểm tra lại.
2. **Chạy script đồng bộ tài liệu tự động**:
   ```bash
   java scripts/SyncDocs.java
   ```
   Script sẽ tự cập nhật các khối `AUTO-GENERATED` trong `PROJECT_SPEC.md`, `ENTITY_SCHEMA.md` và `BACKEND_ROADMAP.md`.
3. **Cập nhật mô tả nghiệp vụ & tiến độ**:
   - Viết/cập nhật Javadoc (`/** ... */`) ngay trên class, method hoặc field trong mã nguồn Java để script tự trích xuất vào Markdown.
   - Nếu thay đổi Actor, Main Features hoặc 6 Core Business Flows, cập nhật phần mô tả tương ứng trong `PROJECT_SPEC.md` và `ENTITY_SCHEMA.md`.
   - Khi hoàn thành hạng mục nào, đánh dấu `[x]` vào dòng tương ứng trong `BACKEND_ROADMAP.md`.
