# NGUYÊN LÝ HỆ THỐNG GAME (CẬP NHẬT)

Phiên bản: 2.1
Cập nhật lần cuối: 2026-04-04
Áp dụng cho: toàn bộ app Android Native (Kotlin/XML)

## 1) Mục đích

Tài liệu này là nguồn chân lý cho kiến trúc, hành vi và các quy tắc gameplay của ứng dụng.
Phiên bản 2.1 cập nhật để đồng bộ với phân đoạn level (1–1000) và các cơ chế mới: `Túi` (Bag), `Bag-Timer`, cùng các chướng ngại vật (Frozen, Hidden, Chain Lock, Cracked Tube, Inversion).

Mục tiêu chính:
- Định nghĩa rõ ràng các quy tắc level để team dev và thiết kế giữ đồng bộ.
- Mô tả chi tiết cơ chế Túi và cách hoạt động của Bag-Timer, hình phạt và tương tác với chướng ngại vật.
- Vạch ra hợp đồng dữ liệu, API nội bộ và các kiểm tra bắt buộc khi thay đổi luật chơi.

## 2) Tổng quan thay đổi chính

- Thống nhất 5 chương level (1–1000) với thông số riêng cho mỗi chương.
- Giới thiệu và chuẩn hoá cơ chế `Túi` (Bag) — start xuất hiện từ Level 10.
- Chuẩn hoá `Bag-Timer` theo chương (số lượt giới hạn để hoàn thành túi).
- Bổ sung hành vi chướng ngại vật và cơ chế đặc biệt (Frozen, Hidden, Chain Lock, Cracked Tube, Inversion).
- Quy định rõ model dữ liệu level, seed cho random, và contract cho GameStateManager.

## 3) Quy ước dữ liệu level (bắt buộc cho dev/designer)

Mỗi level phải định nghĩa rõ (ví dụ JSON):

{
  "id": 1,
  "colors": 6,
  "tubes": 8,
  "empty_tubes": 2,
  "bag_intro": 10,
  "bag_timer": null,          // null = không giới hạn
  "obstacles": ["Frozen"],
  "frozen_percent": 0.2,
  "hidden_layers": 0,         // số lớp ẩn ở đáy
  "locked_colors": [],        // với Chain Lock
  "cracked_timeout": null,    // số lượt trước khi vật vỡ (nếu có)
  "board_rotation_interval": null, // số lượt tổng để xoay (ví dụ 15)
  "seed": 12345
}

Ghi chú:
- `bag_timer` là số lượt (hiệp) khi túi được kích hoạt; `null` = không giới hạn.
- `bag_intro` cho biết level bắt đầu xuất hiện cơ chế Túi (thường 10).
- `seed` bắt buộc để đảm bảo deterministic generation và debug/replay.

## 4) Tiến độ & thông số theo chương (chi tiết)

CHƯƠNG 1: KHỞI ĐẦU THOẢI MÁI (Level 1 - 100)
- Màu sắc: bắt đầu 3, tăng dần đến 6.
- Số ống: 4 → 8 (luôn giữ 2 ống trống dự phòng cho level dễ).
- Cơ chế Túi: giới thiệu từ Level 10 (ban đầu không bắt buộc trong 1–50).
- Bag-Timer:
  - Level 1–50: không có giới hạn (chơi tự do).
  - Level 51–100: giới hạn 15 hiệp (mục tiêu làm quen với đồng hồ đếm ngược).
- Chướng ngại vật: không có.

CHƯƠNG 2: THỬ THÁCH ĐẦU TIÊN (Level 101 - 300)
- Màu sắc: cố định 7 màu.
- Số ống: 8 → 9 (ở các màn Hard giảm ống trống xuống còn 1).
- Bag-Timer: mặc định 10 hiệp.
- Hình phạt khi hết Bag-Timer: toàn bộ vật trong túi của màu đó sẽ bị trả ngược ngẫu nhiên vào các ống.
- Chướng ngại vật: `Frozen` (xuất hiện ~20% ống). Cần đổ 1 món bất kỳ vào để phá băng và lộ màu bên dưới.

