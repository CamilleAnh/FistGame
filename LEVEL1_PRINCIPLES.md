Gemini đã nói
TÀI LIỆU THIẾT KẾ: LEVEL1_PRINCIPLES.md
Phiên bản: 3.2
Ngày cập nhật: 04/04/2026
Được đồng bộ theo: MASTER_PROMPT.md và GAME_SYSTEM_PRINCIPLES.md

1. MỤC TIÊU MÀN 1
Màn 1 là puzzle xếp hộp màu vào các cây (thùng đựng).
Người chơi chọn 1 cây nguồn, sau đó chọn 1 cây đích để chuyển hộp theo luật.

Mục tiêu học của người chơi:

Hiểu luật chuyển hộp cùng màu.

Hiểu luật cây hoàn thành sẽ được đóng gói vào túi (box) tự động.

Thông số màn 1:

Số màu sử dụng: 4 đến 5 màu.

Số cây tổng: 6 cây (4 cây có màu + 2 cây rỗng).

Pool hệ màu toàn game: 7 đến 8 màu.

2. KIẾN TRÚC BẮT BUỘC (ANDROID NATIVE)
Không sử dụng Unity runtime.

Gameplay render bằng Android View (GridLayout, LinearLayout, TextView, View).

Logic game nằm trong Kotlin (LevelOneEngine), Fragment chỉ render state.

Điều hướng bằng Navigation Component.

3. CẬP NHẬT RULE THẮNG MỚI (QUAN TRỌNG)
Điều kiện thắng không còn là "tất cả cây đều hoàn thành trên board".

Điều kiện thắng mới:

Mỗi khi một cây đạt trạng thái hoàn thành (đầy 1 màu), cây đó sẽ:

Tự động bỏ vào 1 trong 2 túi ngẫu nhiên còn chỗ chứa.

Biến mất khỏi board (archived), để nhường chỗ xử lý cây khác.

Nếu túi đầy, túi đó biến mất và xuất hiện túi mới ngẫu nhiên.

Màn thắng khi đã đóng gói đủ số cây mục tiêu của màn.

Mục tiêu đóng gói màn 1:

Tổng số cây cần đóng gói: 4 cây (tương ứng 4 nhóm màu ban đầu).

4. RULE 2 TÚI NGẪU NHIÊN MỖI MÀN
Mỗi màn chỉ hiển thị 2 túi bất kỳ.

Mỗi túi có:

Tên túi (random theo danh sách túi)

Sức chứa (capacity) ngẫu nhiên trong khoảng 1 đến 3 cây

Số cây đã nhận (filled)

Khi filled == capacity:

Túi cũ bị thay thế ngay bằng túi mới ngẫu nhiên.

Ý nghĩa gameplay:

Tạo cảm giác "đóng gói đơn hàng" liên tục.

Vẫn giữ puzzle xếp màu là cốt lõi, nhưng có phần thưởng trực quan theo từng cây hoàn thành.

5. LUẬT THAO TÁC NGƯỜI CHƠI
Chọn nguồn hợp lệ: cây không rỗng và chưa bị archived.

Chọn đích hợp lệ:

Đích chưa đầy.

Đích rỗng hoặc màu trên cùng trùng màu nguồn.

Số hộp được chuyển:

Chuyển theo cụm cùng màu trên đỉnh nguồn.

Tối đa bằng số ô trống còn lại của đích.

6. UI/UX HIỂN THỊ BẮT BUỘC CHO MÀN 1
Hiện 2 thẻ túi trên đầu board (tvBoxA, tvBoxB):

Định dạng: TÊN_TÚI + tiến độ (filled/capacity).

Hiện thanh tiến độ đóng gói (tvPackedProgress):

Định dạng: Đã đóng gói: x/y cây.

Khi màn hoàn thành:

Toast thông báo: LEVEL COMPLETE.

7. FILE LIÊN QUAN ĐÃ ÁP DỤNG
app/src/main/java/com/example/a2dgame/LevelOneEngine.kt

Bổ sung BoxSlot, tiến độ đóng gói, archived tube, win condition mới.

app/src/main/java/com/example/a2dgame/LevelOneFragment.kt

Render 2 túi, render tiến độ đóng gói, cập nhật hướng dẫn theo state.

app/src/main/res/layout/fragment_level_one.xml

Thêm khu hiển thị 2 túi, thêm text tiến độ, thêm nút back.

8. RỦI RO CẦN KIỂM SOÁT
Null binding nếu truy cập view sau onDestroyView().

Sai xử lý archived tube có thể làm tube vẫn click được.

Back stack điều hướng sai nếu route chưa khai báo đúng.

9. CHECKLIST NGHIỆM THU
Có đúng 2 túi random được hiển thị.

Hoàn thành 1 cây -> tự động vào 1 túi còn chỗ.

Túi đầy -> tự thay bằng túi mới ngay.

Cây hoàn thành biến mất khỏi board.

Khi đạt 4/4 cây thì màn báo complete.