# UmaStar Backend — Roadmap & Checklist Chức năng

Tài liệu theo dõi tiến độ triển khai Backend theo thứ tự ưu tiên **P0 → P10**.
- Đánh dấu `[x]` vào ô **Done** khi chức năng đã code + test xong.
- Chạy `java scripts/SyncDocs.java` để tự động cập nhật bảng thống kê tiến độ và danh sách API/Service/Repository thực tế ở cuối file.

---

## Chuẩn bị nền tảng (Pre-P0)
| Done | Hạng mục | Chi tiết / Ghi chú |
|---|---|---|
| [x] | Đồng bộ package & cấu hình Postgres + Swagger | `umastar.*`, PostgreSQL (`umastar_db`), SpringDoc `3.2.1` |
| [x] | Cấu hình `.env` & `.env.example` không hardcode | `spring.config.import=optional:file:.env[.properties]` |
| [x] | Bổ sung Optimistic Locking & field còn thiếu trên Entity | `@Version` (`TransportOrder`, `Trip`, `Vehicle`, `LegalDocument`), `Trip.confirmed`/`confirmedAt`, `HorseConditionLog.photoUrls` |
| [x] | Thêm `spring-boot-starter-security` + thư viện JWT vào `pom.xml` | Khai báo thêm `JWT_SECRET`, `JWT_ACCESS_EXP_MS`, `JWT_REFRESH_EXP_MS`, `UPLOAD_DIR`, `MAX_FILE_SIZE`, `MAX_REQUEST_SIZE` trong `.env` & `.env.example` |

---

## P0 — Nền tảng chung, Auth & Tài khoản
> Mục tiêu: Mọi API dùng `/api/v1`, chuẩn JSON lỗi thống nhất, JWT stateless, `@PreAuthorize`, upload file cơ bản.

| Done | Chức năng / Thành phần | API / Vị trí | Ai dùng | Validation & Quy tắc chính |
|---|---|---|---|---|
| [x] | Chuẩn hóa Response lỗi & Phân trang | `dto/response`, `GlobalExceptionHandler`, `PageableFactory` | Hệ thống | Trả `{timestamp, status, code, error, message, path, errors{field}}` cho 400, 401, 403, 404, 405, 409, 413, 415, 422, 423; phân trang `page, size (max 100), sort` |
| [x] | Hạ tầng Upload File | `service/FileStorageService` + `service/impl/LocalFileStorageService` | Hệ thống | Lưu local theo `UPLOAD_DIR` (dễ mở rộng S3), whitelist đuôi & MIME + magic bytes, giới hạn dung lượng, đổi tên UUID |
| [x] | Cấu hình Security, CORS, JWT Filter & Swagger Bearer | `SecurityConfig`, `CorsConfig`, `JwtAuthenticationFilter`, `JwtService`, `OpenApiConfig` | Hệ thống | Stateless, BCrypt, CORS theo `CORS_ALLOWED_ORIGINS`, trả JSON 401/403, `permitAll` cho `/api/v1/auth/**` (trừ `/logout-all`, `/change-password`) & Swagger UI |
| [x] | Seed tài khoản mẫu theo từng Role | `DataSeeder`, `SeedProperties` | Hệ thống | Tạo sẵn tài khoản cho từng `Role` cấu hình trong `.env` (kèm `Customer` profile cho `CUSTOMER`) nếu DB chưa có |
| [ ] | Đăng ký khách hàng | `POST /api/v1/auth/register` | Công khai | Email lowercase, unique, ≤ 150 ký tự; mật khẩu 8–72 ký tự (hoa, thường, số); phone `+?[0-9]{8,15}`; tạo `User(CUSTOMER)` + `Customer` |
| [ ] | Đăng nhập | `POST /api/v1/auth/login` | Công khai | Account phải `ACTIVE`; kiểm tra `lockedUntil`; sai 5 lần khóa 15 phút; thành công reset counter + ghi `lastLoginAt`; lỗi chung chung |
| [ ] | Làm mới token (Rotation) | `POST /api/v1/auth/refresh` | Công khai | Tìm SHA-256 hash, chưa revoke, chưa hết hạn; thu hồi token cũ, gán `replacedByTokenId`, cấp mới cùng `familyId`; dùng lại token đã revoke → revoke cả family |
| [ ] | Đăng xuất | `POST /api/v1/auth/logout`, `/logout-all` | Đã đăng nhập | Revoke token hiện tại hoặc toàn bộ token của user (ghi `revokedReason`) |
| [ ] | Quên / Đặt lại mật khẩu | `POST /api/v1/auth/forgot-password`, `/reset-password` | Công khai | Luôn trả 200; lưu `resetTokenHash`, `resetTokenExpiresAt`, `resetRequestedAt` trên `users` (hạn 15–30 phút, dùng xong xóa về `null`); đổi xong revoke hết refresh token; rate limit |
| [ ] | Đổi mật khẩu | `POST /api/v1/auth/change-password` | Đã đăng nhập | Kiểm tra mật khẩu cũ, mật khẩu mới khác mật khẩu cũ và đạt chính sách; cập nhật `passwordChangedAt` |
| [ ] | Xem / Sửa hồ sơ cá nhân | `GET/PUT /api/v1/me` | Đã đăng nhập | Không cho sửa `email` và `role`; validate `phone` |
| [ ] | Quản lý nhân viên | `POST/GET /api/v1/users`, `PATCH /api/v1/users/{id}/status` | `LOGISTICS_MANAGER` | Role hợp lệ (`Role` enum); không tự khóa mình, không khóa Manager cuối cùng; khi `INACTIVE` revoke refresh token; lọc role/status/keyword |

