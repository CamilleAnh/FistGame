# NGUYÊN LÝ HỆ THỐNG GAME (CẬP NHẬT MỚI NHẤT)

Phiên bản: 3.1
Cập nhật lần cuối: 2026-05-24
Áp dụng cho: toàn bộ app Android Native (Kotlin/XML)

## 1) Mục đích

Tài liệu này định nghĩa kiến trúc lõi và các quy tắc vận hành của hệ thống game **sắp xếp trái cây vào thùng chứa (Fruit Sort)**. Phiên bản 3.1 cập nhật để đồng bộ với cơ chế **16 loại trái cây**, **Dung tích cố định**, và hệ thống **Lượt đi độc lập cho từng túi thu hoạch**.

## 2) Tổng quan cơ chế cốt lõi

- **Dung tích chuẩn**: Tất cả thùng chứa có 4 ngăn (4 ô chứa trái cây). Mọi thùng đầy khi bắt đầu màn phải chứa đủ 4 khối trái cây.
- **Quy mô lớn**: Hỗ trợ tối đa 16 loại trái cây. Số thùng tăng tỉ lệ thuận với số loại trái cây (lên đến ~30 thùng ở level cao).
- **Thuật toán Solvable**: Sử dụng **Reverse Shuffle (Xáo trộn ngược)**. Game khởi tạo từ trạng thái thắng rồi xáo trộn để đảm bảo 100% màn chơi có lời giải.
- **Cơ chế trùng loại**: Một màn chơi có thể có nhiều thùng chứa cùng một loại trái cây (ví dụ: 3 thùng cùng loại 🍓 Dâu).

## 3) Hệ thống Túi Thu Hoạch & Bag-Timer (Lượt đi)

- **Kích hoạt**: Xuất hiện từ Level 20.
- **Lượt đi riêng biệt**: Mỗi túi thu hoạch khi xuất hiện có bộ đếm **25 lượt đi** độc lập.
- **Logic trừ lượt**: Mỗi lần người chơi di chuyển khối trái cây, **tất cả** túi đang hiện có đều bị trừ 1 lượt.
- **Hệ quả hết lượt**: Nếu bất kỳ túi nào về 0 lượt mà chưa đóng gói xong -> **THUA NGAY (Game Over)**. Người chơi phải Reset màn.
- **Túi thông minh**: Loại trái cây mục tiêu của túi được chọn ưu tiên từ các loại đang lộ diện trên bàn chơi hoặc thùng đang chờ đóng gói.

## 4) Hệ thống Chướng ngại vật

1.  **Ẩn đáy (?) [Level 20+]**:
    *   Các lớp dưới bị che bởi dấu `?`.
    *   Khối ẩn không bị kéo đi cùng cho dù cùng loại với khối đang di chuyển bên trên.
    *   Tự động lộ diện ngay khi khối đè lên nó bị dời đi.
2.  **Mạng nhện 🕸️ [Level 80+]**:
    *   Phủ miệng thùng. Tốn 1 lượt chạm để dọn dẹp (tính 1 lượt đi).
3.  **Đóng băng ❄️ [Level 120+]**:
    *   Khóa thùng. Phải chuyển **đúng loại trái cây** vào để phá băng.
    *   Chuyển sai loại -> Thùng bị **Khóa vĩnh viễn** (Archived).
4.  **Xích sắt ⛓️ [Level 160+]**:
    *   Khóa thùng hoàn toàn. Chỉ mở sau khi đóng gói thành công 1 thùng bất kỳ vào túi thu hoạch.

## 5) Lộ trình phát triển 1000 Level

| Giai đoạn | Loại trái cây | Thùng đầy | Thùng trống | Đặc điểm nổi bật |
| :--- | :---: | :---: | :---: | :--- |
| **Chương 1 (1-99)** | 3 - 8 | = Loại x 1.2 | 3 | Dạy chơi, xuất hiện Ẩn đáy, Túi 25 lượt. |
| **Chương 2 (100-399)** | 9 - 12 | = Loại x 1.3 | 3 | Nhiều thùng trùng loại. Xuất hiện Mạng nhện. |
| **Chương 3 (400-799)** | 13 - 14 | = Loại x 1.5 | 3 | Xuất hiện Đóng băng. Bàn chơi cực đông. |
| **Chương 4 (800-1000)** | **16** | = Loại x 1.8 | 2 | **Final Boss**. Full cạm bẫy, kẹt chỗ cực nặng. |

## 6) Nguyên tắc lập trình (Cho Dev)

- **Engine Logic**: `LevelOneEngine` chịu trách nhiệm toàn bộ về trạng thái game (Box, BoxSlot).
- **Snapshot Rendering**: Fragment chỉ nhận dữ liệu từ Engine và vẽ lại (renderBoard) sau mỗi hành động.
- **Kích thước thích ứng**: UI phải tính toán `boxWidth` dựa trên `displayMetrics` để hiển thị nhiều cột hàng ngang không bị tràn viền.
- **Tài nguyên**: Tuyệt đối không hardcode text. Sử dụng `strings.xml` với placeholders (ví dụ: `progress_packed`).

## 7) Quy trình kiểm tra (PR Checklist)

- [ ] Level khởi tạo có đủ 4 khối trái cây cho mỗi loại không?
- [ ] Khi hết lượt ở 1 túi, game có báo Thua (Red text) ngay không?
- [ ] Khối ẩn có bị kéo đi cùng khối lộ diện không? (Phải đứng yên).
- [ ] Bàn chơi có bị tràn viền ở các level 15+ thùng không? (Tự động scale thùng).
