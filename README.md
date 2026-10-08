# Khu vườn trên mây / Cloud Garden

Giao diện HTML/CSS/JavaScript và Canvas 2D; backend Java OOP; database SQLite qua JDBC. Đã thay toàn bộ giao diện Swing. Cần JDK 17 trở lên, đã kiểm tra trên JDK 26. Driver SQLite có sẵn trong `lib`, không cần npm, Maven hay cài database server.

## Chạy chương trình

Mở thư mục `src` trong VS Code, dùng **Run Code** hoặc **F5 → Run Cloud Garden**. Hoặc chạy trong PowerShell:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\run.ps1
```

Giữ terminal đang chạy, rồi mở **http://localhost:8080** trong trình duyệt. Dùng Ctrl+C để lưu và dừng server. Nếu cổng bị dùng, chạy `run.ps1 -Port 8081`, rồi mở cổng tương ứng. Không chạy riêng `Main.java` và không mở HTML trực tiếp bằng Live Server.

Giao diện co giãn theo cửa sổ, hỗ trợ màn hình 16:9 và 16:10. Sau khi mở rộng đất, đảo thu nhỏ để hiển thị đủ; phép chọn ô dùng cùng tỷ lệ với cảnh vẽ.

## Cấu trúc và OOP

```text
src/
├── Entity/       # Animal, Ga, Lon, Bo, PlantPot, FlowerType, GameSave, các enum
├── Controller/   # Main khởi động; GameController nhận HTTP và phục vụ frontend
├── Service/      # GameState, GameService: luật chơi; Json; FlowerCatalog
│   └── data/flowers.csv
├── Repository/   # GameRepository và SqliteGameRepository dùng JDBC
├── database/     # schema.sql; cloudgarden.db được tạo khi chạy
├── frontend/     # index.html; css/; js/; assets/
├── lib/          # SQLite JDBC driver
├── tests/        # Kiểm thử Java, SQLite và HTTP
├── previews/     # Ảnh kiểm tra giao diện
└── run.ps1
```

Luồng xử lý: **Trình duyệt → GameController → GameService/GameState → Entity → Repository → SQLite**. Java kiểm tra tiền, điều kiện xây dựng, thời gian tưới và nuôi trước khi lưu. Frontend gửi yêu cầu và hiển thị trạng thái Java trả về; không tự quyết định số dư hoặc ghi database.

- **Đóng gói:** `PlantPot` và `Animal` giữ trạng thái riêng, thay đổi qua phương thức.
- **Trừu tượng/kế thừa/đa hình:** `Animal` là lớp trừu tượng; `Ga`, `Lon`, `Bo` định nghĩa thức ăn, thời gian và sản phẩm riêng. Service xử lý chung qua kiểu `Animal`.
- **Tách trách nhiệm:** Controller xử lý HTTP, Service xử lý nghiệp vụ, Repository xử lý lưu trữ. `GameRepository` là interface để thay cách lưu mà không thay giao diện.

## Database và API

SQLite lưu tại `database/cloudgarden.db`: bảng `player` (tiền/cài đặt/đồng hồ/nhiệm vụ), `plots`, `buildings`, `animals`, `inventory`. Mỗi thao tác lưu trong một transaction; nếu SQL thất bại, transaction và trạng thái trong Java đều được hoàn tác. Save tự động mỗi 5 giây khi đồng hồ chạy và ngay sau thao tác thành công.

Lần đầu chạy khi database chưa có người chơi, Java đọc save Preferences cũ `cloudgarden/farm-v2` để chuyển sang SQLite. Các lần sau đọc SQLite. Save Preferences cũ được giữ nguyên. Đóng server trước khi sao lưu file `.db` để dữ liệu WAL được ghi hết.

| API | Chức năng |
| --- | --- |
| `GET /api/state` | Trạng thái, danh mục hoa/công trình/vật nuôi, token phiên |
| `POST /api/action` | Trồng, tưới, đào, gặt, mua đất, xây, nuôi, thu sản phẩm, bán, nhận thưởng, cài đặt, ngày/đêm |

POST dùng form URL encoded, header `X-Game-Token`; token lấy từ GET. Java trả JSON và kiểm tra mọi tham số. Yêu cầu đồng thời được xử lý tuần tự trong Service để không nhân đôi phần thưởng. Server chỉ nghe `127.0.0.1`; demo hiện có một nông trại chung cho các tab, chưa có tài khoản/multiplayer.

## Trồng hoa

1. Bấm **Trồng hoa**, chọn loại hạt, rồi bấm một ô đất trống. Coin chỉ bị trừ khi trồng thành công.
2. Bấm **Tưới**; con trỏ đổi thành bình tưới. Bấm lên hoa để tưới. Hai lần tưới tăng một cấp, mỗi lần cách nhau 4 giây. Hoa có 3 cấp; cấp cao có hình lớn hơn, sức sống và giá bán cao hơn.
3. Bấm **Đào bỏ**; con trỏ đổi thành xẻng. Bấm lên hoa cần bỏ. Không hoàn tiền hạt và không tính nhiệm vụ thu hoạch.
4. Bấm **Chọn**, chọn hoa rồi **Thu hoạch** để bán. Ô trống có thể trồng lại. **Esc** cất dụng cụ.

### Dữ liệu hoa demo

Chỉnh `Service/data/flowers.csv` (UTF-8). Thêm một dòng để thêm loài mới; cửa hàng tự phân trang, 4 loài mỗi trang. Chạy lại game sau khi sửa dữ liệu.

| Cột | Ý nghĩa |
| --- | --- |
| id | Mã không dấu duy nhất; giữ ổn định để save cũ nhận đúng loài |
| vietnamese, english | Tên hiển thị, không dùng dấu phẩy trong tên |
| seedPrice | Giá hạt bằng coin |
| salePrice | Giá bán hoa cấp 1; mỗi cấp cộng 12 coin |
| basePower | Sức sống cấp 1; nhân với cấp của hoa |
| color | Màu cánh dạng `#RRGGBB` |

