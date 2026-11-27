# Hướng dẫn tích hợp Auth, Hồ sơ & Quản lý Tài khoản cho Frontend (UmaStar)

Tài liệu này mô tả chi tiết toàn bộ luồng xác thực, quản lý phiên đăng nhập, hồ sơ cá nhân và quản lý nhân viên đã hoàn thiện ở **P0**, kèm đầy đủ quy tắc validation, cấu trúc request/response và mã lỗi để Frontend (FE) tích hợp chính xác.

---

## 1. Quy ước chung & Cơ chế bảo mật

- **Base URL mặc định (dev)**: `http://localhost:8080/api/v1` (cấu hình qua `SERVER_PORT` trong `.env`).
- **Swagger UI (test trực tiếp)**: `http://localhost:8080/swagger-ui.html` (OpenAPI JSON: `/v3/api-docs`).
- **Cơ chế xác thực (Stateless JWT + Refresh Token Rotation)**:
  - Hệ thống **không dùng session/cookie**, xác thực hoàn toàn qua HTTP header:
    ```http
    Authorization: Bearer <accessToken>
    ```
  - **Access Token**: là JWT ký `HS256`, chứa `sub` (`userId`), `role`, `iat`, `exp`. Thời hạn mặc định **15 phút** (`JWT_ACCESS_EXP_MS=900000`, trả về cho FE trường `expiresIn` tính bằng **giây**).
  - **Refresh Token**: là chuỗi ngẫu nhiên bảo mật (không phải JWT), backend chỉ lưu bản băm `SHA-256` trong DB. Thời hạn mặc định **7 ngày** (`JWT_REFRESH_EXP_MS=604800000`).
  - **CORS**: Backend chỉ chấp nhận các origin khai báo trong `CORS_ALLOWED_ORIGINS` (mặc định `http://localhost:3000,http://localhost:5173`), cho phép header `Authorization`, `Content-Type`, `Accept` và không bật cookie (`allowCredentials = false`).
- **Múi giờ**: Mọi trường thời gian (`Instant`) dùng chuẩn ISO-8601 **UTC** (ví dụ `"2026-11-27T15:00:00Z"`). FE tự quy đổi sang múi giờ địa phương khi hiển thị.

---

## 2. Cấu trúc Response Lỗi & Phân trang dùng chung

### 2.1. Format lỗi thống nhất (`ErrorResponse`)
Mọi lỗi (`400`, `401`, `403`, `404`, `409`, `423`, `429`, `500`, ...) đều trả về cùng một khuôn JSON:

```json
{
  "timestamp": "2026-11-27T15:10:00.123Z",
  "status": 400,
  "code": 602,
  "error": "Bad Request",
  "message": "Dữ liệu nhập chưa hợp lệ",
  "path": "/api/v1/auth/register",
  "errors": {
    "email": "Email không đúng định dạng",
    "password": "Mật khẩu từ 8 ký tự, có chữ hoa, chữ thường và chữ số (tối đa 72 byte)"
  }
}
```

- `code`: Mã lỗi nghiệp vụ nội bộ (FE nên dựa vào `code` hoặc `status` để xử lý logic, và có thể hiển thị trực tiếp `message` tiếng Việt).
- `errors`: Chỉ xuất hiện khi lỗi validation (`code: 602`). Mỗi key là tên trường bị lỗi, value là thông báo lỗi đầu tiên của trường đó.

### 2.2. Format phân trang (`PageResponse<T>`)

