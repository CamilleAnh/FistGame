# ⚙️ NGUYÊN TẮC THIẾT KẾ 1000 LEVEL (CẬP NHẬT MỚI NHẤT)

Được đồng bộ theo: `MASTER_PROMPT.md` và `GAME_SYSTEM_PRINCIPLES.md`

## 💎 QUY TẮC CỐT LÕI
1. **Dung tích cố định**: Tất cả các ống nghiệm có dung tích cố định là **4 khối màu**. Đảm bảo tính cân đối về mặt hình ảnh và logic.
2. **Quy mô 16 màu**: Hệ thống hỗ trợ tối đa 16 màu sắc đa dạng. Số lượng màu tăng dần theo từng mốc level.
3. **Fill đầy 100%**: Mọi ống màu khi bắt đầu màn chơi đều chứa đúng **4 khối màu**. Không có tình trạng ống lẻ tẻ gây mất thẩm mỹ.
4. **Cơ chế Trùng màu**: Cho phép nhiều ống nghiệm chứa cùng một loại màu để lấp đầy bàn chơi quy mô lớn (ví dụ: 3 ống cùng màu Đỏ).
5. **Đảm bảo 100% thắng**: Sử dụng thuật toán **Trộn ngược (Reverse Shuffle)** từ trạng thái hoàn thành. Game luôn có lời giải toán học.

---

## 🎒 CƠ CHẾ TÚI ĐÓNG GÓI (BAG MECHANISM)
*   **Kích hoạt**: Xuất hiện từ **Level 20**.
*   **Bag-Timer 25 hiệp**: Mỗi túi màu có bộ đếm ngược **25 lượt đi** độc lập.
*   **Logic trừ lượt**: Mỗi lần di chuyển khối màu, tất cả các túi hiện có đều bị trừ 1 lượt.
*   **Thua cuộc (Game Over)**: Nếu bất kỳ túi nào về **0 lượt** mà chưa đóng gói xong ống màu yêu cầu -> Người chơi thua ngay lập tức (hiện chữ Đỏ).
*   **Túi thông minh**: Màu của túi tiếp theo được chọn ưu tiên từ các màu đang "lộ diện" trên bàn chơi để đảm bảo người chơi có thể hoàn thành mục tiêu.

---

## 🚫 HỆ THỐNG CHƯỚNG NGẠI VẬT
1. **Ẩn đáy (?) [Level 20+]**: 
    *   Các lớp dưới bị che bởi dấu `?`.
    *   **Quy tắc**: Lớp trên cùng luôn lộ diện. Chỉ dời được khối đã lộ diện. Khối ẩn không bị kéo đi cùng cho dù cùng màu.
    *   **Mở khóa**: Lớp ẩn tự reveal ngay khi khối phía trên bị dời đi.
2. **Mạng nhện 🕸️ [Level 80+]**: 
    *   Phủ trên miệng ống. Phải chạm 1 lần để dọn dẹp (tốn 1 lượt Bag-Timer).
3. **Đóng băng ❄️ [Level 120+]**: 
    *   Phải đổ đúng màu vào để phá băng. Thả sai màu ống sẽ bị **khóa vĩnh viễn**.
4. **Xích sắt ⛓️ [Level 160+]**: 
    *   Khóa ống hoàn toàn. Chỉ mở sau khi đóng gói thành công 1 ống màu bất kỳ.

---

## 📈 LỘ TRÌNH PHÁT TRIỂN (16 MÀU)
| Cấp độ | Số màu khác nhau | Số ống đầy màu | Ống trống | Đặc điểm |
| :--- | :---: | :---: | :---: | :--- |
| **1 - 19** | 3 - 4 | = Số màu | 3 | Dạy chơi cơ bản. |
| **20 - 99** | 5 - 8 | Số màu x 1.2 | 3 | **Ẩn đáy** + **Túi 25 lượt**. |
| **100 - 399** | 9 - 12 | Số màu x 1.3 | 3 | Trùng màu bình nhiều. **Mạng nhện**. |
| **400 - 799** | 13 - 14 | Số màu x 1.5 | 3 | **Đóng băng**. Bàn chơi cực đông. |
| **800 - 1000** | **16** | Số màu x 1.8 | 2 | **Final Boss**. ~30 ống, full cạm bẫy. |

---

## 🎯 CHIẾN THUẬT GỢI Ý
*   **Quan sát lượt**: Ưu tiên xử lý túi sắp hết lượt (số lượt đỏ) trước.
*   **Giải phóng trạm trung chuyển**: Luôn giữ ít nhất 1-2 ống trống làm trạm luân chuyển màu bí ẩn từ lớp dưới.
