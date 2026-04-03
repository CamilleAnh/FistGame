================================================================================
TÀI LIỆU THIẾT KẾ: MAN_1_LOGIC_ANDROID_NATIVE.txt
Phiên bản: 3.1
Ngày cập nhật: 03/04/2026
Dựa theo: MASTER_PROMPT.md & GAME_SYSTEM_PRINCIPLES.md
================================================================================

1. MỤC TIÊU MÀN 1
--------------------------------------------------------------------------------
Màn 1 là dạng puzzle xếp hộp màu vào thùng đứng:
- Thao tác: Người chơi chọn 1 thùng nguồn, sau đó chọn 1 thùng đích.
- Quy tắc: Các hộp màu trên cùng sẽ được di chuyển theo luật hợp lệ.
- Điều kiện thắng: Khi tất cả các thùng đã "hoàn thành" thì qua màn.

Thông số kỹ thuật Màn 1:
- Số màu: 4 - 5 màu (mức độ nhập môn).
- Số lượng thùng: Tổng 6 - 7 thùng.
- Thùng hỗ trợ: Có 1 - 2 thùng rỗng để thao tác.
- Hệ màu: Sử dụng pool 7 - 8 màu cơ bản của toàn game.

2. KIẾN TRÚC HỆ THỐNG (ANDROID NATIVE)
--------------------------------------------------------------------------------
- Nền tảng: Chạy 100% Android Native (Kotlin/XML).
- Công nghệ: Không dùng Unity runtime, không dùng script C#.
- Render UI: Sử dụng Android View thuần (ConstraintLayout, GridLayout, ImageView).
- Xử lý Logic: Toàn bộ logic đặt trong lớp Kotlin (Domain), UI chỉ render State.

3. TRẠNG THÁI VÀ LUỒNG XỬ LÝ
--------------------------------------------------------------------------------
- Trạng thái: MainMenu, LevelSelect, Playing, Paused, GameOver, LevelComplete.
- Luồng di chuyển: FirstFragment -> SecondFragment -> LevelOneFragment.
- Kết thúc: Khi hoàn thành -> LevelComplete -> Điều hướng qua Navigation Component.

4. CẤU TRÚC DỮ LIỆU CỐT LÕI
--------------------------------------------------------------------------------
4.1. Model (Kotlin):
- enum class ColorId: Định nghĩa các màu (PURPLE, RED, BLUE, v.v.).
- data class TubeState: Quản lý danh sách khối màu và sức chứa (mặc định 4).
- data class LevelState: Quản lý danh sách các thùng và ID thùng đang chọn.

4.2. Định nghĩa "Thùng hoàn thành":
Một thùng được xác nhận hoàn thành khi:
- Thùng hoàn toàn trống rỗng.
- HOẶC thùng đã đầy (4 khối) và tất cả các khối phải cùng một màu duy nhất.

5. QUY TẮC KHỞI TẠO (RANDOM CÓ KIỂM SOÁT)
--------------------------------------------------------------------------------
Để đảm bảo màn chơi luôn có lời giải:
- Chọn ngẫu nhiên 4 hoặc 5 màu. Mỗi màu tạo ra đúng 4 khối.
- Thuật toán "Xáo trộn ngược" (Reverse Shuffling): Bắt đầu từ trạng thái thắng, 
  thực hiện các nước đi hợp lệ ngược để tạo ra đề bài.
- Tuyệt đối không để xảy ra tình trạng Seed random tạo ra màn chơi vô nghiệm