# TÀI LIỆU THIẾT KẾ: LEVEL1_PRINCIPLES.md

Phiên bản: 3.3
Ngày cập nhật: 2026-05-24
Được đồng bộ theo: MASTER_PROMPT.md và GAME_SYSTEM_PRINCIPLES.md

## 1. MỤC TIÊU MÀN 1
Màn 1 là puzzle sắp xếp trái cây cùng loại vào chung một thùng chứa.
Người chơi chọn 1 thùng nguồn, sau đó chọn 1 thùng đích để chuyển trái cây theo luật.

**Mục tiêu học của người chơi:**
- Hiểu luật chuyển trái cây cùng loại.
- Hiểu luật thùng hoàn thành sẽ được đóng gói vào túi thu hoạch tự động.

**Thông số màn 1:**
- Số loại trái cây: 4 đến 5 loại.
- Số thùng tổng: 6 thùng (4 thùng có trái cây + 2 thùng rỗng).
- Pool hệ trái cây toàn game: 7 đến 8 loại.

## 2. KIẾN TRÚC BẮT BUỘC (ANDROID NATIVE)
- Không sử dụng Unity runtime.
- Gameplay render bằng Android View (GridLayout, LinearLayout, TextView, View).
- Logic game nằm trong Kotlin (LevelOneEngine), Fragment chỉ render state.
- Điều hướng bằng Navigation Component.

## 3. CẬP NHẬT RULE THẮNG MỚI (QUAN TRỌNG)
Điều kiện thắng không còn là "tất cả thùng đều hoàn thành trên board".

**Điều kiện thắng mới:**
Mỗi khi một thùng đạt trạng thái hoàn thành (đầy 1 loại trái cây), thùng đó sẽ:
1. Tự động bỏ vào 1 trong 2 túi thu hoạch ngẫu nhiên còn chỗ chứa.
2. Biến mất khỏi board (archived), để nhường chỗ xử lý thùng khác.
3. Nếu túi đầy, túi đó biến mất và xuất hiện túi mới ngẫu nhiên.

Màn thắng khi đã đóng gói đủ số thùng mục tiêu của màn.

**Mục tiêu đóng gói màn 1:**
- Tổng số thùng cần đóng gói: 4 thùng (tương ứng 4 nhóm loại trái cây ban đầu).

## 4. RULE 2 TÚI THU HOẠCH MỖI MÀN
Mỗi màn chỉ hiển thị 2 túi bất kỳ.

Mỗi túi có:
- Loại trái cây mục tiêu
- Sức chứa (capacity) trong khoảng 1 đến 3 thùng
- Số thùng đã nhận (filled)

Khi filled == capacity: Túi cũ bị thay thế ngay bằng túi mới ngẫu nhiên.

**Ý nghĩa gameplay:**
- Tạo cảm giác "đóng gói đơn hàng" liên tục.
- Vẫn giữ puzzle sắp xếp trái cây là cốt lõi, nhưng có phần thưởng trực quan theo từng thùng hoàn thành.

## 5. LUẬT THAO TÁC NGƯỜI CHƠI
- Chọn nguồn hợp lệ: thùng không rỗng và chưa bị archived.
- Chọn đích hợp lệ: Đích chưa đầy. Đích rỗng hoặc loại trái cây trên cùng trùng loại nguồn.
- Số khối trái cây được chuyển: Chuyển theo cụm cùng loại trên đỉnh nguồn. Tối đa bằng số ô trống còn lại của đích.

## 6. UI/UX HIỂN THỊ BẮT BUỘC CHO MÀN 1
- Hiện 2 thẻ túi thu hoạch trên đầu board (tvBoxA, tvBoxB).
- Hiện thanh tiến độ đóng gói (tvPackedProgress): Đã đóng gói: x/y thùng.
- Khi màn hoàn thành: Toast thông báo: LEVEL COMPLETE.

## 7. FILE LIÊN QUAN
- `LevelOneEngine.kt`: Bổ sung BoxSlot, tiến độ đóng gói, archived box, win condition mới.
- `LevelOneFragment.kt`: Render 2 túi thu hoạch, render tiến độ, cập nhật hướng dẫn theo state.
- `fragment_level_one.xml`: Thêm khu hiển thị 2 túi, thêm text tiến độ, thêm nút back.

## 8. RỦI RO CẦN KIỂM SOÁT
- Null binding nếu truy cập view sau onDestroyView().
- Sai xử lý archived box có thể làm thùng vẫn click được.
- Back stack điều hướng sai nếu route chưa khai báo đúng.

## 9. CHECKLIST NGHIỆM THU
- [ ] Có đúng 2 túi thu hoạch random được hiển thị.
- [ ] Hoàn thành 1 thùng -> tự động vào 1 túi còn chỗ.
- [ ] Túi đầy -> tự thay bằng túi mới ngay.
- [ ] Thùng hoàn thành biến mất khỏi board.
- [ ] Khi đạt 4/4 thùng thì màn báo complete.