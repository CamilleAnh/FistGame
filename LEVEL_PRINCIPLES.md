# ⚙️ NGUYÊN TẮC THIẾT KẾ 1000 LEVEL (PHIÊN BẢN HOÀN THIỆN 3.3)

Được đồng bộ theo: `MASTER_PROMPT.md`, `GAME_SYSTEM_PRINCIPLES.md` và `AD_SYSTEM_PRINCIPLES.md`

## 💎 QUY TẮC CỐT LÕI
1. **Thùng chứa cố định (4/4)**: Mọi kiện hàng (thùng chứa) khi bắt đầu màn chơi đều được **lấp đầy tuyệt đối đúng 4 loại trái cây**. Hình dáng thùng chứa được thiết kế dạng **khối vuông/hộp kiện hàng** lùn và rộng để tối ưu không gian hiển thị.
2. **Trái cây cực đại (Max Fill)**: Hình ảnh trái cây (icon) được phóng to tối đa để che gần hết diện tích ô chứa, tạo cảm giác thùng hàng đầy đặn và bắt mắt.
3. **Giao diện Ngang 4**: Bàn chơi luôn cố định hiển thị theo **4 cột hàng ngang**. Kích thước thùng tự động co giãn để vừa khít chiều ngang màn hình, đảm bảo không bị tràn viền ngay cả khi có nhiều hàng.
4. **Thùng trung trung chuyển (Empty Crates)**: 
    *   Duy trì **3 thùng trống hoàn toàn** cho đến Level 500 để đảm bảo không gian luân chuyển.
    *   Level 500+ có thể giảm còn 2 thùng trống để tăng độ khó cực hạn.
5. **Quy mô 16 loại Trái cây**: Hệ thống hỗ trợ tối đa 16 loại khác nhau (🍎, 🍓, 🍇...). Số lượng màu tăng dần theo cấp độ.

---

## 👹 HỆ THỐNG MÀN CHƠI BOSS (MILESTONES)
Xuất hiện định kỳ mỗi **20 Level** (20, 40, 60, 80, 100...).
*   **Nhận diện**: Background chuyển sang tông màu Đỏ/Tối, nhạc nền kịch tính hơn.
*   **Đặc điểm khó**:
    *   **Lượt đi cực ngắn**: Túi thu hoạch chỉ có **15 lượt** thay vì 25.
    *   **Màu sắc đột biến**: Luôn có nhiều hơn màn thường 2 loại trái cây.
    *   **Hỗn hợp cạm bẫy**: Boss Level 100+ sẽ xuất hiện cùng lúc 2 loại chướng ngại vật (vừa Ẩn đáy vừa Đóng băng).
*   **Phần thưởng**: Thắng màn Boss nhận **x3 Gold** và cơ hội mở khóa skin Thùng hàng mới.

---

## 🎒 CƠ CHẾ TÚI THU HOẠCH (BAG-TIMER)
*   **Lượt đi riêng biệt (25 lượt)**: Màn thường có 25 hiệp độc lập cho mỗi túi.
*   **Logic trừ lượt**: Mỗi lần di chuyển trái cây, **tất cả** các túi hiện có đều bị trừ 1 lượt.
*   **Hậu quả khi hết lượt**: Về **0 lượt** -> **GAME OVER**.

---

## 🚫 HỆ THỐNG CHƯỚNG NGẠI VẬT
1. **Kiện hàng bí ẩn (?) [Level 20+]**: Chỉ lộ trái cây đỉnh. Di chuyển đi mới lộ lớp dưới.
2. **Mạng nhện 🕸️ [Level 80+]**: Khóa miệng thùng, tốn 1 lượt chạm để dọn.
3. **Đóng băng ❄️ [Level 120+]**: Phải đổ đúng loại trái cây vào để phá băng.
4. **Xích sắt ⛓️ [Level 160+]**: Tự động mở khi thu hoạch xong 1 túi bất kỳ.

---

## 📈 LỘ TRÌNH PHÁT TRIỂN ĐỘ KHÓ
| Cấp độ | Số màu | Tổng số thùng | Đặc điểm nổi bật |
| :--- | :---: | :---: | :--- |
| **1 - 19** | 3 - 4 | 6 - 7 | Thùng lộ diện hoàn toàn, dạy chơi. |
| **20 - 99** | 5 - 8 | 10 - 15 | **BOSS 20/40/60/80**: Ẩn đáy (?) cực mạnh + 15 lượt đi. |
| **100 - 399** | 9 - 12 | 15 - 20 | **BOSS 100/200/300**: Mạng nhện phủ kín 50% bàn chơi. |
| **400 - 799** | 13 - 14 | 20 - 25 | **BOSS 400/600**: Đóng băng + Xích sắt đan xen. |
| **800 - 1000** | **16** | ~30 | **FINAL BOSS (1000)**: 16 màu, 2 thùng trống, 10 lượt đi. |

---

## 💡 CHIẾN THUẬT & HIỂN THỊ
*   **Độ tương phản**: Text màu **Vàng (#FFD54F)** trên nền kiện hàng gỗ.
*   **Quản lý lượt**: Ưu tiên túi có số lượt hiển thị màu Đỏ.
