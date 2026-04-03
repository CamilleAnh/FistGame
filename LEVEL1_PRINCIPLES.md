# THIET KE MAN 1 - LOGIC ANDROID NATIVE (THEO MASTER)

Phien ban: 3.0
Ngay cap nhat: 2026-04-03

Tai lieu nay duoc cap nhat theo:
- `MASTER_PROMPT.md`
- `GAME_SYSTEM_PRINCIPLES.md` (ban khong dung Unity)

## 1) Muc tieu Man 1

Man 1 la puzzle xep hop mau vao thung dung thang:
- Nguoi choi chon 1 thung nguon, sau do chon 1 thung dich.
- Cac hop mau tren cung se duoc chuyen theo luat hop le.
- Khi tat ca thung da "hoan thanh" thi qua man.

Yeu cau do kho Man 1:
- Chi dung 4-5 mau (nhap mon).
- So thung tong: 6-7 thung.
- Co 1-2 thung rong de thao tac.

Yeu cau he mau toan game:
- Tong he mau su dung 7-8 mau co ban.

## 2) Kien truc bat buoc (khong Unity)

1. App chay 100% Android Native (Kotlin/XML).
2. Khong dung Unity runtime, khong dung scene Unity, khong dung script C#.
3. Gameplay render bang Android View:
  - `ConstraintLayout`, `GridLayout`, `ImageView`, `TextView`.
4. Logic game dat trong Kotlin (Domain), UI chi render state.

## 3) Trang thai va luong man hinh

Trang thai hop le:
- `MainMenu`, `LevelSelect`, `Playing`, `Paused`, `GameOver`, `LevelComplete`.

Luong Man 1:
- `FirstFragment` (MainMenu) -> `SecondFragment` (LevelSelect).
- Tu LevelSelect vao gameplay Man 1 (`Playing`).
- Khi tat ca thung hoan thanh -> `LevelComplete` -> dieu huong sang man tiep theo hoac quay LevelSelect.

## 4) Cau truc du lieu Man 1

### 4.1 Model
- `enum class ColorId { PURPLE, GREEN, BLUE, RED, YELLOW, ORANGE, CYAN, WHITE }`
- `data class TubeState(val blocks: MutableList<ColorId>, val capacity: Int = 4)`
- `data class LevelState(val tubes: MutableList<TubeState>, val selectedTubeIndex: Int?)`

### 4.2 Dinh nghia "thung hoan thanh"
Mot thung duoc xem la hoan thanh khi:
- Rong, hoac
- Day (`size == capacity`) va tat ca block cung 1 mau.

## 5) Quy tac random khoi tao

Man 1 random co kiem soat:
1. Chon ngau nhien 4 hoac 5 mau tu pool 7-8 mau.
2. Moi mau lap lai dung 4 block.
3. Phan bo vao cac thung co du lieu + de 1-2 thung rong.
4. Bat buoc level giai duoc:
  - Cach an toan: tao trang thai da giai truoc, sau do xao tron bang cac nuoc di hop le nguoc.

Khong chap nhan:
- Seed random tao level vo nghiem.

## 6) Luat thao tac nguoi choi

1. Chon thung nguon hop le khi thung khong rong.
2. Chon thung dich hop le khi:
  - Chua day, va
  - Rong hoac block tren cung trung mau block tren cung cua nguon.
3. So block duoc chuyen:
  - Lay cum lien tiep cung mau tren dinh nguon.
  - Chi chuyen toi da bang so cho trong cua dich.
4. Moi lan chuyen cap nhat state 1 lan, sau do UI render lai.

## 7) Dieu kien qua man

Qua man khi tat ca thung deu hoan thanh theo dinh nghia muc 4.2.

Khi qua man:
- Set state `LevelComplete`.
- Thuc hien dieu huong qua man tiep theo bang Navigation Component.

## 8) File muc tieu can lam (theo master)

File Android can sua/them khi trien khai Man 1:
1. `.xml`:
  - `app/src/main/res/layout/fragment_second.xml` (nut vao man choi).
  - `app/src/main/res/layout/fragment_level_one.xml` (layout gameplay Man 1).
2. `.kt`:
  - `app/src/main/java/com/example/a2dgame/SecondFragment.kt` (dieu huong vao Man 1).
  - `app/src/main/java/com/example/a2dgame/LevelOneFragment.kt` (xu ly input + bind UI).
  - `app/src/main/java/com/example/a2dgame/game/LevelOneEngine.kt` (luat game thuần Kotlin).

## 9) Rang buoc lifecycle va clean code

1. Dung ViewBinding, khong dung `findViewById`.
2. Neu co coroutine/timer/loop, huy trong `onDestroyView()`.
3. Tach ro UI / Domain / Data, khong de logic puzzle nam trong XML.

## 10) Checklist nghiem thu Man 1

1. Man 1 hien thi dung 6-7 thung, trong do co 1-2 thung rong.
2. So mau dung trong man la 4 hoac 5.
3. Moi lan choi co the random bo tri (co seed de debug).
4. Luat chuyen hop dung 100% (khong cho phep nuoc di sai).
5. Hoan tat tat ca thung thi qua man ngay.
6. Khong co crash khi xoay man hinh hoac quay lui.

## 11) Rui ro ky thuat can canh bao

- `NullPointerException` khi binding hoac state chua khoi tao.
- Khong huy coroutine trong `onDestroyView()` gay memory leak.
- Random sai thuat toan tao level vo nghiem.
- Dieu huong sai action trong `nav_graph.xml` gay vo back stack.

## ⚠️ Luu y Build

- Dam bao `nav_graph.xml` co route vao `LevelOneFragment`.
- Dam bao image tai nguyen mau da duoc them vao `res/drawable` truoc khi bind trong Kotlin.
- Neu bo sung file Kotlin moi, can dung dung package `com.example.a2dgame` (hoac subpackage thong nhat).
