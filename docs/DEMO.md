# Kịch bản video thuyết trình (7–10 phút)

1. Giới thiệu đề tài, Kotlin, Retrofit/Gson, Coil, Media3. Mở MockAPI cho thấy hai resource và quan hệ categoryId.
2. Chạy `node scripts/seed.mjs`, kiểm tra tối thiểu 3 danh mục/10 khóa học trên API.
3. Mở app: chỉ rõ ảnh, tên, giảng viên, danh mục và học phí. Tìm Kotlin, chọn danh mục Web, xóa tìm kiếm và đổi bộ lọc.
4. Mở chi tiết; giải thích GET /courses/{id}, GET /categories và ghép ID. Phát trailer, thử tạm dừng/tua và đóng.
5. Thêm khóa học: chọn danh mục từ API, nhập ảnh xem trước và URL MP4. Thử bỏ trống tên, học phí âm/không hợp lệ, thời lượng 0. Nhập hợp lệ và lưu; mở MockAPI xác nhận POST đã tạo bản ghi.
6. Sửa tên/học phí/danh mục; giải thích PUT và việc GET lại danh sách. Xóa: bấm Hủy trước, sau đó xác nhận; kiểm tra bản ghi biến mất.
7. Danh mục: thêm và sửa một danh mục thử. Thử xóa danh mục Mobile đang chứa khóa học: app phải chặn. Xóa danh mục thử không có khóa học: thành công.
8. Tắt Internet rồi Tải lại để thấy lỗi; bật lại và thử lại. Tìm chuỗi không tồn tại để thấy trạng thái rỗng. Xoay màn hình trong form để kiểm tra bản nháp.
9. Mở MockApiService, Validation, CategoryRepository và BaseActivity; giải thích JSON → model, coroutine, HTTP errors và kiểm tra quan hệ phía ứng dụng.
10. Kết thúc bằng repository, README và cách build APK.

## Checklist kiểm thử thủ công

- [ ] Các GET/POST/PUT/DELETE thay đổi đúng dữ liệu trên MockAPI.
- [ ] Tên danh mục phản ánh tên đã sửa trên backend.
- [ ] Tìm kiếm và lọc kết hợp chính xác.
- [ ] Ảnh xem trước và trailer MP4 hiển thị trên thiết bị.
- [ ] Hủy xóa không làm thay đổi dữ liệu.
- [ ] Không xóa được danh mục có khóa học; mất mạng không bỏ qua kiểm tra.
- [ ] Danh sách được tải lại khi quay về; các lỗi có thể thử lại.
- [ ] Bản nháp form còn sau khi xoay màn hình.

Các mục này là checklist cần chạy thực tế, không phải tuyên bố đã kiểm thử trên thiết bị.
