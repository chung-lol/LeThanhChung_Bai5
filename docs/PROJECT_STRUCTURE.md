# Cấu trúc mã nguồn

- `data/model`: Course và Category ánh xạ JSON.
- `data/remote`: Retrofit client, cấu hình URL lưu bằng SharedPreferences và khai báo REST endpoints.
- `data/repository`: lớp thao tác dữ liệu; CategoryRepository chịu trách nhiệm kiểm tra quan hệ trước xóa.
- `ui/common/BaseActivity`: giao diện chung, coroutine theo lifecycle, loading và thông báo lỗi.
- `ui/course`: danh sách/tìm kiếm/lọc, chi tiết/player, form thêm/sửa.
- `ui/category`: danh sách danh mục và dialog CRUD.
- `utils/Validation`: quy tắc kiểm tra dữ liệu.
- `scripts/seed.mjs`: nạp mẫu lên API, ánh xạ ID thật, không thay dữ liệu API bằng hardcode trong app.
- `app/src/test`: unit test và kiểm thử repository với HTTP server giả lập.

UI được dựng bằng Android Views trong Kotlin. Activity gọi Retrofit suspend functions trong lifecycleScope; Retrofit thực hiện mạng bất đồng bộ. Gson ánh xạ response thành model. Danh sách kết hợp categories.id với courses.categoryId để hiện tên; không hardcode tên danh mục.

Luồng xóa danh mục: người dùng xác nhận → CategoryRepository GET /courses → nếu có tham chiếu hoặc API lỗi thì dừng → DELETE /categories/{id} → GET /categories để làm mới.

Player được giải phóng khi đóng dialog hoặc Activity dừng, tránh tiếp tục phát âm thanh khi rời màn hình.
