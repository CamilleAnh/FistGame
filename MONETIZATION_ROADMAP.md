# 💰 KẾ HOẠCH PHÁT TRIỂN HỆ THỐNG KIẾM TIỀN (MONETIZATION ROADMAP)

Phiên bản: 1.0
Cập nhật lần cuối: 2026-04-05
Mục tiêu: Đa dạng hóa nguồn thu nhập bên cạnh Quảng cáo, tăng tỷ lệ giữ chân người dùng (Retention).

## 1) Hệ thống Skin (Cá nhân hóa)
Thay đổi diện mạo game để người chơi không bị nhàm chán. Người chơi dùng Coin (kiếm được hoặc mua) để mở khóa.

### A. Skin Lọ (Tube Skins)
- **Classic**: Lọ thủy tinh mặc định.
- **Neon Glow**: Lọ phát sáng trong bóng tối.
- **Laboratory**: Bình thí nghiệm chuyên nghiệp với thước đo.
- **Ancient Vase**: Bình gốm cổ với hoa văn tinh xảo.
- **Cyberpunk**: Lọ kim loại hiện đại với đèn LED chạy dọc.

### B. Skin Khối/Nước (Block Skins)
- **Solid Color**: Màu trơn mặc định.
- **Fruit Slices**: Khối màu biến thành lát trái cây (Cam, Dưa hấu, Chanh...).
- **Emoji Blocks**: Mỗi khối có một biểu cảm khuôn mặt khác nhau.
- **Sparkle/Jewel**: Khối màu lấp lánh như kim cương, hồng ngọc.

### C. Chủ đề Bối cảnh (Background Themes)
- **Deep Space**: Chơi giữa các vì sao.
- **Zen Garden**: Nền vườn Nhật Bản tĩnh lặng giúp tập trung.
- **Undersea**: Hiệu ứng bong bóng và cá bơi xung quanh.

## 2) Vật phẩm hỗ trợ (Power-ups/Consumables)
Bán các vật phẩm giúp vượt qua các màn BOSS cực khó trong `LEVEL_PRINCIPLES.md`.

- **Gậy Phép (Undo)**: Quay lại 1 hoặc nhiều bước đi sai.
- **Bình Dự Phòng (+1 Tube)**: Thêm ngay 1 ống trống vào bàn chơi hiện tại.
- **Búa Phá Băng**: Phá hủy ngay lập tức tất cả các lớp Băng (Ice) trong màn chơi.
- **Kính Hiển Vi (Reveal All)**: Lộ diện tất cả các khối đang bị ẩn (?) trong 5 lượt đi.
- **Thêm Hiệp (+5 Turns)**: Cộng thêm 5 lượt cho tất cả các túi đang sắp hết hạn.

## 3) Các gói mua hàng trực tiếp (IAP - In-App Purchase)

| Gói sản phẩm | Giá dự kiến | Quyền lợi |
| :--- | :--- | :--- |
| **No Ads** | $1.99 | Xóa vĩnh viễn Banner và Interstitial Ads. |
| **Starter Bundle** | $2.99 | 1000 Coin + No Ads + 5 bình dự phòng. |
| **Collector's Pack** | $4.99 | Mở khóa toàn bộ Skin Neon + 2000 Coin. |
| **Coin Packs** | $0.99 - $19.99 | Các gói nạp Coin từ ít đến nhiều. |

## 4) Hệ thống Tiền tệ & Gacha (Loot Boxes)
- **Soft Currency (Coin)**: Kiếm được qua mỗi màn thắng, xem Ads hàng ngày.
- **Mystery Box**: Dùng 500 Coin để mở một hộp quà ngẫu nhiên (có cơ hội nhận Skin hiếm hoặc vật phẩm hỗ trợ).
- **Daily Spin**: Vòng quay may mắn miễn phí mỗi ngày, xem Ads để quay thêm.

## 5) Đăng ký Thành viên (Subscription/Season Pass)
- **VIP Pass (Hàng tháng)**: 
    - Nhận 100 Coin mỗi ngày.
    - Nhận 1 vật phẩm hỗ trợ ngẫu nhiên mỗi ngày.
    - Quyền lợi hồi sinh miễn phí 1 lần/màn chơi không cần xem Ads.
    - Icon tên người chơi màu vàng gold nổi bật trên bảng xếp hạng.

## 6) Nguyên tắc triển khai kỹ thuật
- **Local Storage & Cloud Sync**: Lưu trữ số lượng Coin và Skin đã mua. Đồng bộ qua Google Play Games để tránh mất dữ liệu khi đổi điện thoại.
- **Shop UI**: Thiết kế màn hình Shop chuyên nghiệp, phân loại rõ ràng giữa "Skins", "Items", và "Coins".
- **Dynamic Pricing**: Tự động giảm giá các gói IAP vào các dịp lễ hoặc cho người chơi mới.