CHƯƠNG 3: BÍ ẨN VÀ TÍNH TOÁN (Level 301 - 500)
- Màu sắc: tối đa 8 màu chủ đạo.
- Số ống: 10 ống.
- Bag-Timer: 8 hiệp.
- Chướng ngại vật: `Hidden` (vật phẩm ẩn). Hai lớp dưới cùng của mỗi ống có thể là dấu hỏi — màu đáy ẩn.
- Chiến thuật: khuyến cáo người chơi mở ít nhất 3/4 vật cùng màu trước khi bắt đầu bỏ vào Túi để tránh hết hiệp.

CHƯƠNG 4: KHÓA VÀ THỨ TỰ (Level 501 - 750)
- Màu sắc: 8 màu (tách tông gần giống).
- Số ống: 10–11 ống.
- Bag-Timer: 6 hiệp.
- Chướng ngại vật: `Chain Lock` (xích sắt). Một số ống bị khóa; chỉ khi hoàn thành một "Túi màu chỉ định" (ví dụ túi Vàng) thì xích mở.
- Ảnh hưởng: ép người chơi giải quyết theo thứ tự quy định.

CHƯƠNG 5: BẬC THẦY SẮP XẾP (Level 751 - 1000)
- Màu sắc: 8 màu.
- Số ống: 12 ống (vật phẩm xáo trộn cực độ).
- Bag-Timer: 4–5 hiệp (rất ngắn, sai 1 là thua).
- Chướng ngại vật: `Cracked Tube` (ống nứt) & `Inversion` (đảo ngược).
  - `Cracked Tube`: vật bị để yên quá lâu sẽ vỡ mất.
  - `Inversion`: sau mỗi 15 lượt tổng, toàn bộ bàn chơi xoay 180° (đáy ↔ đỉnh).
- Level 1000 (Boss): kết hợp tất cả cơ chế: 8 màu, 12 ống, vật ẩn, xích, Bag-Timer 4 hiệp.

### Bảng tóm tắt (tham khảo cho dev/designer)

- 1–100: Màu 3–6 | Ống trống 2 | Bag-Timer: 51–100 = 15/1 | Chướng ngại vật: Không
- 101–300: Màu 7 | Ống trống 1–2 | Bag-Timer: 10 | Chướng ngại vật: Frozen (~20%)
- 301–500: Màu 8 | Ống trống 1 | Bag-Timer: 8 | Chướng ngại vật: Hidden
- 501–750: Màu 8 | Ống trống 1 | Bag-Timer: 6 | Chướng ngại vật: Chain Lock
- 751–1000: Màu 8 | Ống trống 1 | Bag-Timer: 4–5 | Chướng ngại vật: Cracked + Inversion

## 5) Quy tắc chi tiết cơ chế Túi (Bag) & Bag-Timer

- Giới thiệu: `Túi` là cơ chế mục tiêu, xuất hiện từ `bag_intro` (mặc định level 10). Khi một túi được kích hoạt, người chơi phải bỏ các vật phẩm màu mục tiêu vào Túi để hoàn thành.
- Kích hoạt Bag-Timer: Bag-Timer chỉ bắt đầu đếm khi người chơi thực hiện nước đi đầu tiên liên quan đến Túi (bỏ vật vào Túi).
- Hệ quả khi hết lượt:
  - CHƯƠNG 2: trả ngược tất cả vật màu trong Túi vào các ống ngẫu nhiên.
  - CHƯƠNG 3+: có thể áp dụng hình phạt bổ sung theo thiết kế (tham khảo data level).
- Hoàn thành Túi:
  - Khi hoàn thành túi theo mục tiêu level, cập nhật trạng thái `LevelComplete` hoặc mở khóa (với Chain Lock).
  - Hoàn thành 2 túi liên tiếp không nghỉ lượt sẽ cộng thêm 2 hiệp cho túi tiếp theo (hệ thống Combo).

