---
name: create-module
description: Tự động hóa quá trình tạo Android module mới chuẩn Clean Architecture và Jetpack Compose cho ApexFileManager. Tự động cấu hình build.gradle.kts, consumer-rules.pro, package source folder và chèn include vào settings.gradle.kts. Kích hoạt khi user hoặc AI cần tạo module mới (ví dụ "tạo module :core:storage", "tạo module :feature:explorer", "create module").
---

# Create Module Workflow & Skill

Skill này tự động hóa việc khởi tạo một Android Library hoặc Feature Module mới cho dự án ApexFileManager, tuân thủ nghiêm ngặt quy định đặt tên package, versions từ `libs.versions.toml` và không bao giờ phải gõ file cấu hình thủ công.

## Khi Nào Sử Dụng
- Khi thêm một tầng chức năng quan trọng (vd: `:core:storage`, `:core:archive`, `:core:designsystem`).
- Khi tạo một màn hình/tính năng mới dạng module độc lập (vd: `:features:explorer`, `:features:viewer`).

## Các Loại Module Hỗ Trợ

| Type Flag | Loại Module | Mục Đích Sử Dụng |
| :--- | :--- | :--- |
| `--type lib` | Android Library chuẩn | Dành cho tầng logic, data, core I/O, domain (không cần Compose UI). Vd: `:core:storage`, `:core:archive`. |
| `--type compose-lib`| Android Library + Compose | Dành cho thư viện UI dùng chung, design system. Vd: `:core:designsystem`. |
| `--type feature` | Feature Module (Activity + Compose) | Dành cho màn hình hoàn chỉnh, đã cấu hình sẵn dependencies `:core:base`, `:core:designsystem`, `:core:storage` và Lifecycle ViewModel Compose. Vd: `:features:explorer`. |

## Cách Sử Dụng

### 1. Qua Shell Script trực tiếp:
```bash
# Tạo thư viện UI design system
./scripts/create-module.sh :core:designsystem --type compose-lib

# Tạo core storage engine
./scripts/create-module.sh :core:storage --type lib

# Tạo feature explorer
./scripts/create-module.sh :features:explorer --type feature
```

### 2. Qua Python Script:
```bash
python3 .agents/skills/create-module/scripts/create_module.py :core:storage --type lib
```

## Các Bước Script Tự Động Thực Hiện:
1. Chuẩn hóa tên module (`:core:storage` -> đường dẫn `core/storage`).
2. Sinh `build.gradle.kts` chuẩn AGP 9.2.1, compileSdk 37, minSdk 24, tham chiếu `libs.versions.toml`.
3. Sinh `consumer-rules.pro`.
4. Sinh file `.gitignore` cục bộ chứa `/build`.
5. Tạo cây thư mục source package `src/main/java/app/pwhs/apexfilemanager/...`.
6. Tự động thêm `include(":module:name")` vào `settings.gradle.kts` nếu chưa có.
7. Hỗ trợ cờ `--force` nếu muốn ghi đè cấu hình.

## Bước Sau Khi Tạo Module
Sau khi chạy lệnh tạo module, AI hoặc Developer **bắt buộc** chạy lệnh verify:
```bash
./gradlew assembleDebug
```
đảm bảo Gradle sync và compile thành công.
