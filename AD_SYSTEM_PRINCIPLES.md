# 📺 HỆ THỐNG QUẢNG CÁO & CHIẾN LƯỢC MONETIZATION

Phiên bản: 1.0
Cập nhật lần cuối: 2026-04-05
Trạng thái: Đang triển khai tích hợp AdMob

## 1) Cơ chế Quảng cáo Hiện tại (Current State)

Hiện tại, ứng dụng đã tích hợp Google Mobile Ads SDK và triển khai các thành phần cơ bản:
- **Banner Ad**: Hiển thị cố định tại `ad_container` ở dưới cùng màn hình trong `LevelOneFragment`.
- **Test ID**: Đang sử dụng ID thử nghiệm của Google để đảm bảo an toàn trong quá trình phát triển.
- **Tối ưu UI**: Bình và bàn chơi đã được thu nhỏ (scale 1.6 - 1.8) để dành không gian 50dp cho Banner mà không gây tràn viền.

## 2) Các loại Quảng cáo triển khai

| Loại Ads | Vị trí hiển thị | Tần suất | Mục đích |
| :--- | :--- | :--- | :--- |
| **Banner** | Dưới cùng màn hình chơi | Luôn hiện | Duy trì doanh thu thụ động. |
| **Interstitial** | Sau khi nhấn "Next Level" hoặc "Reset" | Mỗi 2-3 lần nhấn | Tối ưu doanh thu khi chuyển cảnh. |
| **Rewarded** | Khi hết lượt đi (Bag-Timer) hoặc bị kẹt | Theo nhu cầu | Hồi sinh hoặc nhận Item hỗ trợ. |

## 3) Hướng phát triển & Tính năng "Ép" xem Ads

Dựa trên độ khó của 1000 Level trong `LEVEL_PRINCIPLES.md`, hệ thống quảng cáo sẽ phát triển theo hướng cung cấp giá trị để người chơi vượt qua các màn BOSS:

### A. Rewarded Video (Xem để nhận quà)
1. **Thêm ống nghiệm (+1 Tube)**: Khi bàn chơi quá chật (Level 400+), người chơi xem 1 Ads để có thêm 1 ống trống vĩnh viễn trong màn đó.
2. **Hồi sinh (Revive)**: Khi một túi (Bag) về 0 lượt đi, thay vì Game Over, người chơi có thể xem Ads để nhận thêm 10 lượt đi cho túi đó.
3. **Phá băng/Dọn nhện**: Xem Ads để dọn sạch toàn bộ mạng nhện hoặc phá toàn bộ băng trong 1 màn chơi.

### B. Interstitial (Quảng cáo xen kẽ)
- Triển khai "Smart Interstitial": Nếu người chơi thắng màn chơi quá nhanh (dưới 30s), sẽ không hiện Ads. Nếu màn chơi kéo dài, sẽ hiển thị Ads khi nhấn "Next Level".

## 4) Chiến lược In-App Purchase (IAP) tương lai

Để cân bằng trải nghiệm người dùng, ứng dụng sẽ cung cấp các gói mua hàng:
- **Remove Ads**: Xóa hoàn toàn Banner và Interstitial (giá dự kiến: $1.99).
- **Starter Pack**: Gói khởi đầu bao gồm 500 Coin + No Ads.
- **Coin Shop**: Dùng tiền thật mua Coin để đổi lấy các Item hỗ trợ (Gậy phép, Búa phá bình, v.v.) mà không cần xem Ads.

## 5) Nguyên tắc lập trình Quảng cáo

- **Ad Manager**: Tạo một lớp singleton `AdManager.kt` để quản lý việc load và show toàn bộ Interstitial/Rewarded Ads tập trung.
- **Pre-load**: Luôn load trước Interstitial và Rewarded Ads ở màn hình Splash hoặc trong lúc đang chơi để đảm bảo người chơi không phải chờ khi nhấn nút.
- **User Experience**: Tuyệt đối không hiển thị quảng cáo ngay khi người chơi vừa mở ứng dụng hoặc đang thực hiện thao tác di chuyển dở tay.