---

## P1 — Danh mục (Country, City, Vehicle, Regulation)
| Done | Chức năng | API | Ai dùng | Validation & Quy tắc chính |
|---|---|---|---|---|
| [ ] | Danh sách nước theo loại đơn | `GET /api/v1/countries?scope=DOMESTIC\|INTERNATIONAL` | Mọi role | `DOMESTIC`: `homeCountry=true`; `INTERNATIONAL`: `partner=true` & không phải nước sở tại; chỉ `active=true` |
| [ ] | Danh sách thành phố theo nước | `GET /api/v1/countries/{id}/cities` | Mọi role | Chỉ lấy `active=true` của đúng nước đó |
| [ ] | CRUD Quốc gia / Thành phố | `/api/v1/admin/countries`, `/api/v1/admin/cities` | `LOGISTICS_MANAGER` | `isoCode` 2 chữ in hoa unique; tối đa 1 `homeCountry`; `timezone` hợp lệ (`ZoneId`); city không trùng tên trong 1 nước; soft delete `active=false` |
| [ ] | CRUD Phương tiện | `/api/v1/vehicles` | `FLEET_COORDINATOR` | `codeOrPlate` unique; `capacityHorses ≥ 1`; không chuyển `MAINTENANCE` khi `IN_USE`; không xóa nếu đã có chuyến |
| [ ] | CRUD Quy định kiểm dịch | `/api/v1/regulations` | `TRANSPORT_SPECIALIST` | Nước (và `city` nếu có) tồn tại; `requiredDocTypes` không rỗng; `quarantineDays ≥ 0`; `effectiveDate` hợp lệ; soft delete `active=false` |

---

## P2 — Customer & Horse (Flow 1 nền)
| Done | Chức năng | API | Ai dùng | Validation & Quy tắc chính |
|---|---|---|---|---|
| [ ] | Hồ sơ khách hàng | `GET/PUT /api/v1/customers/me`, `GET /api/v1/customers` | `CUSTOMER`, `LOGISTICS_MANAGER` | `organizationName` bắt buộc; kiểm `taxCode` nếu có, `countryId` tồn tại; Manager xem danh sách có lọc |
| [ ] | CRUD Ngựa đua | `/api/v1/horses` | `CUSTOMER` (của mình), `LOGISTICS_MANAGER` & `TRANSPORT_SPECIALIST` (xem) | `microchipNo` bắt buộc, unique, 15 chữ số; `birthDate` không ở tương lai & tuổi ≤ 40; `weightKg` 100–1000; chỉ sửa ngựa của mình; không xóa khi đang nằm trong đơn hoạt động |

