# Hướng dẫn dự án UmaStar (AI / Contributor Rules)

## Tự động đồng bộ tài liệu Markdown theo toàn bộ Backend
Mỗi khi thêm, sửa hoặc xóa bất kỳ file nào trong `src/main/java/umastar/**` (ví dụ: `controller`, `service`, `repository`, `dto`, `entity`, `enums`, `security`, `config`, `exception`, ...) hoặc `src/main/resources/**`:

1. **Chạy script đồng bộ tự động** (quét toàn bộ thư mục backend):
   ```bash
   java scripts/SyncDocs.java
   ```
   Script sẽ tự động cập nhật các khối `AUTO-GENERATED` trong `PROJECT_SPEC.md`, `ENTITY_SCHEMA.md` và `BACKEND_ROADMAP.md`.
2. **Cập nhật mô tả nghiệp vụ & tiến độ**:
   - Viết/cập nhật Javadoc (`/** ... */`) ngay trên class, method hoặc field trong mã nguồn Java để script tự trích xuất vào Markdown.
   - Nếu thay đổi Actor, Main Features hoặc 6 Core Business Flows, cập nhật phần mô tả tương ứng trong `PROJECT_SPEC.md` và `ENTITY_SCHEMA.md`.
   - Khi hoàn thành hạng mục nào, đánh dấu `[x]` vào dòng tương ứng trong `BACKEND_ROADMAP.md`.
3. **Không hardcode cấu hình**: Mọi biến môi trường khai báo trong `.env` và `.env.example`, tham chiếu qua `${...}` trong `src/main/resources/application.properties`.

