#!/usr/bin/env python3
"""
create_module.py - Script tự động tạo Android module chuẩn Clean Architecture & Jetpack Compose
cho dự án ApexFileManager.

Hỗ trợ các loại module:
- compose-lib: Android Library có tích hợp Jetpack Compose (ví dụ: :core:designsystem)
- lib: Android Library chuẩn thuần Android/Kotlin (ví dụ: :core:storage, :core:archive)
- feature: Feature module hoàn chỉnh với Activity, Compose, phụ thuộc sẵn :core:base, :core:designsystem, :core:storage
"""

import argparse
import os
import re
import sys
from pathlib import Path

BASE_PACKAGE = "app.pwhs.apexfilemanager"


def normalize_module_name(name: str) -> str:
    """Chuẩn hóa tên module dạng :core:storage hoặc core:storage thành :core:storage"""
    name = name.strip()
    if not name.startswith(":"):
        name = ":" + name
    return name


def module_to_path(module_name: str, root_dir: Path) -> Path:
    """Chuyển :core:storage thành path core/storage"""
    parts = [p for p in module_name.split(":") if p]
    return root_dir.joinpath(*parts)


def module_to_package(module_name: str) -> str:
    """Chuyển :core:storage thành app.pwhs.apexfilemanager.core.storage"""
    parts = [p.replace("-", "_") for p in module_name.split(":") if p]
    return f"{BASE_PACKAGE}.{'.'.join(parts)}"


def get_build_gradle_template(module_type: str, namespace: str) -> str:
    """Tạo nội dung build.gradle.kts chuẩn theo loại module"""
    if module_type == "compose-lib":
        return f"""plugins {{
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}}

android {{
    namespace = "{namespace}"
    compileSdk = 37

    defaultConfig {{
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }}

    buildTypes {{
        release {{
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }}
    }}
    compileOptions {{
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }}
    buildFeatures {{
        compose = true
    }}
}}

dependencies {{
    implementation(project(":core:base"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}}
"""
    elif module_type == "feature":
        return f"""plugins {{
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}}

android {{
    namespace = "{namespace}"
    compileSdk = 37

    defaultConfig {{
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }}

    buildTypes {{
        release {{
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }}
    }}
    compileOptions {{
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }}
    buildFeatures {{
        compose = true
    }}
}}

dependencies {{
    implementation(project(":core:base"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:storage"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}}
"""
    else:  # standard android library (pure logic / data / storage)
        return f"""plugins {{
    alias(libs.plugins.android.library)
}}

android {{
    namespace = "{namespace}"
    compileSdk = 37

    defaultConfig {{
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }}

    buildTypes {{
        release {{
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }}
    }}
    compileOptions {{
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }}
}}

dependencies {{
    implementation(project(":core:base"))
    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}}
"""


def update_settings_gradle(settings_file: Path, module_name: str) -> bool:
    """Tự động chèn include(":module_name") vào settings.gradle.kts nếu chưa có"""
    if not settings_file.exists():
        print(f"Error: {settings_file} không tồn tại!")
        return False

    content = settings_file.read_text(encoding="utf-8")
    include_statement = f'include("{module_name}")'

    if include_statement in content:
        print(f"ℹ️  {include_statement} đã tồn tại trong settings.gradle.kts.")
        return True

    # Thêm vào cuối file
    if not content.endswith("\n"):
        content += "\n"
    content += f"{include_statement}\n"

    settings_file.write_text(content, encoding="utf-8")
    print(f"✅ Đã thêm '{include_statement}' vào {settings_file.name}")
    return True


def create_module(root_dir: Path, module_name: str, module_type: str, custom_pkg: str = None, force: bool = False):
    module_name = normalize_module_name(module_name)
    module_dir = module_to_path(module_name, root_dir)
    namespace = custom_pkg or module_to_package(module_name)

    print(f"🚀 Đang tạo module: {module_name}")
    print(f"   - Thư mục: {module_dir.relative_to(root_dir)}")
    print(f"   - Kiểu: {module_type}")
    print(f"   - Namespace: {namespace}")

    if module_dir.exists() and any(module_dir.iterdir()) and not force:
        print(f"⚠️  Thư mục {module_dir} đã tồn tại và không rỗng! Dùng --force nếu muốn ghi đè.")
        return False

    module_dir.mkdir(parents=True, exist_ok=True)

    # 1. Tạo build.gradle.kts
    build_gradle = module_dir / "build.gradle.kts"
    build_gradle.write_text(get_build_gradle_template(module_type, namespace), encoding="utf-8")
    print(f"✅ Đã tạo {build_gradle.relative_to(root_dir)}")

    # 2. Tạo consumer-rules.pro
    consumer_rules = module_dir / "consumer-rules.pro"
    consumer_rules.write_text(f"# Consumer ProGuard rules for {module_name}\n", encoding="utf-8")
    print(f"✅ Đã tạo {consumer_rules.relative_to(root_dir)}")

    # 3. Tạo .gitignore cho module
    module_gitignore = module_dir / ".gitignore"
    module_gitignore.write_text("/build\n", encoding="utf-8")
    print(f"✅ Đã tạo {module_gitignore.relative_to(root_dir)}")

    # 4. Tạo cấu trúc thư mục source code
    pkg_path = namespace.replace(".", "/")
    src_java_dir = module_dir / f"src/main/java/{pkg_path}"
    src_java_dir.mkdir(parents=True, exist_ok=True)
    print(f"✅ Đã tạo package source: src/main/java/{pkg_path}")

    # 4. Tạo file placeholder marker
    placeholder = src_java_dir / "Placeholder.kt"
    placeholder.write_text(f"package {namespace}\n\n// Module {module_name}\n", encoding="utf-8")

    # 5. Cập nhật settings.gradle.kts
    settings_file = root_dir / "settings.gradle.kts"
    update_settings_gradle(settings_file, module_name)

    print(f"\n🎉 Tạo thành công module {module_name}!")
    print(f"👉 Hãy chạy './gradlew assembleDebug' để đồng bộ và kiểm tra.")
    return True


def main():
    parser = argparse.ArgumentParser(description="Script tự động tạo module cho dự án ApexFileManager")
    parser.add_argument("name", help="Tên module dạng :core:storage hoặc :features:explorer")
    parser.add_argument(
        "--type",
        choices=["lib", "compose-lib", "feature"],
        default="lib",
        help="Kiểu module: 'lib' (Android Library), 'compose-lib' (Library có Compose), 'feature' (Feature module UI + ViewModel)",
    )
    parser.add_argument("--package", help="Namespace tùy chỉnh (mặc định tự suy ra từ module name)")
    parser.add_argument("--force", action="store_true", help="Ghi đè nếu thư mục đã tồn tại")

    args = parser.parse_args()

    # Tìm root workspace
    current = Path.cwd()
    root_dir = current
    while not (root_dir / "settings.gradle.kts").exists():
        if root_dir.parent == root_dir:
            print("Error: Không tìm thấy thư mục root của dự án (chứa settings.gradle.kts)!")
            sys.exit(1)
        root_dir = root_dir.parent

    success = create_module(root_dir, args.name, args.type, args.package, args.force)
    sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()
