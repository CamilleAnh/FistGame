# NGUYÊN LÝ VẬN HÀNH APP (KHÔNG DÙNG UNITY)

Phiên bản: 2.0
Cập nhật lần cuối: 2026-04-03
Áp dụng cho: toàn bộ app Android Native (Kotlin/XML)

## 1) Mục đích

Tệp này là nguồn chân lý duy nhất cho kiến trúc và hành vi của app.
Từ thời điểm này, app không còn sử dụng Unity trong runtime.

Mục tiêu chính:
- Duy trì một vòng đời rõ ràng cho menu, chọn level và gameplay ngay trong Android Native.
- Loại bỏ phụ thuộc Unity để đơn giản build, debug và bảo trì.
- Giữ logic nhất quán giữa các màn hình và các level.

## 2) Kiến trúc hiện tại

Lớp điều hướng và giao diện:
- `app/src/main/java/com/example/a2dgame/MainActivity.kt`: Activity host toàn màn hình.
- `app/src/main/java/com/example/a2dgame/FirstFragment.kt`: Main Menu.
- `app/src/main/java/com/example/a2dgame/SecondFragment.kt`: Level Select.

Lớp gameplay (mục tiêu mới):
- Gameplay được triển khai bằng Kotlin trong module Android (`app`).
- Dùng View hệ thống Android hoặc custom view để render board và xử lý input.
- Không gọi hoặc phụ thuộc scene/script Unity khi chạy app.

## 3) Quy tắc bắt buộc (BẮT BUỘC TUÂN THỦ)

1. Không dùng Unity runtime
- Không nhúng UnityPlayer, không load scene Unity, không dùng script C# cho gameplay runtime.
- Thư mục `Assets/Scripts` (nếu còn) chỉ mang tính lịch sử hoặc tài liệu tham khảo.

2. Nguồn quyền trạng thái duy nhất
- Trạng thái app/gameplay phải được quản lý tập trung trong lớp Kotlin (ví dụ `GameStateManager` hoặc `ViewModel` cấp màn hình).
- Không để nhiều nơi tự ý đổi trạng thái cùng lúc.

3. Trạng thái hợp lệ
- `MainMenu`, `LevelSelect`, `Playing`, `Paused`, `GameOver`, `LevelComplete`.
- Trạng thái mới chỉ được thêm sau khi cập nhật tài liệu này.

4. Chuyển trạng thái có kiểm soát
- `MainMenu -> LevelSelect`.
- `LevelSelect -> Playing`.
- `Playing -> Paused`.
- `Paused -> Playing`.
- `Playing -> GameOver` hoặc `Playing -> LevelComplete`.
- `LevelComplete -> Playing` (level kế tiếp) hoặc về `LevelSelect`.

5. Chính sách điều hướng
- Điều hướng màn hình thực hiện qua Navigation Component.
- Mọi chuyển màn phải đi qua action đã khai báo trong `nav_graph.xml`.
- Cấm điều hướng "tắt" gây sai back stack.

6. Chính sách gameplay puzzle màu
- Rule gameplay được xử lý thuần Kotlin.
- Dữ liệu level gồm danh sách thùng, block màu, capacity, số thùng rỗng.
- Điều kiện qua màn: tất cả thùng đều rỗng hoặc đầy và đồng nhất 1 màu.

7. Chính sách random level
- Random phải có kiểm soát seed (để debug/replay).
- Không sinh level vô nghiệm.
- Man 1: chỉ dùng 4-5 màu; tổng hệ màu game: 7-8 màu cơ bản.

## 4) Hợp đồng dữ liệu và sự kiện

- UI chỉ render theo state/data hiện tại, không tự suy diễn luật.
- Event người dùng (chọn ống nguồn, ống đích, undo, restart) đi qua lớp xử lý trung tâm.
- Mọi thay đổi board phải phát ra state mới để UI cập nhật.

## 5) Quy tắc mã nguồn

- Tách rõ 3 lớp:
  - UI (`Fragment`, `View`, `Adapter`)
  - Domain (luật game, kiểm tra nước đi, kiểm tra win)
  - Data (định nghĩa level, seed, lưu tiến độ)
- Không trộn logic game nặng vào XML hoặc callback UI rời rạc.
- Ưu tiên code thuần, dễ test unit cho luật game.

## 6) Quy trình phát triển bắt buộc

Với mọi tính năng/sửa lỗi:
1. Cập nhật tệp này trước nếu có thay đổi quy tắc vận hành.
2. Xác định thay đổi thuộc UI, Domain hay Data.
3. Sửa mã Kotlin/XML tương ứng.
4. Chạy kiểm tra nhanh:
   - Main Menu mở đúng.
   - Level Select mở đúng.
   - Vào gameplay và thao tác hợp lệ.
   - Qua màn/khởi động lại hoạt động đúng.
5. Nếu hành vi khác tài liệu: sửa mã hoặc cập nhật tài liệu có phê duyệt.

## 7) Phạm vi không làm (hiện tại)

- Không multiplayer online.
- Không server authoritative gameplay.
- Không remote config cho luật game.

## 8) Kế hoạch chuyển đổi khỏi Unity

1. Freeze logic Unity cũ, không phát triển tính năng mới ở C#.
2. Port luật gameplay cần thiết sang Kotlin.
3. Điều hướng level hoàn toàn qua Android.
4. Sau khi port đủ, loại bỏ phụ thuộc Unity còn lại khỏi build/runtime.

## 9) Lịch sử thay đổi

- v2.0 (2026-04-03): Chuyển nguyên lý hệ thống sang Android Native hoàn toàn, không dùng Unity runtime.
- v1.0 (2026-04-03): Baseline cũ Android shell + Unity core.
