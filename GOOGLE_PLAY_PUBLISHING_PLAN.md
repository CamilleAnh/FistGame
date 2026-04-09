# 🚀 KẾ HOẠCH ĐƯA GAME LÊN GOOGLE PLAY STORE (CẬP NHẬT)

Phiên bản: 2.0
Cập nhật lần cuối: 2026-04-05
Trạng thái: **Technical Refactor Complete** | **Ads Integrated**

## 1) Chuẩn bị về mặt Kỹ thuật (Technical Readiness)
Hệ thống đã được tối ưu hóa hiệu năng (Refactor 60 FPS) và tích hợp AdMob. Cần thực hiện thêm:

- **Thay thế Ad IDs**: Chuyển từ Test IDs sang Production IDs (Banner, Interstitial, Rewarded) lấy từ dashboard AdMob.
- **Firebase Integration**: 
    - Tích hợp `Google Analytics` để theo dõi tỷ lệ người chơi kẹt ở level nào.
    - Tích hợp `Crashlytics` để nhận báo cáo lỗi tự động từ thiết bị người dùng.
- **SDK Update**: Nâng `compileSdk` và `targetSdk` từ 34 lên **35** để tuân thủ yêu cầu mới nhất của Google Play (áp dụng từ tháng 8/2024).
- **ProGuard/R8**: Kích hoạt `isMinifyEnabled = true` trong `build.gradle` để bảo mật thuật toán `Reverse Shuffle`.

## 2) Tài liệu và Chính sách (Legal & Compliance)
- **Privacy Policy**: Nội dung phải nêu rõ ứng dụng sử dụng ID quảng cáo của Google.
- **Data Safety Form**: Khai báo thu thập:
    - *Device or other IDs* (cho Quảng cáo).
    - *App interactions* (cho Analytics).
    - *Crash logs* (cho Crashlytics).

## 3) Chuẩn bị Store Listing (Marketing Assets)
- **App Name**: Gợi ý: "Fruit Sort Puzzle: 1000+ Levels" hoặc "Color Water Sort: Fruit Edition".
- **Graphic Assets**:
    - **Icon**: Thiết kế dựa trên 16 loại trái cây mới (🍏, 🍓, 🍇...).
    - **Screenshots**: Cần chụp ảnh ở Level 1 (Dễ), Level 80 (Mạng nhện) và Level 120 (Băng) để thể hiện sự đa dạng.
- **Feature Graphic**: Ảnh 1024x500 làm nổi bật hiệu ứng 16 màu rực rỡ.

## 4) Các giai đoạn Thử nghiệm (Testing Phases)
Google yêu cầu quy trình nghiêm ngặt cho tài khoản cá nhân:
1. **Internal Test**: Gửi link cho bạn bè/người thân (tối đa 100 người).
2. **Closed Test (Bắt buộc)**: Mời **20 người dùng** trải nghiệm liên tục trong **14 ngày**. Ứng dụng chỉ được duyệt lên Production sau khi hoàn thành bước này.

## 5) Danh sách việc cần làm ngay (Action Items)
- [x] Refactor UI Performance (View Reuse & Drawable Caching).
- [x] Tích hợp AdMob SDK & Banner Ad.
- [ ] Cấu hình Firebase Project và tải tệp `google-services.json`.
- [ ] Nâng `targetSdk` lên 35 trong `app/build.gradle.kts`.
- [ ] Thiết kế Graphic Assets (Icon, Feature Graphic).
- [ ] Đăng ký tài khoản Google Play Console ($25 phí một lần).

---
*Lưu ý: Thuật toán Reverse Shuffle hiện tại đảm bảo 100% win, giúp giảm tỷ lệ người dùng bỏ game (Churn rate) ở các màn khó.*