```json
{
  "content": [ /* danh sách phần tử */ ],
  "page": 0,
  "size": 20,
  "totalElements": 12,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

---

## 3. Quy tắc Validation dùng chung & Enum

### 3.1. Quy tắc mật khẩu mạnh (`@StrongPassword` / `PasswordPolicy`)
Áp dụng cho: **Đăng ký**, **Đổi mật khẩu (`newPassword`)**, **Đặt lại mật khẩu (`newPassword`)**, **Tạo nhân viên**.
- Không được để trống (`@NotBlank`).
- Độ dài tối thiểu **8 ký tự** và tối đa **72 byte UTF-8** (giới hạn của BCrypt — lưu ý chữ tiếng Việt có dấu tốn 2–3 byte/ký tự).
- Bắt buộc có ít nhất:
  - **1 chữ cái in hoa** (`Character.isUpperCase`)
  - **1 chữ cái in thường** (`Character.isLowerCase`)
  - **1 chữ số** (`Character.isDigit`)
- **Lưu ý quan trọng**: Mật khẩu **không bị cắt khoảng trắng (`trim`)** ở cả đăng ký, đăng nhập và đổi mật khẩu — FE không tự ý `.trim()` ô mật khẩu.
- Thông báo lỗi mặc định từ BE: `"Mật khẩu từ 8 ký tự, có chữ hoa, chữ thường và chữ số (tối đa 72 byte)"`.

### 3.2. Quy tắc số điện thoại Việt Nam (`@ValidPhone`)
Áp dụng cho: **Đăng ký**, **Cập nhật hồ sơ (`/profile`)**, **Tạo nhân viên**.
- Trường **không bắt buộc** (FE gửi `null` hoặc `""` đều được BE chuẩn hóa thành `null`).
- Nếu có nhập: sau khi trim phải khớp regex số điện thoại di động Việt Nam `^(?:0|\+?84)[35789][0-9]{8}$`:
  - Bắt đầu bằng `0`, `84`, hoặc `+84` (ví dụ `0912345678`, `84912345678`, `+84912345678`).
  - Đầu số hợp lệ theo quy chuẩn VN: `3`, `5`, `7`, `8`, `9` + đúng 8 chữ số phía sau (không chứa dấu cách hay gạch ngang; đầu `01` cũ hoặc số nước ngoài sẽ bị từ chối).
- Thông báo lỗi: `"Số điện thoại Việt Nam không hợp lệ (bắt đầu bằng 0, 84 hoặc +84 và đầu số 3, 5, 7, 8, 9)"`.

### 3.3. Danh sách Enum
- **`Role`** (6 vai trò):
  - `LOGISTICS_MANAGER`: Quản lý Điều hành Logistics
  - `TRANSPORT_SPECIALIST`: Chuyên viên Thủ tục & Kiểm dịch
  - `FLEET_COORDINATOR`: Điều phối viên Đội xe & Lộ trình
  - `DRIVER`: Tài xế vận chuyển
  - `ESCORT`: Nhân viên đi kèm chăm sóc ngựa
  - `CUSTOMER`: Khách hàng (CLB / Chủ ngựa)
- **`UserStatus`**: `ACTIVE` (hoạt động), `INACTIVE` (bị vô hiệu hóa), `LOCKED` (bị admin khóa cứng).
- **`CustomerType`**: `CLUB` (Câu lạc bộ / tổ chức), `INDIVIDUAL` (Cá nhân chủ ngựa).

---

## 4. Tài khoản Seed mặc định cho môi trường Dev (`DataSeeder`)

Khi backend khởi động lần đầu (hoặc DB chưa có user thuộc role nào), `DataSeeder` tự động đọc cấu hình trong `.env` (qua `SeedProperties`) để tạo **1 tài khoản `ACTIVE`, `emailVerified = true`** cho từng `Role` được cấu hình:

| Role | Email mặc định trong `.env` (dev) | Ghi chú |
|---|---|---|
| `LOGISTICS_MANAGER` | `manager@umastar.local` | Khởi tạo từ `SEED_MANAGER_EMAIL` / `SEED_MANAGER_PASSWORD` |
| `TRANSPORT_SPECIALIST` | `specialist@umastar.local` | Khởi tạo từ `SEED_SPECIALIST_EMAIL` / `SEED_SPECIALIST_PASSWORD` |
| `FLEET_COORDINATOR` | `coordinator@umastar.local` | Khởi tạo từ `SEED_COORDINATOR_EMAIL` / `SEED_COORDINATOR_PASSWORD` |
| `DRIVER` | `driver@umastar.local` | Khởi tạo từ `SEED_DRIVER_EMAIL` / `SEED_DRIVER_PASSWORD` |
| `ESCORT` | `escort@umastar.local` | Khởi tạo từ `SEED_ESCORT_EMAIL` / `SEED_ESCORT_PASSWORD` |
| `CUSTOMER` | `customer@umastar.local` | Khởi tạo từ `SEED_CUSTOMER_EMAIL` / `SEED_CUSTOMER_PASSWORD`, tự tạo kèm bản ghi `Customer` (`CLUB`) |

> **Lưu ý**: Mật khẩu dev lấy theo các biến `SEED_*_PASSWORD` trong file `.env` cục bộ của đội phát triển (không lưu mật khẩu thật trong tài liệu/Git). Nếu DB đã có sẵn bất kỳ user nào mang role đó thì `DataSeeder` sẽ bỏ qua role đó.

---

## 5. Chi tiết từng API

### Nhóm A — Xác thực (`/api/v1/auth/**`)

#### 1. Đăng ký khách hàng
- **Endpoint**: `POST /api/v1/auth/register`
- **Quyền**: Công khai (không cần token)
- **Mục đích**: Khách hàng tự đăng ký tài khoản `CUSTOMER` và hệ thống tự tạo kèm hồ sơ `Customer` 1-1. Nhân viên nội bộ **không** đăng ký qua API này (phải do `LOGISTICS_MANAGER` tạo ở `/api/v1/users`).
- **Request Body** (`RegisterRequest` — BE tự trim `fullName`, `organizationName`, `phone`, trim + lowercase `email`, chuỗi `phone` rỗng thành `null`):

| Trường | Kiểu | Bắt buộc | Điều kiện validation |
|---|---|---|---|
| `fullName` | `string` | Có | `@NotBlank` ("Vui lòng nhập họ tên"), tối đa **150 ký tự** |
| `email` | `string` | Có | `@NotBlank` ("Vui lòng nhập email"), đúng định dạng `@Email`, tối đa **150 ký tự**, unique không phân biệt hoa/thường |
| `password` | `string` | Có | `@NotBlank` ("Vui lòng nhập mật khẩu"), đạt `@StrongPassword` (8 ký tự – 72 byte, có hoa + thường + số) |
| `phone` | `string` | Không | Nếu nhập: khớp SĐT Việt Nam `^(?:0|\+?84)[35789][0-9]{8}$` |
| `organizationName` | `string` | Có | `@NotBlank` ("Vui lòng nhập tên tổ chức hoặc chủ ngựa"), tối đa **200 ký tự** |
| `customerType` | `CustomerType` | Có | `@NotNull` ("Vui lòng chọn loại khách hàng (CLUB hoặc INDIVIDUAL)"), chỉ nhận `CLUB` hoặc `INDIVIDUAL` |

- **Response `201 Created`** (`RegisterResponse`):
  ```json
  {
    "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "customerId": "7d1a9c10-1234-4b56-8901-abcdef123456",
    "fullName": "Nguyễn Văn An",
    "email": "an@club.vn",
    "role": "CUSTOMER",
    "organizationName": "CLB Thiên Mã",
    "customerType": "CLUB"
  }
  ```
  > Lưu ý: Đăng ký thành công **chưa tự đăng nhập** (không trả token). FE có thể chuyển hướng sang trang đăng nhập hoặc gọi tiếp `/login`.
- **Lỗi có thể gặp**:
  - `400` (`602 VALIDATION_FAILED`): Dữ liệu đầu vào sai (xem `errors`).
  - `400` (`601 INVALID_REQUEST`): JSON sai cú pháp hoặc `customerType` không thuộc `CLUB`/`INDIVIDUAL`.
  - `409` (`301 EMAIL_ALREADY_EXISTS`): `"Email đã được sử dụng"`.

---

#### 2. Đăng nhập
- **Endpoint**: `POST /api/v1/auth/login`
- **Quyền**: Công khai
- **Request Body** (`LoginRequest` — BE tự trim + lowercase `email`, giữ nguyên `password`):

| Trường | Kiểu | Bắt buộc | Điều kiện validation |
|---|---|---|---|
| `email` | `string` | Có | `@NotBlank` ("Vui lòng nhập email"), tối đa **150 ký tự** |
| `password` | `string` | Có | `@NotBlank` ("Vui lòng nhập mật khẩu"), tối đa **128 ký tự** (không ép policy mật khẩu mới để hỗ trợ tài khoản cũ) |

- **Quy tắc bảo mật ở BE**:
  - Sai email hoặc sai mật khẩu trả chung thông báo `INVALID_CREDENTIALS` để tránh lộ email có tồn tại hay không.
  - Tài khoản `ACTIVE` nhập sai mật khẩu **5 lần liên tiếp** sẽ bị **khóa tạm 15 phút** (`lockedUntil`).
  - Nếu tài khoản đang khóa tạm (`lockedUntil > now`) hoặc bị admin khóa cứng (`UserStatus.LOCKED`) → từ chối ngay kể cả nhập đúng mật khẩu.
  - Tài khoản bị vô hiệu hóa (`UserStatus.INACTIVE`): nhập sai mật khẩu báo `INVALID_CREDENTIALS`, nhập đúng mới báo `ACCOUNT_DISABLED`.
  - Thành công: reset bộ đếm sai về `0`, xóa `lockedUntil`, ghi `lastLoginAt`, cấp `accessToken` + `refreshToken` (tạo mới một `familyId` cho phiên/thiết bị này).
- **Response `200 OK`** (`AuthResponse`):
  ```json
  {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "dGhpcy1pcy1hLXJhbmRvbS10b2tlbg",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "user": {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "fullName": "Nguyễn Văn An",
      "email": "an@club.vn",
      "role": "CUSTOMER"
    }
  }
  ```
- **Lỗi có thể gặp**:
  - `400` (`602 VALIDATION_FAILED`): Thiếu email/mật khẩu hoặc vượt quá độ dài.
  - `401` (`100 INVALID_CREDENTIALS`): `"Email hoặc mật khẩu không đúng"`.
  - `403` (`104 ACCOUNT_DISABLED`): `"Tài khoản đã bị vô hiệu hóa"`.
  - `423` (`103 ACCOUNT_LOCKED`): `"Tài khoản tạm thời bị khóa do đăng nhập sai nhiều lần. Vui lòng thử lại sau"`.

---

#### 3. Làm mới token (Refresh Token Rotation)
- **Endpoint**: `POST /api/v1/auth/refresh`
- **Quyền**: Công khai (xác thực bằng `refreshToken`)
- **Request Body** (`RefreshTokenRequest` — BE tự trim `refreshToken`):

| Trường | Kiểu | Bắt buộc | Điều kiện validation |
|---|---|---|---|
| `refreshToken` | `string` | Có | `@NotBlank` ("Vui lòng cung cấp refresh token"), tối đa **512 ký tự** |

- **Cách hoạt động (Rotation & Chống đánh cắp)**:
  - Mỗi lần gọi thành công, BE **thu hồi ngay `refreshToken` cũ** (`ROTATED`) và cấp cặp `accessToken` + `refreshToken` **mới hoàn toàn** trong cùng một `familyId`.
  - **Cảnh báo cực kỳ quan trọng cho FE**: Nếu dùng lại một `refreshToken` đã được xoay vòng/thu hồi trước đó, BE coi như **token bị lộ** và **thu hồi toàn bộ phiên (`familyId`) của thiết bị đó**, trả `401` (`203 REFRESH_TOKEN_REVOKED`). Vì vậy FE **phải khóa tuần tự (single-flight lock)** khi nhiều API trả `401` cùng lúc — chỉ gọi `/refresh` 1 lần rồi dùng `accessToken` mới cho các request đang chờ, **và phải lưu đè `refreshToken` mới ngay lập tức**.
  - Lưu ý: Khóa tạm 15 phút do ai đó cố tình nhập sai mật khẩu không làm mất phiên đang đăng nhập hợp lệ qua `/refresh`, nhưng tài khoản `INACTIVE` hoặc `LOCKED` sẽ bị từ chối.
- **Response `200 OK`**: Giống `AuthResponse` ở `/login`.
- **Lỗi có thể gặp**:
  - `400` (`602 VALIDATION_FAILED`): Thiếu hoặc token vượt 512 ký tự.
  - `401` (`202 INVALID_REFRESH_TOKEN`): Token không tồn tại hoặc đã hết hạn → FE xóa token và đưa về `/login`.
  - `401` (`203 REFRESH_TOKEN_REVOKED`): Dùng lại token cũ hoặc phiên đã bị thu hồi → FE xóa token và bắt buộc đăng nhập lại.
  - `403` (`104 ACCOUNT_DISABLED`) / `423` (`103 ACCOUNT_LOCKED`): Tài khoản đã bị khóa/vô hiệu hóa.

---

#### 4. Đăng xuất thiết bị hiện tại
- **Endpoint**: `POST /api/v1/auth/logout`
- **Quyền**: Công khai (chỉ cần gửi `refreshToken`, vẫn đăng xuất được ngay cả khi `accessToken` đã hết hạn)
- **Request Body**: `RefreshTokenRequest` (`{ "refreshToken": "..." }`, validation như trên).
- **Hành vi**: Thu hồi toàn bộ `familyId` của `refreshToken` đó (`LOGOUT`). Endpoint có tính **idempotent**: token không tồn tại hoặc đã thu hồi vẫn trả thành công để không lộ thông tin. FE sau khi gọi cần tự xóa `accessToken` và `refreshToken` ở client.
- **Response**: `204 No Content`.

---

#### 5. Đăng xuất tất cả thiết bị
- **Endpoint**: `POST /api/v1/auth/logout-all`
- **Quyền**: Đã đăng nhập (bắt buộc `Authorization: Bearer <accessToken>`)
- **Request Body**: Không có.
- **Hành vi**: Thu hồi **toàn bộ `refreshToken`** đang hoạt động của user trên mọi thiết bị (`LOGOUT_ALL`). FE phải xóa token ở thiết bị hiện tại và điều hướng về `/login`. *(Access token cũ ở các thiết bị khác tự hết hạn tối đa sau `expiresIn` giây).*
- **Response**: `204 No Content`.
- **Lỗi có thể gặp**: `401` (`101 UNAUTHORIZED` / `200 INVALID_TOKEN` / `201 TOKEN_EXPIRED`).

---

#### 6. Quên mật khẩu
- **Endpoint**: `POST /api/v1/auth/forgot-password`
- **Quyền**: Công khai
- **Request Body** (`ForgotPasswordRequest` — BE tự trim + lowercase `email`):

| Trường | Kiểu | Bắt buộc | Điều kiện validation |
|---|---|---|---|
| `email` | `string` | Có | `@NotBlank` ("Vui lòng nhập email"), đúng định dạng `@Email`, tối đa **150 ký tự** |

- **Hành vi**:
  - **Luôn trả `200 OK`** thông báo chung dù email có tồn tại hay không (chống dò email).
  - Nếu tài khoản tồn tại và đang `ACTIVE`: tạo mã đặt lại mật khẩu 1 lần, lưu SHA-256 hash (`resetTokenHash`), thời hạn **30 phút** (`RESET_TOKEN_EXP_MS=1800000`) và `resetRequestedAt` trên bảng `users`, rồi gọi `MailService`. *(Ở môi trường dev chưa có SMTP, `MailServiceImpl` in `[DEV MAIL] ... Token reset: <rawToken>` ra console của backend).*
  - **Rate limit**: Gửi lặp lại cùng một email trong vòng **60 giây** (`RESET_COOLDOWN_MS=60000`) sẽ bị chặn đồng nhất cho cả email thật và email không tồn tại.
- **Response `200 OK`** (`MessageResponse`):
  ```json
  {
    "message": "Nếu email tồn tại trong hệ thống, hướng dẫn đặt lại mật khẩu đã được gửi"
  }
  ```
- **Lỗi có thể gặp**:
  - `400` (`602 VALIDATION_FAILED`): Email trống hoặc sai định dạng.
  - `429` (`611 TOO_MANY_REQUESTS`): `"Thao tác quá nhanh, vui lòng thử lại sau"` → FE nên khóa nút "Gửi lại" và đếm ngược 60 giây.

---

#### 7. Đặt lại mật khẩu
- **Endpoint**: `POST /api/v1/auth/reset-password`
- **Quyền**: Công khai
- **Request Body** (`ResetPasswordRequest` — BE tự trim `token`, giữ nguyên `newPassword`):

| Trường | Kiểu | Bắt buộc | Điều kiện validation |
|---|---|---|---|
| `token` | `string` | Có | `@NotBlank` ("Vui lòng cung cấp mã đặt lại mật khẩu"), tối đa **512 ký tự** |
| `newPassword` | `string` | Có | `@NotBlank` ("Vui lòng nhập mật khẩu mới"), đạt `@StrongPassword` |

- **Hành vi**:
  - Kiểm tra token khớp `resetTokenHash` và chưa hết hạn. Nếu hết hạn, BE xóa luôn token cũ và báo lỗi.
  - Mật khẩu mới không được trùng mật khẩu hiện tại.
  - Thành công: cập nhật mật khẩu mới, ghi `passwordChangedAt` (vô hiệu hóa các access token cũ phát hành trước đó), mở khóa tạm thời nếu có (`failedLoginAttempts = 0`, `lockedUntil = null`), xóa `resetTokenHash` / `resetTokenExpiresAt` / `resetRequestedAt` về `null` (chỉ dùng 1 lần), và thu hồi toàn bộ refresh token (`PASSWORD_RESET`).
- **Response**: `204 No Content` → FE thông báo thành công và chuyển về trang `/login`.
- **Lỗi có thể gặp**:
  - `400` (`602 VALIDATION_FAILED`): Thiếu token hoặc `newPassword` yếu.
  - `400` (`204 INVALID_RESET_TOKEN`): `"Mã đặt lại mật khẩu không hợp lệ hoặc đã hết hạn"`.
  - `400` (`401 NEW_PASSWORD_SAME_AS_OLD`): `"Mật khẩu mới phải khác mật khẩu cũ"`.
  - `403` (`104 ACCOUNT_DISABLED`) / `423` (`103 ACCOUNT_LOCKED`).

---

#### 8. Đổi mật khẩu
- **Endpoint**: `POST /api/v1/auth/change-password`
- **Quyền**: Đã đăng nhập (bắt buộc `Authorization: Bearer <accessToken>`)
- **Request Body** (`ChangePasswordRequest` — giữ nguyên cả 2 trường, không trim):

| Trường | Kiểu | Bắt buộc | Điều kiện validation |
|---|---|---|---|
| `currentPassword` | `string` | Có | `@NotBlank` ("Vui lòng nhập mật khẩu hiện tại"), tối đa **128 ký tự** |
| `newPassword` | `string` | Có | `@NotBlank` ("Vui lòng nhập mật khẩu mới"), đạt `@StrongPassword` |

- **Quy tắc bảo mật**:
  - Nhập sai `currentPassword` được tính chung vào bộ đếm 5 lần sai → đủ 5 lần sẽ bị khóa tạm 15 phút (`423 ACCOUNT_LOCKED`).
  - `newPassword` phải khác `currentPassword`.
  - Đổi thành công: ghi `passwordChangedAt` (mọi access token cũ bị `JwtAuthenticationFilter` từ chối ở request sau) và thu hồi toàn bộ refresh token (`PASSWORD_CHANGED`). **FE bắt buộc xóa token và chuyển người dùng về `/login` để đăng nhập lại bằng mật khẩu mới**.
- **Response**: `204 No Content`.
- **Lỗi có thể gặp**:
  - `400` (`602 VALIDATION_FAILED`): Thiếu trường hoặc `newPassword` không đạt policy.
  - `400` (`400 INVALID_OLD_PASSWORD`): `"Mật khẩu hiện tại không đúng"`.
  - `400` (`401 NEW_PASSWORD_SAME_AS_OLD`): `"Mật khẩu mới phải khác mật khẩu cũ"`.
  - `401` (`101` / `200` / `201`): Chưa đăng nhập hoặc token hết hạn.
  - `423` (`103 ACCOUNT_LOCKED`): Nhập sai quá 5 lần.

---

### Nhóm B — Hồ sơ cá nhân (`/api/v1/profile`)

#### 9. Xem hồ sơ cá nhân
- **Endpoint**: `GET /api/v1/profile`
- **Quyền**: Đã đăng nhập (mọi role)
- **Response `200 OK`** (`UserResponse`):
  ```json
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "fullName": "Nguyễn Văn An",
    "email": "an@club.vn",
    "phone": "+84912345678",
    "role": "CUSTOMER",
    "status": "ACTIVE",
    "emailVerified": false,
    "lastLoginAt": "2026-11-27T15:00:00Z",
    "createdAt": "2026-11-27T14:30:00Z"
  }
  ```

#### 10. Cập nhật hồ sơ cá nhân
- **Endpoint**: `PUT /api/v1/profile`
- **Quyền**: Đã đăng nhập (mọi role)
- **Request Body** (`UpdateProfileRequest` — BE tự trim `fullName`, chuyển `phone` rỗng thành `null`):

| Trường | Kiểu | Bắt buộc | Điều kiện validation |
|---|---|---|---|
| `fullName` | `string` | Có | `@NotBlank` ("Vui lòng nhập họ tên"), tối đa **150 ký tự** |
| `phone` | `string` | Không | Nếu có: khớp SĐT Việt Nam `^(?:0|\+?84)[35789][0-9]{8}$` |

- **Lưu ý**: Không cho phép sửa `email` và `role` (trên UI để disabled/readonly; nếu cố gửi kèm trong JSON cũng bị bỏ qua).
- **Response `200 OK`**: Trả về `UserResponse` mới nhất.
- **Lỗi có thể gặp**: `400` (`602 VALIDATION_FAILED`), `401` (`101`/`200`/`201`).

---

### Nhóm C — Quản lý Nhân viên (`/api/v1/users/**` — Chỉ `LOGISTICS_MANAGER`)

> Mọi API trong nhóm này đều kiểm tra quyền `@PreAuthorize("hasRole('LOGISTICS_MANAGER')")`:
> - Chưa đăng nhập / token sai → `401`.
> - Đăng nhập bằng role khác (`CUSTOMER`, `DRIVER`, `ESCORT`, `FLEET_COORDINATOR`, `TRANSPORT_SPECIALIST`) → `403` (`102 FORBIDDEN`).

#### 11. Tạo tài khoản nhân viên
- **Endpoint**: `POST /api/v1/users`
- **Quyền**: `LOGISTICS_MANAGER`
- **Request Body** (`CreateUserRequest` — BE tự trim `fullName`, trim + lowercase `email`, `phone` rỗng thành `null`):

| Trường | Kiểu | Bắt buộc | Điều kiện validation |
|---|---|---|---|
| `fullName` | `string` | Có | `@NotBlank` ("Vui lòng nhập họ tên"), tối đa **150 ký tự** |
| `email` | `string` | Có | `@NotBlank` ("Vui lòng nhập email"), đúng `@Email`, tối đa **150 ký tự**, unique |
| `password` | `string` | Có | `@NotBlank` ("Vui lòng nhập mật khẩu"), đạt `@StrongPassword` |
| `phone` | `string` | Không | Nếu có: khớp SĐT Việt Nam `^(?:0|\+?84)[35789][0-9]{8}$` |
| `role` | `Role` | Có | `@NotNull` ("Vui lòng chọn vai trò nhân viên"), chỉ chấp nhận các role nhân viên (`LOGISTICS_MANAGER`, `TRANSPORT_SPECIALIST`, `FLEET_COORDINATOR`, `DRIVER`, `ESCORT`). **Cấm chọn `CUSTOMER`** |

- **Response `201 Created`**: Trả về `UserResponse` (`status: "ACTIVE"`, `emailVerified: true`).
- **Lỗi có thể gặp**:
  - `400` (`602 VALIDATION_FAILED`) hoặc `400` (`601 INVALID_REQUEST` nếu sai enum `role`).
  - `400` (`302 INVALID_STAFF_ROLE`): `"Vai trò nhân viên không hợp lệ"` (khi truyền `role = "CUSTOMER"`).
  - `409` (`301 EMAIL_ALREADY_EXISTS`): Email đã tồn tại.

---

#### 12. Danh sách & Tìm kiếm người dùng
- **Endpoint**: `GET /api/v1/users`
- **Quyền**: `LOGISTICS_MANAGER`
- **Query Params** (tất cả đều tùy chọn):

| Tham số | Kiểu | Mặc định | Mô tả |
|---|---|---|---|
| `role` | `Role` | - | Lọc chính xác theo vai trò |
| `status` | `UserStatus` | - | Lọc chính xác theo trạng thái (`ACTIVE`, `INACTIVE`, `LOCKED`) |
| `keyword` | `string` | - | Tìm gần đúng không phân biệt hoa/thường theo `fullName` hoặc `email` |
| `page` | `integer` | `0` | Số trang (bắt đầu từ `0`; truyền `< 0` tự về `0`) |
| `size` | `integer` | `20` | Kích thước trang (`1..100`; truyền `> 100` tự giới hạn còn `100`) |
| `sort` | `string` | `createdAt,desc` | Định dạng `trường,asc\|desc`. Trường hợp lệ: `createdAt`, `fullName`, `email`, `role`, `status` |

- **Response `200 OK`**: `PageResponse<UserResponse>`.
- **Lỗi có thể gặp**: `400` (`601 INVALID_REQUEST` nếu truyền sai tên trường `sort`, sai hướng `asc`/`desc`, hoặc sai enum `role`/`status`).

---

#### 13. Xem chi tiết một người dùng
- **Endpoint**: `GET /api/v1/users/{id}`
- **Quyền**: `LOGISTICS_MANAGER`
- **Path Variable**: `id` (`UUID`).
- **Response `200 OK`**: `UserResponse`.
- **Lỗi có thể gặp**: `400` (`601 INVALID_REQUEST` nếu `id` không phải UUID), `404` (`300 USER_NOT_FOUND`).

---

#### 14. Khóa / Vô hiệu hóa / Kích hoạt tài khoản
- **Endpoint**: `PATCH /api/v1/users/{id}/status`
- **Quyền**: `LOGISTICS_MANAGER`
- **Request Body** (`UpdateUserStatusRequest`):

| Trường | Kiểu | Bắt buộc | Điều kiện validation |
|---|---|---|---|
| `status` | `UserStatus` | Có | `@NotNull` ("Vui lòng chọn trạng thái tài khoản"): `ACTIVE`, `INACTIVE`, `LOCKED` |

- **Quy tắc nghiệp vụ**:
  - Không thể tự chuyển tài khoản của chính mình sang `INACTIVE` hoặc `LOCKED` (`CANNOT_DEACTIVATE_SELF`).
  - Không thể vô hiệu hóa / khóa `LOGISTICS_MANAGER` đang `ACTIVE` cuối cùng trong hệ thống (`LAST_MANAGER_PROTECTED`).
  - Khi chuyển sang `INACTIVE` hoặc `LOCKED`: BE tự động thu hồi mọi `refreshToken` đang hoạt động của user đó (`USER_STATUS_CHANGED`), đồng thời mọi request dùng `accessToken` cũ cũng bị `JwtAuthenticationFilter` chặn ngay (`403 ACCOUNT_DISABLED` hoặc `423 ACCOUNT_LOCKED`).
  - Khi chuyển về `ACTIVE`: BE tự reset luôn `failedLoginAttempts = 0` và `lockedUntil = null` để user đăng nhập lại được ngay.
- **Response `200 OK`**: `UserResponse` đã cập nhật.
- **Lỗi có thể gặp**:
  - `404` (`300 USER_NOT_FOUND`)
  - `409` (`303 CANNOT_DEACTIVATE_SELF`): `"Bạn không thể tự vô hiệu hóa tài khoản của mình"`
  - `409` (`304 LAST_MANAGER_PROTECTED`): `"Không thể vô hiệu hóa quản lý điều hành cuối cùng của hệ thống"`

---

## 6. Lưu ý cài đặt HTTP Client (Axios / Fetch) phía Frontend

1. **Tự động đính kèm Access Token**: Mọi API cần đăng nhập (`/auth/logout-all`, `/auth/change-password`, `/profile`, `/users/**`, và các API nghiệp vụ sau này) gắn header `Authorization: Bearer <accessToken>`. Không gắn hoặc bỏ qua cho các endpoint công khai (`/auth/register`, `/auth/login`, `/auth/refresh`, `/auth/logout`, `/auth/forgot-password`, `/auth/reset-password`).
2. **Xử lý khi nhận `401` (`code: 201 TOKEN_EXPIRED`)**:
   - Dùng cơ chế **1 promise refresh dùng chung (single-flight)**: nếu trang đang gọi 3 API song song đều bị `401` vì hết hạn access token, chỉ gọi `POST /api/v1/auth/refresh` **đúng 1 lần**, lưu đè ngay cặp `accessToken` + `refreshToken` mới, rồi retry 3 request kia.
   - Tuyệt đối không gọi `/refresh` 2 lần với cùng 1 `refreshToken` cũ (sẽ kích hoạt chống đánh cắp `203 REFRESH_TOKEN_REVOKED` và văng khỏi phiên).
   - Nếu chính request `/refresh` trả `401` (`202` / `203`), `403` (`104`), hoặc `423` (`103`), xóa hết token ở storage và chuyển hướng về `/login`.
3. **Hiển thị lỗi form (`400`, `code: 602`)**:
   - Đọc object `error.errors` (ví dụ `errors.email`, `errors.password`, `errors.phone`) để gắn trực tiếp dưới từng ô input tương ứng; nếu không có `errors` thì hiển thị `error.message`.
4. **Sau `change-password`, `reset-password`, `logout`, `logout-all`**:
   - Xóa `accessToken`, `refreshToken` và user cache ở FE, đưa về màn hình đăng nhập.

