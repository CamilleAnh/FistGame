# 🎵 HỆ THỐNG ÂM THANH (AUDIO SYSTEM)

Phiên bản: 1.1
Cập nhật lần cuối: 2026-04-05
Trạng thái: **Đã tích hợp BGM (Chapter 1)**

## 1) Danh sách tệp tin & Trạng thái
Toàn bộ file âm thanh nằm trong: `app/src/main/res/raw`

| Tên File (Resource ID) | Chức năng | Trạng thái |
| :--- | :--- | :--- |
| `sunny_orchard_shuffle` | Nhạc nền Level 1 - 100 | **Đã kích hoạt** |
| `bgm_level_101_400` | Nhạc nền Chapter 2 | Chờ tích hợp |
| `sfx_harvest` | Hiệu ứng thu hoạch trái cây | Chờ tích hợp |
| `sfx_win` | Nhạc chiến thắng màn chơi | Chờ tích hợp |
| `sfx_fail` | Nhạc khi hết lượt đi | Chờ tích hợp |

## 2) Cấu hình Kỹ thuật
- **BGM Volume**: **35%** (`setVolume(0.35f, 0.35f)`).
- **Loại xử lý**: `MediaPlayer` (Stream từ disk để tiết kiệm RAM).
- **Tự động hóa**: Nhạc tự động dừng/phát theo Lifecycle của Android (onPause/onResume).

## 3) Quy tắc Quản lý
- Tuyệt đối không dùng file có dấu hoặc khoảng trắng.
- Ưu tiên định dạng `.mp3` chất lượng 128kbps để giảm dung lượng APK.
