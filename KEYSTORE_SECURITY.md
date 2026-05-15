# Hướng dẫn bảo mật keystore (release key)

Mục đích: tập hợp các bước an toàn để tạo, lưu trữ, sử dụng và phục hồi keystore (file `.jks`) dùng để ký bản phát hành Android. Không lưu mật khẩu hay keystore trong mã nguồn công khai.

---

## Tóm tắt nhanh

1. Tạo keystore cục bộ bằng `keytool`.
2. Lưu keystore an toàn (offline + mã hóa + backup). Không commit vào Git.
3. Đặt thông tin kết nối vào `local.properties` hoặc `~/.gradle/gradle.properties` (không commit).
4. Dùng CI/CD với secret store (GitHub Secrets/GCP Secret Manager) nếu tự động build.

---

## 1) Tạo keystore (ví dụ trên Windows PowerShell)

Chạy lệnh (thay đường dẫn, alias, và thông tin theo ý bạn):

```powershell
keytool -genkeypair -v -keystore C:/Users/You/keystores/release-keystore.jks \
  -alias twinbrother_key -keyalg RSA -keysize 2048 -validity 10000
```

- `keystore`: đường dẫn file `.jks` bạn sẽ dùng để ký. Nên để ở thư mục an toàn, không trong repo.
- `alias`: tên key (ví dụ `twinbrother_key`).
- `store password` & `key password`: nhớ rõ, lưu trong password manager.

## 2) Kiểm tra & lấy fingerprint

Kiểm tra nội dung keystore và fingerprint:

```powershell
keytool -list -v -keystore C:/Users/You/keystores/release-keystore.jks -alias twinbrother_key
```

Xuất certificate upload (PEM) để đăng lên Play Console (nếu cần):

```powershell
keytool -exportcert -rfc -alias twinbrother_key -keystore release-keystore.jks -file upload_cert.pem
```

Sao chép SHA-1 / SHA-256 nếu Play Console yêu cầu upload key.

## 3) Lưu trữ an toàn (bắt buộc)

- Không commit file `.jks` và `local.properties` vào Git. Thêm vào `.gitignore`:

```
/local.properties
**/release-keystore.jks
*.jks
```

- Backup keystore vào:
  - Ổ cứng ngoài (offline) mã hóa, hoặc
  - Cloud storage có mã hóa và khóa riêng (ví dụ Google Cloud Storage + CMEK), hoặc
  - Password manager / Secret manager (chỉ lưu mật khẩu), hoặc
  - Google Cloud Secret Manager / AWS Secrets Manager cho CI.

- Hạn chế quyền truy cập: chỉ 1-2 người tin cậy, đặt quyền filesystem (Linux: `chmod 600`) và lưu ở vị trí an toàn.

- Ghi lại nơi lưu backup và mật khẩu vào công cụ quản lý mật khẩu (Bitwarden, 1Password...).

## 4) Thêm thông tin keystore vào `local.properties` (không commit)

Tạo/ mở `local.properties` ở thư mục gốc project và thêm:

```
RELEASE_STORE_FILE=C:/Users/You/keystores/release-keystore.jks
RELEASE_STORE_PASSWORD=your_store_password
RELEASE_KEY_ALIAS=twinbrother_key
RELEASE_KEY_PASSWORD=your_key_password
```

Hoặc lưu trong `~/.gradle/gradle.properties` để không đưa vào repo.

> Lưu ý: `app/build.gradle.kts` trong project đã được cấu hình đọc các property này.

## 5) Build signed AAB cục bộ

Sau khi `local.properties` đã có thông tin, chạy:

```powershell
./gradlew.bat bundleRelease
```

File tạo ra: `app/build/outputs/bundle/release/app-release.aab`.

## 6) Kiểm tra file đã ký

Bạn có thể kiểm tra chữ ký của bundle bằng `jarsigner`:

```powershell
jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab
```

Hoặc kiểm tra chi tiết fingerprint của key bằng `keytool -list -v` như phía trên.