Demo có Cúc trắng, Oải hương, Hoa hồng, Hướng dương. Cấp 2 là dạng nở rộ, cấp 3 là dạng rực rỡ; sức sống là chỉ số demo, chưa có hệ thống chiến đấu.

## Mở rộng và xây dựng

Bấm **Xây dựng** ở cạnh trái. Công trình được đặt tại vị trí quy hoạch sẵn.

| Hạng mục | Giá coin | Điều kiện / tác dụng |
| --- | ---: | --- |
| Mở rộng đảo | 260 | Thêm 4 ô trồng và một vị trí nhà mới; demo cho mở rộng một lần |
| Nhà gỗ mới | 200 | Cần mở rộng đảo trước; công trình trang trí |
| Chuồng gà | 100 | Có sẵn từ đầu cùng một gà demo |
| Bãi bùn cho lợn | 140 | Cần có trước khi mua lợn |
| Chuồng bò và bãi cỏ | 180 | Cần có trước khi mua bò |

## Chăn nuôi

Bấm **Chăn nuôi**. Mỗi khu chứa một con trong bản demo. Mua con giống, mua thức ăn để bắt đầu đợt nuôi, chờ rồi thu sản phẩm vào kho. Bấm **Bán tất cả** để đổi sản phẩm sang coin.

| Lớp | Khu nuôi | Giá con giống | Thức ăn / đợt | Thời gian | Sản phẩm / đợt | Giá bán / sản phẩm |
| --- | --- | ---: | --- | ---: | --- | ---: |
| `Ga` | Chuồng gà | 30 | Ngũ cốc, 3 coin | 30 giây | 3 trứng | 8 coin |
| `Lon` | Bãi bùn | 45 | Cám, 8 coin | 45 giây | 4 thịt lợn | 28 coin |
| `Bo` | Chuồng bò | 70 | Cỏ công nghiệp, 12 coin | 60 giây | 5 thịt bò | 40 coin |

Gà được giữ lại sau khi lấy trứng và có thể cho ăn tiếp. Xuất lợn/bò lấy thịt sẽ kết thúc vòng nuôi của con đó; cần mua con mới. Các lớp kế thừa `Animal`; giá, thời gian, thức ăn và sản phẩm đều có thể chỉnh trong từng lớp. Tiến trình nuôi chạy khi server Java đang mở, kể cả lúc đóng tab. Dừng server sẽ tạm dừng tiến trình.

## Ngày/đêm và cài đặt

Đồng hồ tự chạy: 1 giây thật bằng 10 phút game; ban đêm từ 18:00 đến 06:00. Bấm đồng hồ hoặc **N** để đổi ngày/đêm; không tua thời gian nuôi. Cài đặt nhạc, SFX và tiếng Việt/Anh lưu trong SQLite. Trình duyệt chỉ phát âm thanh sau tương tác của người dùng; nhạc/SFX được tổng hợp bằng Web Audio.

Thư có nhiệm vụ thu hoạch 6 ô: thưởng 120 coin và 1 kim cương, nhận một lần. Kim cương là tiền cao cấp; phần nạp tiền đang để **Sắp ra mắt**, chưa có thanh toán.

Phím tắt: **1** chọn, **2** hạt giống, **3** tưới, **4** xẻng, **H** Home, **M** thư, **S** cài đặt, **B** xây dựng, **A** chăn nuôi, **N** ngày/đêm, **Esc** đóng bảng/cất dụng cụ. Khi Canvas có focus, **Enter** mở danh sách ô để chọn bằng bàn phím. Có thể dùng nút danh sách ô bên phải dưới để chọn ô và áp dụng dụng cụ.

## Kiểm tra

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\run.ps1 -Test
```

Kiểm tra luật chơi, chuyển save cũ, SQLite đọc lại sau đóng/mở connection, SQL rollback, Service rollback khi lưu lỗi, API tham số/điều kiện/token, chống thu hoạch lặp khi hai yêu cầu đồng thời, trồng/tưới/thu sản phẩm/bán và phục vụ frontend. Test dùng Preferences trong bộ nhớ và database tạm riêng, không thay save thật.

Đã kiểm tra trình duyệt tại 16:9 và 16:10: ngôn ngữ/âm lượng, gieo/đào/tưới bằng chuột, tăng cấp, chăn nuôi, mở rộng và lưu lại. Ảnh kiểm chứng giao diện nằm trong `previews/web-*.jpg`.

