# NGUYÊN LÝ HỆ THỐNG GAME (CẬP NHẬT MỚI NHẤT)

Phiên bản: 3.0
Cập nhật lần cuối: 2026-04-05
Áp dụng cho: toàn bộ app Android Native (Kotlin/XML)

## 1) Mục đích

Tài liệu này định nghĩa kiến trúc lõi và các quy tắc vận hành của hệ thống game sắp xếp ống nghiệm. Phiên bản 3.0 cập nhật để đồng bộ với cơ chế **16 màu**, **Dung tích cố định**, và hệ thống **Lượt đi độc lập cho từng túi**.

## 2) Tổng quan cơ chế cốt lõi

- **Dung tích chuẩn**: Tất cả ống nghiệm có 4 ngăn. Mọi ống đầy màu phải chứa đủ 4 khối ngay khi bắt đầu.
- **Quy mô lớn**: Hỗ trợ tối đa 16 màu sắc. Số bình tăng tỉ lệ thuận với số màu (lên đến ~30 bình ở level cao).
- **Thuật toán Solvable**: Sử dụng **Reverse Shuffle (Xáo trộn ngược)**. Game khởi tạo từ trạng thái thắng rồi xáo trộn để đảm bảo 100% màn chơi có lời giải.
- **Cơ chế trùng màu**: Một màn chơi có thể có nhiều ống nghiệm chứa cùng một loại màu (ví dụ: 3 ống cùng màu Đỏ).

## 3) Hệ thống Túi & Bag-Timer (Lượt đi)

- **Kích hoạt**: Xuất hiện từ Level 20.
- **Lượt đi riêng biệt**: Mỗi túi màu khi xuất hiện có bộ đếm **25 lượt đi** độc lập.
- **Logic trừ lượt**: Mỗi lần người chơi di chuyển khối màu, **tất cả** túi đang hiện có đều bị trừ 1 lượt.
- **Hệ quả hết lượt**: Nếu bất kỳ túi nào về 0 lượt mà chưa đóng gói xong -> **THUA NGAY (Game Over)**. Người chơi phải Reset màn.
- **Túi thông minh**: Màu mục tiêu của túi được chọn ưu tiên từ các màu đang lộ diện trên bàn chơi hoặc ống đang chờ đóng gói.

## 4) Hệ thống Chướng ngại vật

1.  **Ẩn đáy (?) [Level 20+]**:
    *   Các lớp dưới bị che bởi dấu `?`.
    *   Khối ẩn không bị kéo đi cùng cho dù cùng màu với khối đang di chuyển bên trên.
    *   Tự động lộ diện ngay khi khối đè lên nó bị dời đi.
2.  **Mạng nhện 🕸️ [Level 80+]**:
    *   Phủ miệng ống. Tốn 1 lượt chạm để dọn dẹp (tính 1 lượt đi).
3.  **Đóng băng ❄️ [Level 120+]**:
    *   Khóa ống. Phải đổ **đúng màu** vào để phá băng.
    *   Đổ sai màu -> Ống bị **Khóa vĩnh viễn** (Archived).
4.  **Xích sắt ⛓️ [Level 160+]**:
    *   Khóa ống hoàn toàn. Chỉ mở sau khi đóng gói thành công 1 ống bất kỳ vào túi.

## 5) Lộ trình phát triển 1000 Level

| Giai đoạn | Màu | Ống đầy | Ống trống | Đặc điểm nổi bật |
| :--- | :---: | :---: | :---: | :--- |
| **Chương 1 (1-99)** | 3 - 8 | = Màu x 1.2 | 3 | Dạy chơi, xuất hiện Ẩn đáy, Túi 25 lượt. |
| **Chương 2 (100-399)** | 9 - 12 | = Màu x 1.3 | 3 | Nhiều ống trùng màu. Xuất hiện Mạng nhện. |
| **Chương 3 (400-799)** | 13 - 14 | = Màu x 1.5 | 3 | Xuất hiện Đóng băng. Bàn chơi cực đông. |
| **Chương 4 (800-1000)** | **16** | = Màu x 1.8 | 2 | **Final Boss**. Full cạm bẫy, kẹt chỗ cực nặng. |

## 6) Nguyên tắc lập trình (Cho Dev)

- **Engine Logic**: `LevelOneEngine` chịu trách nhiệm toàn bộ về trạng thái game (Tube, BoxSlot).
- **Snapshot Rendering**: Fragment chỉ nhận dữ liệu từ Engine và vẽ lại (renderBoard) sau mỗi hành động.
- **Kích thước thích ứng**: UI phải tính toán `tubeWidth` dựa trên `displayMetrics` để hiển thị 4 cột hàng ngang không bị tràn viền.
- **Tài nguyên**: Tuyệt đối không hardcode text. Sử dụng `strings.xml` với placeholders (ví dụ: `progress_packed`).

## 7) Quy trình kiểm tra (PR Checklist)

- [ ] Level khởi tạo có đủ 4 khối cho mỗi bộ màu không?
- [ ] Khi hết lượt ở 1 túi, game có báo Thua (Red text) ngay không?
- [ ] Khối ẩn có bị kéo đi cùng khối lộ diện không? (Phải đứng yên).
- [ ] Bàn chơi có bị tràn viền ở các level 15+ ống không? (Tự động scale bình).
