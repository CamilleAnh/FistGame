# THIET KE MAN 1 - XEP HOP MAU VAO THUNG

Phien ban: 2.0  
Ngay cap nhat: 2026-04-03

## 1) Muc tieu gameplay Man 1

Man 1 theo dung y tuong anh mau tham chieu:
- Nguoi choi sap xep cac hop mau nho vao cac thung dung thang.
- Khi tat ca thung da hoan thanh (moi thung chi 1 mau hoac rong), thi qua man.

Muc tieu hoc nguoi choi moi:
- Hieu luat chon thung nguon -> chon thung dich.
- Hieu luat chi duoc do/chuyen dung mau hop tren cung.

## 2) Pham vi mau va do kho

Muc tieu toan game:
- Su dung 7-8 mau co ban (tim, xanh la, xanh duong, do, vang, cam, cyan, trang/xam nhat).

Rieng Man 1 (de, nhap mon):
- Chi dung 4-5 mau.
- So thung tong: 6-7 thung.
- Trong do co 1-2 thung rong de nguoi choi co cho xoay so.

## 3) Cau truc du lieu cho 1 thung

Moi thung la 1 cot dung, suc chua co dinh `capacity = 4` o.

Moi thung gom:
- `stack`: danh sach cac o mau tu duoi len tren.
- `isLocked`: false (Man 1 chua khoa thung).
- `isCompleted`: true khi:
  - Thung rong, hoac
  - Thung day 4 o va ca 4 o cung 1 mau.

## 4) Quy tac random khoi tao Man 1

Random phai dam bao man giai duoc:
- Chon ngau nhien 4 hoac 5 mau tu bo 7-8 mau tong.
- Moi mau xuat hien dung 4 lan (tong so block theo mau).
- Tron va phan bo vao cac thung khong rong.
- Luon tao it nhat 1 cach giai (co the tao tu trang thai da giai roi xao tron bang cac nuoc hop le nguoc).

Luu y:
- Khong random kieu gay dead-lock ngay tu dau.

## 5) Luat thao tac nguoi choi (core rule)

1. Chon thung nguon
- Hop le khi thung nguon khong rong.

2. Chon thung dich
- Hop le khi:
  - Thung dich chua day, va
  - Thung dich rong, hoac mau o tren cung cua dich trung mau o tren cung cua nguon.

3. So hop duoc chuyen
- Chuyen theo cum lien tiep cung mau o dinh thung nguon.
- So luong chuyen toi da = so o trong con lai cua thung dich.

4. Hoat anh chuyen
- Moi lan chuyen la animation ngan (0.12-0.2s/hop) de de nhin tren mobile.

## 6) Dieu kien hoan thanh va qua man

Qua man khi tat ca thung thoa 1 trong 2 dieu kien:
- Rong, hoac
- Day va dong nhat 1 mau.

Khi hoan thanh:
- Goi `GameManager.LevelComplete(autoLoadNext: true)`.
- Khong script nao khac tu y doi `Time.timeScale`.

## 7) Luong state bat buoc (theo he thong hien tai)

- Vao gameplay: `MainMenu` -> `Playing`.
- Dang choi Man 1: giu `Playing`.
- Hoan thanh: `Playing` -> `Paused` tam thoi trong flow complete -> load man tiep.

Cam:
- Cam script gameplay goi truc tiep `SceneManager.LoadScene`.
- Cam script gameplay dat truc tiep `Time.timeScale`.

## 8) Thanh phan can co trong Unity Scene (khong dung Canvas gameplay)

Theo rang buoc project:
- Gameplay dung `SpriteRenderer` + `BoxCollider2D`.
- Khong dung Unity UI Canvas cho logic choi.

Cac object toi thieu:
- `GameManager` (co san).
- `LevelBoardController` (quan ly board/thung/kiem tra win).
- `TubeSlot_*` (moi thung la mot object co collider de click/cham).
- `ColorBlock_*` (hop mau nho la sprite 2D).

## 9) Cau hinh de xuat cho Man 1 (ban de)

De xuat 5 mau, 7 thung, capacity 4:
- 5 thung co block mau (moi mau tong 4 block).
- 2 thung rong.

Muc tieu do kho:
- So buoc du kien de giai: 12-20 nuoc.

## 10) Checklist trien khai nhanh

1. Tao scene `Level_01_SortBox` va them vao Build Settings.
2. Tao prefab `TubeSlot` (Sprite + BoxCollider2D).
3. Tao prefab `ColorBlock` (Sprite mau).
4. Viet `LevelBoardController` de xu ly:
   - Random level seed
   - Chon nguon/dich
   - Kiem tra nuoc di hop le
   - Kiem tra win
   - Goi `GameManager.LevelComplete()`
5. Test tren chuot (PC) va touch (Android).

## 11) Tieu chi nghiem thu Man 1

Dat khi:
- Co 4-5 mau trong man.
- Co 1-2 thung rong.
- Random moi lan vao man cho bo tri khac nhau (co the theo seed).
- Hoan tat tat ca thung dung quy tac thi tu dong qua man.
- Khong vi pham quy tac state cua `GameManager`.

## 12) Rui ro can canh bao

- `NullReferenceException`: board chua khoi tao ma da nhan input.
- Sai mapping mau -> sprite khi random.
- Random khong kiem soat co the tao man vo nghiem.
- Lech dong bo Android-Unity neu chi so level o Android khong map dung scene Unity.

## ⚠️ Luu y Inspector

- `LevelBoardController.colorsPool`: khai bao du 7-8 mau tong.
- `LevelBoardController.level1ColorCount`: dat 4 hoac 5.
- `LevelBoardController.tubeCount`: dat 6 hoac 7 (khuyen nghi 7).
- `LevelBoardController.emptyTubeCount`: dat 1 hoac 2 (khuyen nghi 2 cho nguoi moi).
- Tat ca `TubeSlot_*` bat buoc co `BoxCollider2D` de nhan click/cham.