---

## P3 — Notification (Nội bộ + API đọc)
| Done | Chức năng | API / Service | Ai dùng | Validation & Quy tắc chính |
|---|---|---|---|---|
| [ ] | Service gửi thông báo nội bộ | `NotificationService.notify(...)` | Hệ thống | Gắn `orderId` / `tripId` khi có; dùng chung cho P4–P9 |
| [ ] | Danh sách & đếm chưa đọc | `GET /api/v1/notifications`, `/unread-count` | Mọi role | Chỉ thấy của chính mình, phân trang, lọc `unreadOnly` |
| [ ] | Đánh dấu đã đọc | `PATCH /api/v1/notifications/{id}/read`, `/read-all` | Mọi role | Kiểm tra quyền sở hữu thông báo, set `read=true`, `readAt` |

---

## P4 — Đơn vận chuyển & Phê duyệt (Flow 1 — Mốc Demo 1)
| Done | Chức năng | API | Ai dùng | Validation & Quy tắc chính |
|---|---|---|---|---|
| [ ] | State Machine `OrderStatus` | `OrderStateMachine` | Hệ thống | Kiểm soát chuyển trạng thái tập trung, sai trả `409 Conflict` |
| [ ] | Tạo đơn vận chuyển | `POST /api/v1/orders` | `CUSTOMER` | Tự gán `transportType`; check nước/city/không trùng điểm đi-đến; `requestedArrival > requestedDeparture`; đặt trước tối thiểu N ngày; `horseCount = horses.size() ≥ 1`; ngựa thuộc customer & không trùng lịch đơn khác; sinh `orderCode` |
| [ ] | Xem danh sách & chi tiết đơn | `GET /api/v1/orders`, `GET /api/v1/orders/{id}` | `CUSTOMER` (của mình), Nhân viên | Dùng `@EntityGraph`/`JOIN FETCH`; lọc `status`, `transportType`, khoảng ngày, từ khóa |
| [ ] | Sửa đơn | `PUT /api/v1/orders/{id}` | `CUSTOMER` | Chỉ sửa khi `PENDING`; áp lại toàn bộ validation lúc tạo |
| [ ] | Hủy đơn | `POST /api/v1/orders/{id}/cancel` | `CUSTOMER`, `LOGISTICS_MANAGER` | Customer hủy khi `PENDING`/`APPROVED`/`DOC_PROCESSING`; từ `READY` chỉ Manager hủy; không hủy từ `IN_TRANSIT`; bắt buộc lý do |
| [ ] | Duyệt đơn | `POST /api/v1/orders/{id}/approve` | `LOGISTICS_MANAGER` | Chỉ khi `PENDING`; `quotedPrice > 0` & có `currency`; ghi `approvedBy`, `approvedAt`; tự sinh `LegalDocument` (`MISSING`) theo `Regulation` nước đến; chuyển `DOC_PROCESSING` (hoặc `READY` nếu không yêu cầu giấy tờ); notify Customer & Specialist |
| [ ] | Từ chối đơn | `POST /api/v1/orders/{id}/reject` | `LOGISTICS_MANAGER` | Chỉ khi `PENDING`; bắt buộc `rejectReason`; notify Customer |

---

