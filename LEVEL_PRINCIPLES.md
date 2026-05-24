# ⚙️ NGUYÊN TẮC THIẾT KẾ 1000 LEVEL (PHIÊN BẢN HOÀN THIỆN 3.6)

Được đồng bộ theo logic Engine mới nhất: Hỗ trợ đa dạng hóa lối chơi Boss và tối ưu áp lực thời gian.

## 💎 QUY TẮC CỐT LÕI
1.  **Thùng chứa 4/4**: Mọi bình nhỏ khi bắt đầu đều chứa đủ 4 quả.
2.  **Cố định 25 lượt đi**: Áp dụng cho TOÀN BỘ các màn (thường và Boss) để đảm bảo người chơi có đủ thời gian giải đố các ma trận cạm bẫy phức tạp.
3.  **Trái cây cực đại**: Icon trái cây được phóng to tối đa để tạo cảm giác bình hàng đầy đặn.
4.  **Hệ số nhân lũy tiến**: Số lượng bình tăng dần theo level: `multiplier = 1.1 + (levelId / 1000.0) * 0.5`.

---

## 👹 HỆ THỐNG 4 ĐẠI BOSS (XOAY VÒNG MỖI 20 LEVEL)
Mỗi mốc 20 level sẽ xuất hiện một dạng Boss khác biệt về cơ chế thu hoạch:

### 🍯 Boss Loại 1: SIÊU BÌNH CHỨA (Mega Container)
*   **Cơ chế:** Ẩn toàn bộ xe tải. Xuất hiện 1 Bình Khổng Lồ ở trung tâm.
*   **Lối chơi:** Đổ TRỰC TIẾP từng quả cùng loại vào bình trung tâm. Không cần gom đủ 4 quả mới gửi đi.
*   **Đặc điểm khó:** Trái cây mục tiêu bị giấu sâu dưới đáy của các bình xung quanh.

### 🕸️ Boss Loại 2: MA TRẬN MẠNG NHỆN (Spider Maze)
*   **Cơ chế:** 100% các bình có chứa trái cây đều bị phủ mạng nhện ngay khi bắt đầu.
*   **Lối chơi:** Người chơi phải tốn nhiều lượt chạm ban đầu để dọn dẹp "mặt bằng" trước khi có thể sắp xếp.

### ❄️ Boss Loại 3: KHO LẠNH CẤP ĐÔNG (Deep Freeze)
*   **Cơ chế:** Xuất hiện các "Bình Băng Vĩnh Cửu". 
*   **Lối chơi:** Băng sẽ KHÔNG tan khi đổ trái cây vào. Chúng chỉ tự động vỡ ra khi người chơi hoàn thành thu hoạch thành công 1 túi hàng trên xe tải.

### ❓ Boss Loại 4: KIỆN HÀNG BÍ MẬT (Blind Order)
*   **Cơ chế:** 100% trái cây trên bàn chơi hiển thị dấu `?`.
*   **Lối chơi:** Thử thách trí nhớ tuyệt đối. Người chơi phải nhớ vị trí các loại quả sau mỗi lần di chuyển hoặc sử dụng vật phẩm soi sáng.

---

## 🚫 HỆ THỐNG CHƯỚNG NGẠI VẬT (MÀN THƯỜNG)
1.  **Ẩn đáy (?) [Level 20+]**: Lộ diện 3 quả đỉnh, ẩn 1 quả đáy (Màn Boss sẽ ẩn sâu hơn).
2.  **Đóng băng ❄️ [Level 80+]**: Đổ đúng màu vào để phá băng ngay lập tức.
3.  **Xích sắt ⛓️ [Level 120+]**: Khóa bình hoàn toàn, mở khi hoàn thành 1 túi màu bất kỳ.

---

## 📈 LỘ TRÌNH PHÁT TRIỂN ĐỘ KHÓ
| Cấp độ | Số màu | Tổng số thùng | Đặc điểm nổi bật |
| :--- | :---: | :---: | :--- |
| **1 - 19** | 3 | 5 - 7 | Làm quen, không vật cản. |
| **20 - 99** | 6 | 10 - 15 | Xuất hiện Ẩn đáy & Boss 20/40/60/80. |
| **100 - 299** | 9 | 15 - 20 | Bàn chơi đông đúc, Mạng nhện xuất hiện thường xuyên. |
| **300 - 599** | 12 | 20 - 25 | Đóng băng & Xích sắt xuất hiện dày đặc. |
| **600 - 1000** | **14 - 16** | **~30** | **CỰC HẠN**: 16 màu, chỉ 2 bình trống, cạm bẫy chồng chéo. |

---

## 💰 PHẦN THƯỞNG & MONETIZATION
*   **Màn thường:** Thắng nhận 50 Gold.
*   **Màn Boss:** Thắng nhận **150 Gold (x3)**.
*   **Hồi sinh:** Khi hết 25 lượt, người chơi có thể xem **Rewarded Ads** để nhận thêm 5 lượt đi cuối cùng.
