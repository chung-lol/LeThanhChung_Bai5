# LeThanhChung_Bai5 — Quản lý khóa học

Ứng dụng Android Kotlin, gọi REST API MockAPI bằng Retrofit/Gson, tải ảnh bằng Coil và phát trailer ngay trong ứng dụng bằng Media3 ExoPlayer.

## Bản web chạy localhost

Bản web nằm trong `web/`, dùng HTML/CSS/JavaScript và Node.js 20+ để phục vụ tệp tĩnh, không cần cài thư viện.

```powershell
cd web
npm start
```

Mở **http://localhost:3000**. Nhấn `Ctrl+C` trong terminal để dừng server. Kiểm tra cú pháp bằng `npm run check`.

Bản web có danh sách/tìm kiếm/lọc, chi tiết và video HTML5, form thêm/sửa có xem trước ảnh, CRUD danh mục, xác nhận xóa và kiểm tra danh mục có khóa học. Dữ liệu lấy trực tiếp từ cùng MockAPI với Android: thay đổi trên web sẽ xuất hiện trên Android khi tải lại. Có thể đổi Base URL bằng nút **Cấu hình API**; cấu hình lưu trong trình duyệt. Server chỉ lắng nghe loopback trên máy, không cung cấp backend thay thế MockAPI. Cần Internet để tải dữ liệu, ảnh, video và font.

Đã kiểm tra cú pháp JavaScript, HTTP 200 của trang và các tài nguyên, cùng CORS của MockAPI. Chưa kiểm thử tương tác trực quan trong trình duyệt do công cụ trình duyệt không khả dụng trong phiên làm việc.

## Chạy ứng dụng

1. Mở thư mục chứa `settings.gradle.kts` bằng Android Studio.
2. Chọn Gradle JDK **17**, cài Android SDK Platform **35**, đồng bộ Gradle.
3. Chạy trên thiết bị hoặc emulator Android **8.0 (API 26)** trở lên, có Internet.
4. Base URL mặc định: **https://6ad796ea95e98f36aef2ab6b.mockapi.io/api/v1/**. Có thể đổi bằng nút **Cấu hình API** trên màn hình chính; địa chỉ được lưu trên thiết bị.

Build bằng terminal (JAVA_HOME trỏ tới JDK 17, ANDROID_HOME trỏ tới Android SDK):

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug
```

macOS/Linux: `./gradlew assembleDebug testDebugUnitTest lintDebug`.
APK: `app/build/outputs/apk/debug/app-debug.apk`. GitHub Actions cũng build và đính kèm APK trong Artifacts khi workflow chạy thành công.

## Chuẩn bị backend và dữ liệu

Mở [MockAPI](https://mockapi.io/projects) và tạo hai resource độc lập, tên chính xác `categories`, `courses`, dưới prefix `/api/v1`. Không bật quan hệ nested resource.

| Resource | Trường |
| --- | --- |
| categories | id (tự sinh), name (String), description (String) |
| courses | id (tự sinh), title, description, categoryId, instructor, thumbnail, trailerUrl, createdAt (String); price, duration (Number) |

Endpoints:
- [Danh mục](https://6ad796ea95e98f36aef2ab6b.mockapi.io/api/v1/categories)
- [Khóa học](https://6ad796ea95e98f36aef2ab6b.mockapi.io/api/v1/courses)

Sau khi hai endpoint GET trả về mảng JSON (có thể là `[]`), chạy Node.js 20+:

```powershell
node scripts/seed.mjs
# Hoặc project khác:
node scripts/seed.mjs https://YOUR_PROJECT_ID.mockapi.io/api/v1
```

Script tạo 3 danh mục và 10 khóa học, lấy ID thật của danh mục để gán `categoryId`; chạy lại sẽ bỏ qua tên đã tồn tại, không xóa/ghi đè dữ liệu. Các JSON trong `mockapi/` chỉ dùng nạp backend, ứng dụng luôn lấy dữ liệu từ API. Video mẫu là clip minh họa phát MP4, không phải trailer được sản xuất riêng cho từng khóa học.

Nếu API trả 404: kiểm tra project, prefix và tên resource trong MockAPI trước. Nếu danh sách rỗng: chạy script seed hoặc thêm bằng ứng dụng.

## Chức năng

- Danh sách có ảnh, tên, giảng viên, tên danh mục lấy từ API, học phí VNĐ; tìm theo tên và lọc danh mục kết hợp.
- Chi tiết có toàn bộ thông tin, ảnh, ngày tạo, phát trailer có điều khiển trong ứng dụng, sửa và xác nhận xóa.
- Form thêm/sửa lấy danh mục qua API, xem trước ảnh URL, kiểm tra trường bắt buộc, số hữu hạn không âm, thời lượng nguyên dương, URL HTTPS và ngày `yyyy-MM-dd`.
- Danh mục có mục “Chọn danh mục…” để tránh tự gán sai khi danh mục cũ đã bị xóa. Nút tải lại danh mục giữ nội dung form đang nhập.
- Form dùng nhãn nổi, nút Material và khoảng cách theo mật độ màn hình; nội dung tránh bàn phím khi nhập liệu.
- Kiểm tra lại danh mục trước khi POST/PUT. Lưu bản nháp form khi xoay màn hình.
- CRUD danh mục. Trước DELETE luôn GET khóa học và chặn nếu danh mục đang được tham chiếu; lỗi API cũng chặn xóa.
- Có loading, lỗi kết nối/API, nút tải lại, trạng thái rỗng; đọc lại danh sách sau khi quay về từ thêm/sửa/xóa.

MockAPI không có transaction/khóa ngoại: kiểm tra phía ứng dụng không bảo đảm toàn vẹn nếu nhiều thiết bị ghi đồng thời. Chỉ dùng URL HTTPS; URL trang YouTube không được hỗ trợ, hãy dùng MP4 trực tiếp. Nếu tải ảnh lỗi sẽ hiện biểu tượng thay thế; lỗi video có thông báo.

## Kiểm thử và thuyết trình

`ValidationTest` kiểm tra biên học phí, URL, ngày, danh mục và trường bắt buộc. `CategoryRepositoryTest` dùng MockWebServer để xác nhận thứ tự GET/DELETE, chặn xóa danh mục có khóa học và chặn khi backend lỗi.

`CourseApiTest` kiểm tra POST gửi đúng JSON, không gửi ID tự sinh và đọc ID trả về từ server.

Kiểm tra backend đang hoạt động (chỉ đọc dữ liệu, cần Node.js 20+):

```powershell
node scripts/verify-api.mjs
```

Script kiểm tra tối thiểu 3 danh mục/10 khóa học, dữ liệu bắt buộc, liên kết danh mục, endpoint chi tiết và HTTP/content-type của mọi URL ảnh/video. Đây là kiểm tra kết nối media, không thay thế kiểm thử phát video trên thiết bị.

Ngày 22/10/2026, URL trailer mẫu cũ `media.w3.org/2010/05/sintel/trailer.mp4` trả 403, đã được thay bằng `https://www.w3schools.com/html/mov_bbb.mp4` trong dữ liệu mẫu và 10 bản ghi trên backend. Với bản sao backend vẫn dùng URL cũ, chạy `node scripts/repair-trailers.mjs BASE_URL`; script chỉ đổi trường trailerUrl của các bản ghi khớp chính xác URL lỗi.

Xem [kịch bản demo và checklist](docs/DEMO.md) và [cấu trúc mã nguồn](docs/PROJECT_STRUCTURE.md). Video thuyết trình cần được quay trên thiết bị/emulator với backend đã cấu hình; repository không giả lập video đã hoàn thành.
