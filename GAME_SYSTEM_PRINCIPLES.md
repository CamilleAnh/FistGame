# NGUYÊN LÝ HỆ THỐNG GAME (Nguồn duy nhất)

Phiên bản: 1.0
Cập nhật lần cuối: 2026-04-03
Áp dụng cho: lớp Android shell và lớp gameplay Unity trong repository này

## 1) Mục đích

Tệp này là hợp đồng hệ thống cho trò chơi.
Từ nay mọi thay đổi hành vi của game phải cập nhật tệp này trước, rồi mới sửa mã.

Mục tiêu chính:
- Duy trì một vòng lặp game rõ ràng giữa giao diện Android và lớp gameplay Unity.
- Ngăn chặn các thay đổi trạng thái ngẫu nhiên và các tác dụng phụ ẩn.
- Giúp các thay đổi trong tương lai dễ dự đoán.

## 2) Sơ đồ hệ thống hiện tại

Lớp Android shell:
- `app/src/main/java/com/example/a2dgame/MainActivity.kt`: Activity chủ toàn màn hình.
- `app/src/main/java/com/example/a2dgame/FirstFragment.kt`: Menu chính (Play).
- `app/src/main/java/com/example/a2dgame/SecondFragment.kt`: Màn chọn level.

Lớp gameplay Unity:
- `Assets/Scripts/GameManager.cs`: Máy trạng thái game toàn cục và chuyển cảnh.
- `Assets/Scripts/ClickableMover.cs`: Xử lý input (click/chạm) và di chuyển theo lưới.

## 3) Nguyên tắc cốt lõi (BẮT BUỘC TUÂN THỦ)

1. Quyền duy nhất cho trạng thái game
- `GameManager` là nguồn quyền duy nhất quản lý trạng thái runtime.
- Các script khác không được thay đổi trực tiếp `Time.timeScale`.

2. Các trạng thái cho phép
- `MainMenu`, `Playing`, `Paused`, `GameOver`.
- Mọi trạng thái mới phải được thêm vào tài liệu này trước khi đưa vào mã.

3. Chuyển trạng thái có kiểm soát
- `MainMenu -> Playing`: khi gọi `StartGame`.
- `Playing -> Paused`: khi gọi `PauseGame`.
- `Paused -> Playing`: khi gọi `ResumeGame`.
- `Playing -> GameOver`: khi gọi `TriggerGameOver`.
- `Playing -> Paused -> next scene`: khi gọi `LevelComplete`.
- Các chuyển trạng thái không hợp lệ phải bị bỏ qua một cách an toàn.

4. Chính sách `Time.timeScale`
- `MainMenu`, `Paused`, `GameOver`: `Time.timeScale = 0`.
- `Playing`: `Time.timeScale = 1`.
- Con đường tải cảnh phải luôn khôi phục `Time.timeScale` về `1` trước khi load cảnh.

5. Chính sách hiển thị UI
- `mainMenuUI` chỉ hiển thị khi ở `MainMenu`.
- `pauseUI` chỉ hiển thị khi ở `Paused`.
- `gameOverUI` chỉ hiển thị khi ở `GameOver`.
- `levelCompleteUI` chỉ hiển thị trong luồng hoàn thành level.

6. Chính sách input cho di chuyển nhân vật
- Nhận input chỉ khi object chưa đang di chuyển (`isMoving == false`).
- Thứ tự ưu tiên di chuyển cạnh kề: Up -> Right -> Down -> Left.
- Vị trí mục tiêu phải vượt kiểm tra chướng ngại bằng `Physics2D.OverlapCircle`.
- Không cho phép teleport nếu không có tính năng này rõ ràng.

7. Chính sách scene và level
- Scene tiếp theo = buildIndex hiện tại + 1.
- Nếu vượt phạm vi, quay về scene 0 (menu/start scene).
- Khóa chuyển cảnh (`isTransitioning`) phải ngăn chặn việc load trùng lặp.

## 4) Hợp đồng sự kiện

- `OnStateChanged(GameState)` được phát sau khi trạng thái đã được cập nhật.
- Các listener UI và gameplay phải subscribe/unsubscribe một cách an toàn.
- Hệ thống mới nên phản ứng theo sự kiện, tránh việc polling mỗi frame nếu có thể.

## 5) Quy tắc sở hữu dữ liệu

- Các giá trị có thể tinh chỉnh được lưu trong script chủ sở hữu:
  - `nextLevelDelay` trong `GameManager`.
  - `gridSize`, `moveDuration`, `checkRadius` trong `ClickableMover`.
- Tránh nhân bản cấu hình ở nhiều nơi.

## 6) Quy trình bắt buộc từ nay

Với mọi tính năng, sửa lỗi hoặc thay đổi cân bằng:

1. Cập nhật tệp này trước (ghi rõ thay đổi và lý do).
2. Thực hiện thay đổi trong mã.
3. Chạy kiểm tra nhanh:
   - Mở app/menu.
   - Vào màn chọn level.
   - Bắt đầu gameplay.
   - Kiểm tra các chuyển trạng thái: pause, game over, hoàn thành level.
4. Nếu hành vi khác với tài liệu, thì:
   - Sửa mã để đúng theo tài liệu, hoặc
   - Cập nhật tài liệu này với hành vi mới đã được phê duyệt.

## 7) Những điều không thuộc phạm vi (hiện tại)

- Không có logic đồng bộ multiplayer.
- Không có gameplay điều khiển bởi server.
- Không có cấu hình động từ xa.

## 8) Lịch sử thay đổi

- v1.0 (2026-04-03): Tạo baseline nguyên tắc hệ thống.
