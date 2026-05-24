# ⚙️ NGUYÊN TẮC THIẾT KẾ 1000 LEVEL (PHIÊN BẢN HOÀN THIỆN 3.8.1)

Tài liệu này đồng bộ với logic Engine V3.8.1, tập trung vào tính thử thách lũy tiến liên tục và hệ thống cạm bẫy dày đặc, sửa lỗi tàng hình ở màn Boss 4 và tối ưu hóa tương tác vật cản.

## 💎 QUY TẮC CỐT LÕI
1.  **Thùng chứa 4/4**: Mọi bình nhỏ khi bắt đầu đều chứa đủ 4 quả (trừ 2-3 bình trung chuyển trống).
2.  **Cố định 25 lượt đi**: Áp dụng cho TOÀN BỘ các màn. Áp lực lượt đi tăng cực cao ở level cao do số lượng bình tăng nhưng lượt đi không đổi.
3.  **Hệ số nhân tăng trưởng liên tục**: Số lượng bình tăng dần theo level và **TUYỆT ĐỐI KHÔNG** reset sau màn Boss.
    *   Công thức: `multiplier = 1.1 + (levelId / 1000.0) * 0.7`.
4.  **Hệ thống 4 Đại Boss**: Xoay vòng mỗi 20 Level (Mega Container, Spider Maze, Deep Freeze, Blind Order).

---

## 👹 ĐẶC TÍNH MÀN BOSS (THE SPIKE)
Màn Boss là "đỉnh nhọn" của độ khó trong mỗi giai đoạn:
*   **Màu sắc:** Tăng đột ngột **+2 loại màu** so với màn thường liền kề.
*   **Quy mô:** Tăng thêm **+30% số lượng bình** (`multiplier + 0.3`).
*   **Vật cản:** Kích hoạt trạng thái cực hạn tùy theo loại Boss:
    *   Boss 1: Đổ trực tiếp vào xe tải.
    *   Boss 2: Phủ nhện 100% các bình có chứa quả.
    *   Boss 3: Đóng băng ngẫu nhiên các bình chiến lược.
    *   Boss 4: Ẩn các lớp dưới (hiện lớp trên cùng) để tạo tính bất ngờ nhưng vẫn đảm bảo chơi được.

---

## 🚫 HỆ THỐNG CHƯỚNG NGẠI VẬT (LŨY TIẾN MÀN THƯỜNG)
Độ khó không chỉ nằm ở Boss mà lan tỏa sang cả màn thường, đảm bảo màn sau luôn khó hơn màn trước, khắc phục tình trạng lặp lại:

1.  **Mạng nhện 🕸️ [Level 35+]**: 
    *   **CƠ CHẾ TRỪ LƯỢT:** Chạm vào bình để phá nhện. Hành động này **TỐN 1 LƯỢT ĐI**.
    *   **Mật độ:** Cứ mỗi 60 level tăng thêm 1 mạng nhện ở màn thường (Tối đa 8 mạng).
2.  **Ẩn đáy (?) [Level 15+]**: 
    *   Tỉ lệ bình bị ẩn tăng dần từ 20% lên 90% theo tiến trình level.
    *   **Level 150+:** Ẩn 2 lớp đáy thay vì 1 lớp (Người chơi chỉ thấy 2 quả trên cùng).
3.  **Đóng băng ❄️ [Level 75+]**: 
    *   Cứ mỗi 120 level tăng thêm 1 bình băng đá ở màn thường (Tối đa 6 bình).
    *   Phá băng bằng cách đổ đúng màu trái cây vào bình (Màn thường) hoặc hoàn thành đơn hàng (Boss).
4.  **Xích sắt ⛓️ [Level 120+]**: 
    *   Khóa cứng bình. Chỉ mở khi người chơi hoàn thành thu hoạch 1 đơn hàng (túi màu/xe tải) bất kỳ.

---

## 📈 LỘ TRÌNH PHÁT TRIỂN ĐỘ KHÓ (CHƯƠNG TRÌNH 16 MÀU)
| Cấp độ | Số màu | Tổng số thùng | Đặc điểm vật cản tiêu biểu |
| :--- | :---: | :---: | :--- |
| **1 - 14** | 3 | 5 - 7 | Làm quen, không vật cản. |
| **15 - 49** | 4 - 6 | 8 - 12 | Xuất hiện Ẩn đáy (Lvl 15) & Mạng nhện (Lvl 35). |
| **50 - 99** | 6 - 8 | 12 - 15 | Xuất hiện Băng (Lvl 75). Nhện tốn lượt đi. |
| **100 - 199** | 8 - 10 | 15 - 20 | Ẩn 2 lớp đáy. Băng & Xích xuất hiện thường xuyên. |
| **200 - 399** | 10 - 12 | 20 - 25 | Bàn chơi cực đông. Multiplier vượt mốc 1.3. |
| **400 - 1000** | **14 - 16** | **~30+** | **CỰC HẠN**: Chỉ 2 bình trống, 90% bình bị bẫy/ẩn. |

---

## 💰 PHẦN THƯỞNG & MONETIZATION
*   **Phần thưởng:** Màn thường 50 Gold, màn Boss **150 Gold (x3)**.
*   **Giá trị Ads:** Quảng cáo Rewarded nhận thêm 5 lượt đi là chiến lược cốt lõi khi người chơi bị kẹt ở level cao do độ khó tăng trưởng không ngừng.