## P5 — Hồ sơ Pháp lý & Kiểm dịch (Flow 2)
| Done | Chức năng | API | Ai dùng | Validation & Quy tắc chính |
|---|---|---|---|---|
| [ ] | State Machine `ApprovalStatus` | `ApprovalStateMachine` | Hệ thống | `MISSING → SUBMITTED → UNDER_REVIEW → APPROVED/REJECTED`, sai trả `409` |
| [ ] | Xem checklist giấy tờ của đơn | `GET /api/v1/orders/{id}/documents` | `CUSTOMER` (của mình), `TRANSPORT_SPECIALIST`, `LOGISTICS_MANAGER` | Nhóm theo ngựa, hiển thị `approvalStatus` & giấy tờ thiếu |
| [ ] | Tải giấy tờ lên | `POST /api/v1/documents/{id}/upload` | `CUSTOMER`, `TRANSPORT_SPECIALIST` | PDF/JPG/PNG tối đa 10 MB; `docNumber` bắt buộc; `issueDate` không ở tương lai; `expiryDate > issueDate` & còn hạn tới ngày đến dự kiến; `OTHER` bắt buộc `docTypeOther`; chuyển `SUBMITTED`, set `uploadedBy`, `submittedAt` |
| [ ] | Nộp cơ quan chức năng | `PATCH /api/v1/documents/{id}/submit` | `TRANSPORT_SPECIALIST` | Chuyển `SUBMITTED → UNDER_REVIEW`; `agency` và `agencyRefNo` bắt buộc |
| [ ] | Cập nhật kết quả duyệt & tự đẩy `READY` | `PATCH /api/v1/documents/{id}/review` | `TRANSPORT_SPECIALIST` | `UNDER_REVIEW → APPROVED` hoặc `REJECTED` (bắt buộc `rejectReason`); ghi `verifiedBy`, `approvedAt`; từ chối thì notify Customer & đưa đơn về `DOC_PROCESSING`; đủ 100% `APPROVED` → đơn sang `READY` + notify `CLEARANCE_DONE` |
| [ ] | Nhắc bổ sung giấy tờ | `POST /api/v1/orders/{id}/documents/remind` | `TRANSPORT_SPECIALIST` | Chỉ gửi khi còn `MISSING`/`REJECTED`; tối đa 1 lần/ngày/đơn; notify `DOC_REQUIRED` |

---

## P6 — Lập kế hoạch chuyến, Điểm dừng & Phân công (Flow 3)
| Done | Chức năng | API | Ai dùng | Validation & Quy tắc chính |
|---|---|---|---|---|
| [ ] | Tạo / Sửa / Hủy chuyến | `POST /api/v1/orders/{orderId}/trips`, `PUT /api/v1/trips/{id}`, `POST /api/v1/trips/{id}/cancel` | `LOGISTICS_MANAGER` | Đơn ở `APPROVED`/`DOC_PROCESSING`/`READY`; xe `AVAILABLE`, đủ `capacityHorses`, không trùng lịch; `plannedEnd > plannedStart`, `plannedStart` không ở quá khứ; `coordinator` có role `FLEET_COORDINATOR`; sửa/hủy chỉ khi `PLANNED` |
| [ ] | Quản lý điểm dừng | `POST/PUT/DELETE /api/v1/trips/{id}/stops`, `PATCH /api/v1/trips/{id}/stops/reorder` | `FLEET_COORDINATOR`, `LOGISTICS_MANAGER` | `sequenceNo` liên tục từ 1; đầu `ORIGIN`, cuối `DESTINATION`; `plannedArrival` tăng dần trong khoảng chuyến; `OTHER` bắt buộc `customTypeName`; tọa độ hợp lệ; quốc tế có ≥ 1 `CUSTOMS`; chỉ sửa khi `PLANNED` |
| [ ] | Phân công nhân sự | `POST /api/v1/trips/{id}/assignments`, `DELETE /api/v1/trips/{id}/assignments/{id}` | `LOGISTICS_MANAGER` | User `ACTIVE`, role khớp `assignmentRole`; không trùng người/vai trên 1 chuyến; không trùng lịch chuyến khác; đường bộ ≥ 1 `DRIVER`, mọi chuyến ≥ 1 `ESCORT` |
| [ ] | Xác nhận kế hoạch chuyến | `POST /api/v1/trips/{id}/confirm` | `LOGISTICS_MANAGER` | Đủ điểm dừng + nhân sự tối thiểu; set `confirmed=true`, `confirmedAt`; notify người được phân công |
| [ ] | Xem chuyến được phân công | `GET /api/v1/my/trips`, `GET /api/v1/my/trips/{id}` | `DRIVER`, `ESCORT` | Chỉ xem chuyến được phân công; trả lịch trình, điểm dừng, ngựa kèm `stallNumber` |