## 7) CI/CD (GitHub Actions) — lưu trữ an toàn & cách dùng

Gợi ý workflow (tóm tắt):

1. Lưu file keystore dưới dạng Base64 trong GitHub Secret (ví dụ `RELEASE_KEYSTORE_BASE64`).
2. Lưu mật khẩu và alias vào GitHub Secrets (`RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`).
3. Trong workflow: decode keystore ra file tạm, tạo `gradle.properties` (hoặc `local.properties`) từ secrets, chạy `./gradlew bundleRelease`.

Ví dụ snippet GitHub Actions:

```yaml
- name: Restore keystore
  run: |
    echo "$RELEASE_KEYSTORE_BASE64" | base64 -d > ${{ runner.temp }}/release-keystore.jks
  env:
    RELEASE_KEYSTORE_BASE64: ${{ secrets.RELEASE_KEYSTORE_BASE64 }}

- name: Create gradle.properties
  run: |
    echo "RELEASE_STORE_FILE=${{ runner.temp }}/release-keystore.jks" >> $GITHUB_WORKSPACE/gradle.properties
    echo "RELEASE_STORE_PASSWORD=${{ secrets.RELEASE_STORE_PASSWORD }}" >> $GITHUB_WORKSPACE/gradle.properties
    echo "RELEASE_KEY_ALIAS=${{ secrets.RELEASE_KEY_ALIAS }}" >> $GITHUB_WORKSPACE/gradle.properties
    echo "RELEASE_KEY_PASSWORD=${{ secrets.RELEASE_KEY_PASSWORD }}" >> $GITHUB_WORKSPACE/gradle.properties

- name: Build AAB
  run: ./gradlew bundleRelease
```

Không in secrets ra logs; dùng `echo` chèn thẳng vào file và xóa file keystore sau build.

## 8) Play App Signing (khuyến nghị)

- Kích hoạt Play App Signing (Google giữ key ký final). Bạn sẽ upload một **upload key** để Google dùng để verify uploads.
- Nếu bật Play App Signing, bạn có thể đăng ký upload key (có thể dùng keystore trên) và Play sẽ thay bạn ký bản final.
- Nếu thất lạc upload key: liên hệ Play Console để reset upload key (quy trình có thể mất thời gian). Vì vậy phải backup upload key an toàn.

## 9) Nếu lỡ mất keystore

- Nếu bạn đã bật Play App Signing và chỉ mất upload key: tạo khóa mới và yêu cầu Google reset upload key (Play Console → App signing → Request key reset) — theo hướng dẫn Play.
- Nếu bạn mất *app signing key* (Google giữ key khi bật Play App Signing): Google vẫn có key, bạn không mất khả năng cập nhật.
- Nếu chưa bật Play App Signing và mất keystore release: bạn không thể cập nhật app cũ (phải publish app mới với package khác).

## 10) Checklist bảo mật trước khi upload

- [ ] Keystore đã được tạo và backup ở ít nhất 2 nơi an toàn.
- [ ] `local.properties` chứa thông tin đã thêm và không bị commit.
- [ ] `.gitignore` chặn file `.jks` và `local.properties`.
- [ ] Mật khẩu lưu trong password manager, không lưu plaintext.
- [ ] CI secrets đã thiết lập nếu dùng CI.
- [ ] Bạn đã xuất fingerprint/PEM nếu Play Console yêu cầu.

---

## Tài nguyên tham khảo

- Google Play App Signing docs: https://developer.android.com/studio/publish/app-signing
- Gradle signing: https://developer.android.com/studio/build/configure-signing

---

Nếu bạn muốn, tôi có thể:
- Tạo mẫu `local.properties` (không chứa mật khẩu) trong repo, hoặc
- Chạy build `bundleRelease` sau khi bạn thêm `local.properties`, hoặc
- Tạo snippet GitHub Actions hoàn chỉnh cho repo này.

Hãy cho biết bạn muốn bước kế tiếp nào.