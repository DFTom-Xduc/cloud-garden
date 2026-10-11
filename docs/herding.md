# Cừu, dê và chó canh gác

## Cách mở khu mới

Chạy lại backend bằng `run.ps1`, tải lại trình duyệt. Trong **Xây dựng**, mở rộng đảo (260 coin), xây **Chuồng cừu & đồng cỏ** (240 coin) và **Trang trại dê** (230 coin). Nếu đã mở rộng trước đây, không cần trả lại tiền mở rộng. Trong **Chăn nuôi**, mua cừu/dê (65 coin mỗi loài), chó (90 coin, mua một lần). Chó được mở khóa khi có ít nhất một trong hai chuồng mới.

| Loài | Cho ăn | Thời gian thực | Sản phẩm | Giá mỗi phần | Sau thu sản phẩm |
|---|---:|---:|---|---:|---|
| Cừu | 9 coin | 50 giây | 4 lông cừu | 22 coin | Cừu còn; cho ăn để mọc lông tiếp |
| Dê | 10 coin | 55 giây | 5 thịt dê | 38 coin | Kết thúc lượt nuôi; mua dê mới |

Thịt bò vẫn 40 coin/phần. Sản phẩm về kho; bấm Bán tất cả để đổi thành coin. Mỗi loài có một lượt nuôi trong dữ liệu; nhiều hình con vật trên đồng cỏ biểu diễn đàn, không nhân số sản phẩm.

## Lịch tự động

Lịch theo **đồng hồ game**, không theo giờ máy. 1 giây thực = 10 phút game. Lúc 06:00, chó bắt đầu lùa cừu qua cửa ra đồng cỏ; lúc 18:00, dẫn về chuồng. Chặng đường mất 50 phút game (5 giây thực); cừu ở ngoài từ 06:50 và ở trong từ 18:50. Ngoài hai chặng, chó tuần tra quanh bãi dê. Không có chó thì cừu ở chuồng. Dê đi quanh đồng cỏ ban ngày và nghỉ trong chuồng ban đêm. Chó không tạo sản phẩm, không mất sau thu hoạch.

Lịch tính từ thời gian hiện tại nên đúng cả khi tick nhảy qua một mốc, đổi ngày/đêm hoặc tải lại save. Game không chạy khi backend đã tắt. Không mô phỏng thú săn mồi hay sát thương trong phiên bản này.

## Bản đồ và hiệu năng

Đảo cơ bản 22×17, mở rộng 30×20 ô. Hai chuồng và toàn bộ móng ở trong đất; ô hoa mở thêm nằm phía tây nam, không đè lên bãi dê. Cuộn chuột hoặc nút +/− để thu phóng; kéo nền để di chuyển; 100%/nút về nông trại đặt lại góc nhìn. Nút ! mở chăm sóc và nhiệm vụ tương ứng ngay cả khi cừu ở trong chuồng.

Giữ bộ nhớ đệm nền/hoa, giới hạn tốc độ vẽ, chế độ đồ họa tự động/tiết kiệm và hỗ trợ giảm chuyển động. Đàn hiển thị giới hạn 3 cừu/2 dê/1 chó; chỉ thêm một atlas ảnh dùng chung. Sáu nhiệm vụ mới, tổng cộng 20 nhiệm vụ.

## Lưu dữ liệu

Backend thật dùng SQLite. `farm_settings.guard_dog` lưu quyền sở hữu chó. Danh sách loài và sản phẩm mới được đưa vào ràng buộc SQLite. Bảng cũ được sao chép qua bảng mới trong transaction, giữ tiền, động vật, tồn kho và nhiệm vụ; version 3 chỉ đặt sau nâng cấp thành công. Snapshot rollback bao gồm chó.

Các file MySQL là thiết kế tham khảo, chưa nối backend. Không tự chạy SQL trên database MySQL của người dùng.

## Kiểm tra

- Toàn bộ domain, nhiệm vụ, SQLite và HTTP integration suite qua.
- Mốc 05:59/06:00/06:25/06:50, 17:59/18:00/18:25/18:50, qua nửa đêm, nhảy tick, không có chó.
- Hai lượt xén lông vẫn giữ cừu; thu thịt dê kết thúc lượt; từ chối mua chó lần hai.
- SQLite version 2 có dữ liệu: giữ tiền, thịt bò, gà đang ăn, nhiệm vụ đã nhận; cold reload giữ chó/lông/thịt dê; lỗi SQL hoàn tác toàn bộ save.
- HTTP mua/cho ăn/thu sản phẩm cho hai loài và chó.
- Canvas: bốn kích thước viewport, ba mức zoom, kéo bản đồ, năm nút chăm sóc, móng nhà, vị trí hoa, chất lượng và giảm chuyển động.
- Kiểm tra trình duyệt bằng server preview 8108 với dữ liệu trong bộ nhớ, không dùng save người chơi.

## Asset

`frontend/assets/herd-atlas.png` — 2172×724, ba ô ngang cừu/dê/chó, alpha trong suốt. Tạo bằng **imagegen tích hợp**, chế độ tạo mới từ ảnh tham chiếu cừu/dê của người dùng và atlas vật nuôi có sẵn. Không dùng CLI.

Prompt cuối:

> Create a transparent game sprite atlas, landscape aspect ratio 3:1, exactly three equal square cells in ONE horizontal row. LEFT cell: fluffy white sheep based on reference sheep, wool curls, small pale face and dark hooves. CENTER cell: cream goat based on goat reference, curved gray horns, cream beard, slender legs. RIGHT cell: friendly black-and-white border collie shepherd guard dog, ears alert, tail, four legs. All three full body, standing naturally, three-quarter view viewed slightly from above, faces toward viewer-right, feet share same baseline. Use the polished warmly shaded illustrated game style of the provided livestock atlas; readable at small sizes with saturated warm accents. Generous transparent padding in each cell. Each animal must stay inside its own equal cell, no overlap, no extra animals. No text, no border, no background, no floor, no cast shadow, no props. Output true transparent alpha.