---

## P7 — Theo dõi Trạng thái & Nhật ký ngựa (Flow 4 — Mốc Demo 2)
| Done | Chức năng | API | Ai dùng | Validation & Quy tắc chính |
|---|---|---|---|---|
| [ ] | State Machine `TripStatus` | `TripStateMachine` | Hệ thống | Kiểm soát luồng `PLANNED → DEPARTED → IN_TRANSIT → DELIVERED / CANCELLED` |
| [ ] | Bắt đầu chuyến | `POST /api/v1/trips/{id}/start` | `DRIVER`, `ESCORT` | Chuyến `PLANNED` & `confirmed=true`; đơn phải `READY`; xe `AVAILABLE → IN_USE`; ghi `actualStart`, chuyến `DEPARTED`, đơn `IN_TRANSIT`, tạo log `DEPARTED` |
| [ ] | Báo cáo mốc lộ trình | `POST /api/v1/trips/{id}/status-logs` | `DRIVER`, `ESCORT`, `FLEET_COORDINATOR` | Người báo phải được phân công (hoặc Coordinator nhập thay); check thứ tự `stopId`, `CUSTOMS_CLEARED` chỉ tại `CUSTOMS`, `DELIVERED` chỉ tại `DESTINATION`; không trùng trạng thái; `reportedAt` không tương lai (> 5p) & không quá 24h; cập nhật stop + chuyến sang `IN_TRANSIT` |
| [ ] | Cập nhật tọa độ GPS | `POST /api/v1/trips/{id}/location` | `DRIVER`, `ESCORT` | Chuyến `DEPARTED`/`IN_TRANSIT`; tọa độ hợp lệ; throttle tối thiểu 10 giây/lần; lưu `currentLat`, `currentLng`, `lastLocationAt` |
| [ ] | Điều chỉnh trạng thái điểm dừng thủ công | `PATCH /api/v1/trips/{id}/stops/{stopId}/status` | `FLEET_COORDINATOR` | Bắt buộc lý do, ghi `TripStatusLog(manual=true)` để truy vết |
| [ ] | Ghi nhận & xem nhật ký sức khỏe ngựa | `POST/GET /api/v1/trips/{id}/horse-conditions` | `DRIVER`, `ESCORT` (ghi); `CUSTOMER` (đơn mình), Staff (xem) | Ngựa thuộc đơn của chuyến; `NOT_EATING`/`STRESSED` bắt buộc `note`; tối đa 5 ảnh JPG/PNG ≤ 5 MB (`photoUrls`); bất thường → notify Coordinator |
| [ ] | Khách theo dõi lộ trình | `GET /api/v1/orders/{id}/tracking` | `CUSTOMER` (của mình) | Trả điểm dừng, log trạng thái, GPS cuối + `lastLocationAt`, tóm tắt sức khỏe ngựa |

---

## P8 — Sự cố & Đổi lộ trình khẩn cấp (Flow 5)
| Done | Chức năng | API | Ai dùng | Validation & Quy tắc chính |
|---|---|---|---|---|
| [ ] | Báo cáo sự cố | `POST /api/v1/trips/{id}/incidents` | `DRIVER`, `ESCORT`, `FLEET_COORDINATOR` | Chuyến `DEPARTED`/`IN_TRANSIT`; `description` 10–2000 ký tự; `HORSE_MEDICAL` bắt buộc `horseId` thuộc chuyến; notify Coordinator & Manager (`HIGH`/`CRITICAL` báo cả Customer) |
| [ ] | Danh sách & xử lý sự cố | `GET /api/v1/incidents`, `GET /api/v1/incidents/{id}`, `PATCH /api/v1/incidents/{id}/status` | `FLEET_COORDINATOR`, `LOGISTICS_MANAGER`, `DRIVER`/`ESCORT` | State Machine `OPEN → HANDLING → RESOLVED`; `RESOLVED` bắt buộc `resolutionNote`, ghi `resolvedBy`, `resolvedAt` |
| [ ] | Đề xuất đổi lộ trình | `POST /api/v1/incidents/{id}/route-change` | `FLEET_COORDINATOR` | `reason`, `newRouteSummary` bắt buộc; `extraCost ≥ 0`; `newEta > now`; tối đa 1 yêu cầu `PENDING` mỗi chuyến |
| [ ] | Duyệt / Từ chối đổi lộ trình | `POST /api/v1/route-changes/{id}/approve`, `/reject` | `LOGISTICS_MANAGER` | Phải `PENDING`; từ chối bắt buộc `decisionNote`; duyệt cập nhật `TripStop` + `plannedEnd = newEta` trong 1 transaction, ghi nhận `extraCost`, notify các bên |