## 6) Chi tiết chướng ngại vật & tương tác

- Frozen:
  - Xuất hiện theo `frozen_percent` trên số ống.
  - Phá băng bằng cách đổ 1 vật bất kỳ vào ống chứa băng.
- Hidden:
  - Một số vật ở đáy ống ẩn (số lớp `hidden_layers`). Người chơi không biết màu đáy cho tới khi mở lớp trên.
- Chain Lock:
  - Ống bị khóa hiển thị xích; chỉ mở khi hoàn thành túi màu chỉ định.
- Cracked Tube:
  - Ống có khả năng vỡ; nếu vật đứng yên quá nhiều lượt (theo `cracked_timeout`) sẽ mất.
- Inversion (xoay bàn):
  - Được kích hoạt theo `board_rotation_interval` (mặc định: 15 lượt tổng cho chương 5).

## 7) Nguyên tắc implement cho lập trình viên

- GameStateManager / ViewModel:
  - Theo dõi: currentLevel, moveCounter (cho level), bagState (active/complete/timeLeft), comboCounter, totalMoves (để tính inversion), seed.
  - Mọi thay đổi trạng thái phải phát ra snapshot immutable để UI render.
- RNG & seed:
  - Tất cả phép sinh ngẫu nhiên dùng RNG có seed được lưu trong dữ liệu level để đảm bảo replay và debug.
- Kiểm thử tự động (unit/integration):
  - Test Bag-Timer trigger, penalty khi hết lượt, Frozen phá băng, Chain Lock mở khóa, Hidden reveal, Cracked Tube timeout, Inversion sau N lượt.
- Giao diện dữ liệu level:
  - Level designer chỉ chỉnh `LEVEL_PRINCIPLES.md` và export JSON theo schema ở mục 3.
- Backwards compatibility:
  - Khi thay đổi schema level, cập nhật transformer migration và viết test chuyển đổi.

## 8) Hướng dẫn vận hành 1000 level (thiết kế & cân bằng)

- Hệ thống Combo: nếu người chơi hoàn thành 2 túi liên tiếp không nghỉ lượt, tặng +2 hiệp cho túi tiếp theo.
- Độ khó "Sóng": không đặt khó liên tục; cứ 3 màn khó (Hard) xen 2 màn dễ (Easy).
- Visual progression: thay đổi màu túi theo mốc level (vải → bạc → vàng) để tạo cảm giác tiến bộ.

## 9) Hợp đồng giữa Designer & Dev

- Mọi thay đổi thông số level (màu, ống, empty_tubes, bag_timer, obstacles) phải được ghi trong `LEVEL_PRINCIPLES.md` và file JSON level.
- Dev chỉ tin dữ liệu level làm nguồn thẩm quyền; không hardcode tham số trong code.

## 10) Kiểm tra/chạy thử (Quick checklist cho PR)

- Mở level mới: seed deterministic → board giống design.
- Kích hoạt Bag: bag_timer bắt đầu khi bỏ vật vào Túi.
- Hết Bag-Timer: hình phạt đúng theo chương.
- Frozen: phá băng bằng 1 vật bất kỳ.
- Chain Lock: mở đúng khi túi chỉ định hoàn thành.
- Cracked Tube: vật vỡ theo timeout.
- Inversion: xoay sau đúng N lượt tổng.

## 11) Lịch sử thay đổi

- v2.1 (2026-04-04): Đồng bộ và mở rộng quy tắc level theo `LEVEL_PRINCIPLES.md` (Bag, Bag-Timer, Frozen, Hidden, Chain Lock, Cracked, Inversion). Thêm schema dữ liệu level và checklist QA.
- v2.0 (2026-04-03): Chuyển nguyên lý hệ thống sang Android Native hoàn toàn, không dùng Unity runtime.

