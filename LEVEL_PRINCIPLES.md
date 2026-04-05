# ⚙️ NGUYÊN TẮC THIẾT KẾ 1000 LEVEL (CẬP NHẬT)

Được đồng bộ theo: `MASTER_PROMPT.md` và `GAME_SYSTEM_PRINCIPLES.md`

## 💎 QUY TẮC CỐT LÕI
1. **Dung tích cố định**: Tất cả các ống nghiệm có dung tích cố định là **4 khối màu**. Loại bỏ hoàn toàn ống ngắn/dài để đảm bảo tính đồng nhất.
2. **Quy mô 16 màu**: Hệ thống hỗ trợ tối đa 16 màu sắc khác nhau. Số lượng màu tăng dần theo cấp độ để mở rộng bàn chơi.
3. **Cơ chế Trùng màu**: Một màn chơi có thể có nhiều ống nghiệm chứa cùng một loại màu (ví dụ: 3 ống Đỏ, 2 ống Vàng) để lấp đầy bàn chơi cực lớn.
4. **Đảm bảo 100% thắng**: Sử dụng thuật toán **Trộn ngược (Reverse Shuffle)** từ trạng thái hoàn thành. Game luôn có lời giải, không phụ thuộc vào may rủi.

---

## 🎒 CƠ CHẾ TÚI ĐÓNG GÓI (BAG MECHANISM)
*   **Kích hoạt**: Xuất hiện từ **Level 20**.
*   **Bag-Timer riêng biệt**: Mỗi túi màu có một bộ đếm ngược hiệp (`turnsLeft`) độc lập (Mặc định: **15 lượt**).
*   **Logic trừ lượt**: Mỗi lần di chuyển 1 khối (hoặc 1 xấp màu), **tất cả** các túi đang hiện có đều bị trừ 1 lượt.
*   **Hậu quả khi hết lượt**: Nếu một túi về 0 lượt mà chưa đóng gói xong ống màu yêu cầu -> **GAME OVER** (Thua cuộc). Người chơi phải RESET để chơi lại.
*   **Cửa thắng**: Khi đóng gói thành công, túi mới sẽ xuất hiện với màu mục tiêu ưu tiên là các ống đang "chờ túi" hoặc màu còn sót lại trên bàn.

---

## 🚫 HỆ THỐNG CHƯỚNG NGẠI VẬT
1. **Ẩn đáy (?) [Level 20+]**: 
    *   Các khối ở dưới bị che bởi dấu `?`.
    *   **Quy tắc**: Khối trên cùng luôn hiện màu. Chỉ di chuyển khối đã hiện màu. Khối ẩn không bị kéo đi cùng cho dù cùng màu.
    *   **Mở khóa**: Lớp ẩn tự lộ diện ngay khi khối đè lên nó bị dời đi.
2. **Mạng nhện 🕸️ [Level 80+]**: 
    *   Phủ trên miệng ống. Tốn 1 lượt chạm để dọn dẹp (tính 1 hiệp Bag-Timer) mới có thể sử dụng ống.
3. **Đóng băng ❄️ [Level 120+]**: 
    *   Ống bị khóa. Phải đổ **đúng màu** vào để phá băng.
    *   **Hình phạt**: Đổ sai màu vào ống băng -> Ống bị **Khóa vĩnh viễn** (Archived) trong màn đó.
4. **Xích sắt ⛓️ [Level 160+]**: 
    *   Khóa ống hoàn toàn. Chỉ mở khi người chơi đóng gói thành công 1 ống bất kỳ vào túi.

---

## 📈 LỘ TRÌNH PHÁT TRIỂN ĐỘ KHÓ
| Cấp độ | Số màu | Số ống đầy | Ống trống | Đặc điểm |
| :--- | :---: | :---: | :---: | :--- |
| **1 - 19** | 3 - 4 | = Số màu | 3 | Dạy chơi cơ bản, không áp lực. |
| **20 - 99** | 5 - 10 | Số màu x 1.0 | 3 | Xuất hiện **Ẩn đáy** và **Túi (15 lượt)**. |
| **100 - 399** | 10 - 12 | Số màu x 1.2 | 3 | Xuất hiện **Mạng nhện**. Trùng màu bình bắt đầu nhiều. |
| **400 - 799** | 12 - 14 | Số màu x 1.5 | 2 | Xuất hiện **Đóng băng**. Bàn chơi dày đặc (>15 ống). |
| **800 - 1000** | **16** | Số màu x 1.8 | 2 | **Final Boss**. 16 màu, ~30 ống, full chướng ngại vật. |

---

## 🎯 CHIẾN THUẬT NGƯỜI CHƠI
*   **Ưu tiên đóng gói**: Phải đóng gói các ống hoàn thành ngay khi túi yêu cầu để giải phóng chỗ trống và reset rủi ro hết lượt.
*   **Quản lý không gian**: Với 16 màu và chỉ 2 ống trống ở level cao, việc lãng phí ống trung chuyển sẽ dẫn đến kẹt (Dead-end) và buộc phải dùng Item hỗ trợ.