---

## P9 — Bàn giao & Nghiệm thu (Flow 6)
| Done | Chức năng | API | Ai dùng | Validation & Quy tắc chính |
|---|---|---|---|---|
| [ ] | Tạo biên bản bàn giao | `POST /api/v1/trips/{id}/handover` | `DRIVER`, `ESCORT` | Chuyến đã `ARRIVED` tại `DESTINATION`; mỗi chuyến 1 handover; bắt buộc `receiverName`, `receiverPhone`, `horseConditionOnArrival`, `signatureUrl`, `photoUrl`; chuyển chuyến `DELIVERED`, ghi `actualEnd`, trả xe `AVAILABLE`; đủ mọi chuyến `DELIVERED` → đơn `DELIVERED` + notify Customer |
| [ ] | Khách xác nhận & đánh giá | `POST /api/v1/handovers/{id}/confirm` | `CUSTOMER` (đơn mình) | Chưa xác nhận; `rating` 1–5 (nếu có), `feedback` ≤ 2000 ký tự; ghi `confirmedAt`, đơn thành `COMPLETED` |
| [ ] | Chốt chi phí thực tế | `PATCH /api/v1/orders/{id}/final-cost` | `LOGISTICS_MANAGER` | Đơn `DELIVERED` hoặc `COMPLETED`; `actualCost ≥ 0` |

---

## P10 — Báo cáo Manager & Tác vụ chạy nền
| Done | Chức năng | API / Job | Ai dùng | Validation & Quy tắc chính |
|---|---|---|---|---|
| [ ] | Báo cáo doanh thu | `GET /api/v1/reports/revenue?from&to&groupBy` | `LOGISTICS_MANAGER` | `from ≤ to`, tối đa 366 ngày; `groupBy ∈ {month, customer, transportType}`; tổng `quotedPrice` đơn `COMPLETED`, gom theo `currency` |
| [ ] | Báo cáo chi phí & On-time delivery & Tổng quan | `GET /api/v1/reports/costs`, `/on-time-delivery`, `/summary` | `LOGISTICS_MANAGER` | `actualCost` vs `extraCost`; tỷ lệ `actualEnd ≤ plannedEnd + tolerance`; dashboard tổng quan |
| [ ] | Xuất file báo cáo (tùy chọn) | `GET /api/v1/reports/*/export` | `LOGISTICS_MANAGER` | CSV / Excel |
| [ ] | Job nền tự động | `@Scheduled` (`ScheduledJobs`) | Hệ thống | Dọn `refresh_tokens` hết hạn/revoked & `resetToken*` quá hạn (ngày); nhắc giấy tờ trước 7 & 3 ngày (ngày); cảnh báo chuyến trễ (15 phút); tự xác nhận bàn giao sau N ngày; nhắc đơn `PENDING` quá N ngày |

---

## Tiến độ tự động phát hiện từ Code (Auto-generated)

<!-- AUTO-GENERATED-PROGRESS:START -->
> Tự động cập nhật bởi `java scripts/SyncDocs.java`.

- **Tiến độ checklist:** `8/62` hạng mục (13%)
- **Thành phần đã có trong code:** `0` Controllers (`0` endpoints), `2` Services, `3` Repositories
<!-- AUTO-GENERATED-PROGRESS:END -->
