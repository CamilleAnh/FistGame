# 🚀 KẾ HOẠCH ĐƯA GAME LÊN GOOGLE PLAY STORE

Phiên bản: 1.0
Cập nhật lần cuối: 2026-04-05
Mục tiêu: Phát hành ứng dụng lên Google Play theo quy trình chuẩn và tối ưu hóa chuyển đổi (ASO).

## 1) Chuẩn bị về mặt Kỹ thuật (Technical Readiness)
Trước khi tạo bản build chính thức, cần hoàn thiện các bước sau:

- **Versioning**: Cập nhật `versionCode` và `versionName` trong `app/build.gradle.kts` cho mỗi lần release.
- **Signing**: Tạo tệp `keystore` (jks) bí mật để ký ứng dụng. Tuyệt đối không làm mất tệp này.
- **ProGuard/R8**: Cấu hình tệp `proguard-rules.pro` để thu nhỏ mã nguồn (shrink) và bảo mật mã (obfuscate), giúp giảm dung lượng tệp APK/AAB.
- **App Bundle (AAB)**: Xuất tệp định dạng `.aab` thay vì `.apk` để Google Play tự động tối ưu hóa dung lượng cho từng loại thiết bị.

## 2) Tài liệu và Chính sách (Legal & Compliance)
- **Privacy Policy**: Tạo trang web chính sách bảo mật (bắt buộc).
- **Data Safety**: Khai báo các loại dữ liệu ứng dụng thu thập (với Ads AdMob, cần khai báo thu thập ID quảng cáo).
- **Content Rating**: Hoàn thành bảng câu hỏi khảo sát độ tuổi (IARC) trong Google Play Console.

## 3) Chuẩn bị Store Listing (Marketing Assets)
Giao diện trên Store quyết định người dùng có tải game hay không.

- **App Name & Description**: 
    - Tên game (dưới 30 ký tự) chứa từ khóa chính (ví dụ: Color Sort Puzzle).
    - Mô tả ngắn (80 ký tự) và mô tả dài hấp dẫn.
- **Graphic Assets**:
    - **Icon**: 512x512 px (định dạng 32-bit PNG).
    - **Feature Graphic**: 1024x500 px (ảnh bìa làm nổi bật lối chơi).
    - **Screenshots**: Ít nhất 4 ảnh chụp màn hình điện thoại (nên có text hướng dẫn trên ảnh).
- **Video Trailer**: Video ngắn 15-30s giới thiệu gameplay mượt mà.

## 4) Các giai đoạn Thử nghiệm (Testing Phases)
Google Play yêu cầu thử nghiệm trước khi cho phép release công khai:

1.  **Internal Testing**: Gửi cho tối đa 100 người trong đội ngũ phát triển.
2.  **Closed Testing (Alpha)**: Yêu cầu tối thiểu 20 người dùng thử nghiệm liên tục trong 14 ngày (quy định mới của Google cho tài khoản cá nhân).
3.  **Open Testing (Beta)**: Cho phép người dùng bất kỳ đăng ký tham gia dùng thử trên Store.

## 5) Chiến lược Phát hành (Rollout Strategy)
- **Production Stage**: Sau khi vượt qua vòng kiểm duyệt và thử nghiệm.
- **Staged Rollout**: Phát hành dần dần (ví dụ: 10% người dùng -> 50% -> 100%) để theo dõi lỗi phát sinh trên các dòng máy lạ.

## 6) Theo dõi sau khi Phát hành (Post-Launch)
- **Google Play Console**: Theo dõi tỷ lệ crash (ANR) và đánh giá của người dùng.
- **Firebase Analytics**: Xem người dùng thường bị kẹt ở level nào để điều chỉnh lại `LevelOneEngine`.
- **AdMob Dashboard**: Theo dõi doanh thu từ Banner và Interstitial Ads.

## 🛠️ Danh sách việc cần làm ngay (Action Items)
- [ ] Chỉnh `compileSdk` và `targetSdk` lên phiên bản mới nhất (hiện tại là 34 hoặc 35).
- [ ] Thiết kế Icon và 4 ảnh chụp màn hình (Level 1, Level 20, Level 100).
- [ ] Tạo tệp Keystore an toàn.
