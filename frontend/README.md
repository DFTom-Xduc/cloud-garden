# Frontend

Toàn bộ giao diện chạy trên trình duyệt. Không cần npm hoặc framework.

- `index.html`: bố cục, nút và dialog.
- `css/style.css`: giao diện, con trỏ dụng cụ, bố cục theo kích thước màn hình.
- `js/app.js`: gọi API Java, ngôn ngữ, bảng cửa hàng, thao tác người chơi.
- `js/scene.js`: vẽ cảnh nông trại và chọn ô bằng Canvas 2D.
- `js/audio.js`: nhạc/SFX bằng Web Audio, bắt đầu sau tương tác người dùng.
- `js/icons.js`, `assets/`: hình SVG và con trỏ.

Chạy `run.ps1` tại thư mục gốc dự án, rồi mở http://localhost:8080. Không mở `index.html` bằng cách bấm đúp hoặc Live Server vì giao diện cần API Java cùng nguồn.

Frontend không giữ save trong localStorage và không tự cộng/trừ tiền. Java quyết định kết quả mỗi thao tác, lưu vào SQLite rồi trả trạng thái JSON để giao diện vẽ lại. Java đọc danh mục hoa từ `Service/data/flowers.csv`; thêm loài tại đó và khởi động lại server.
