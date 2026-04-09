# ⚙️ NGUYÊN TẮC THIẾT KẾ 1000 LEVEL (PHIÊN BẢN HOÀN THIỆN 3.3)

Được đồng bộ theo: `MASTER_PROMPT.md`, `GAME_SYSTEM_PRINCIPLES.md` và `AD_SYSTEM_PRINCIPLES.md`

## 💎 QUY TẮC CỐT LÕI
1. **Thùng chứa cố định (4/4)**: Mọi kiện hàng (thùng chứa) khi bắt đầu màn chơi đều được **lấp đầy tuyệt đối đúng 4 loại trái cây**. Hình dáng thùng chứa được thiết kế dạng **khối vuông/hộp kiện hàng** lùn và rộng để tối ưu không gian hiển thị.
2. **Trái cây cực đại (Max Fill)**: Hình ảnh trái cây (icon) được phóng to tối đa để che gần hết diện tích ô chứa, tạo cảm giác thùng hàng đầy đặn và bắt mắt.
3. **Giao diện Ngang 4**: Bàn chơi luôn cố định hiển thị theo **4 cột hàng ngang**. Kích thước thùng tự động co giãn để vừa khít chiều ngang màn hình, đảm bảo không bị tràn viền ngay cả khi có nhiều hàng.
4. **Thùng trung chuyển (Empty Crates)**: 
    *   Duy trì **3 thùng trống hoàn toàn** cho đến Level 500 để đảm bảo không gian luân chuyển.
    *   Level 500+ có thể giảm còn 2 thùng trống để tăng độ khó cực hạn.
5. **Quy mô 16 loại Trái cây**: Hệ thống hỗ trợ tối đa 16 loại khác nhau (🍎, 🍓, 🍇...). Số lượng màu tăng dần theo cấp độ.
6. **Thuật toán Swap-Shuffle**: Game khởi tạo từ trạng thái thắng (thùng đơn sắc) rồi tráo đổi khối màu giữa các thùng đầy. Đảm bảo 100% solvable và giữ vững trạng thái lấp đầy 4/4.

---

## 🎒 CƠ CHẾ TÚI THU HOẠCH (BAG-TIMER)
*   **Kích hoạt**: Level 20+.
*   **Lượt đi riêng biệt (25 lượt)**: Mỗi túi màu (thùng mục tiêu) khi xuất hiện có bộ đếm ngược hiệp (`turnsLeft`) độc lập.
*   **Logic trừ lượt**: Mỗi lần di chuyển trái cây, **tất cả** các túi hiện có đều bị trừ 1 lượt.
*   **Hậu quả khi hết lượt**: Nếu bất kỳ túi nào về **0 lượt** mà chưa thu hoạch xong -> **GAME OVER (BẠN ĐÃ THUA)**.
*   **Túi thông minh**: Túi ưu tiên yêu cầu những trái cây **đang lộ diện** trên bàn chơi.

---

## 🚫 HỆ THỐNG CHƯỚNG NGẠI VẬT
1. **Kiện hàng bí ẩn (?) [Level 20+]**: 
    *   Ẩn toàn bộ lớp dưới, **chỉ để lộ trái cây duy nhất trên đỉnh**.
    *   **Quy tắc Reveal**: Chỉ khi trái cây lộ diện bên trên được dời đi, lớp ẩn ngay dưới mới lộ diện màu thật.
    *   **Logic chặn**: Trái cây đang ẩn đóng vai trò là vật cản, không bị kéo đi cùng ngay cả khi cùng loại với trái cây phía trên.
2. **Mạng nhện 🕸️ [Level 80+]**: Khóa miệng thùng, tốn 1 lượt chạm để dọn dẹp.
3. **Đóng băng ❄️ [Level 120+]**: Phải đổ đúng loại trái cây vào để phá băng. Đổ sai -> Thùng bị khóa vĩnh viễn (Archived).
4. **Xích sắt ⛓️ [Level 160+]**: Khóa thùng hoàn toàn. Tự động mở khi thu hoạch thành công 1 túi màu bất kỳ.

---

## 📈 LỘ TRÌNH PHÁT TRIỂN ĐỘ KHÓ
| Cấp độ | Số màu | Tổng số thùng | Đặc điểm nổi bật |
| :--- | :---: | :---: | :--- |
| **1 - 19** | 3 - 4 | 6 - 7 | Thùng lộ diện hoàn toàn, dạy chơi. |
| **20 - 99** | 5 - 8 | 10 - 15 | **Ẩn đáy (?)** cực mạnh. Xuất hiện **Túi (25 lượt)**. |
| **100 - 399** | 9 - 12 | 15 - 20 | Nhiều thùng trùng màu. Xuất hiện **Mạng nhện**. |
| **400 - 799** | 13 - 14 | 20 - 25 | Xuất hiện **Đóng băng**. Bàn chơi dày đặc. |
| **800 - 1000** | **16** | ~30 | **Final Boss**. 16 màu, full cạm bẫy. |

---

## 💡 CHIẾN THUẬT & HIỂN THỊ
*   **Độ tương phản**: Text màu **Vàng (#FFD54F)** kết hợp đổ bóng để nổi bật trên nền hình ảnh kiện hàng gỗ.
*   **Mở khóa kiện hàng**: Ưu tiên giải phóng hàng trên để reveal các kiện hàng bí ẩn (?) sớm nhất.
*   **Quản lý lượt**: Tập trung vào túi sắp hết hiệp (số lượt hiển thị đỏ) để tránh thua cuộc bất ngờ.
