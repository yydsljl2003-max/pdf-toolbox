# PDF Adder

一款 Android 应用，支持多 PDF 文件排序合并。

## 项目结构

```
pdf-adder/
├── app/                    # 主应用模块
│   └── src/main/kotlin/com/pdfadder/app/
│       ├── MainActivity.kt          # 入口 Activity
│       ├── PdfAdderApp.kt           # 应用初始化
│       └── di/FeatureRegistry.kt    # 功能模块注册
├── core-api/               # 共享库：数据模型与 SPI 接口
│   └── src/main/kotlin/com/pdfadder/core/
│       ├── model/    # PdfFile, PdfInfo, MergeResult
│       ├── spi/      # PdfFeatureSpi
│       └── util/     # 工具方法
└── feature-merge/          # 功能库：PDF 合并
    └── src/main/kotlin/com/pdfadder/feature/merge/
        ├── pdf/PdfMerger.kt      # 合并核心逻辑
        ├── vm/MergeViewModel.kt  # ViewModel
        ├── ui/MergeActivity.kt   # 合并界面
        ├── ui/MergeAdapter.kt    # 文件列表适配器
        └── MergeSpiImpl.kt       # SPI 实现
```

## 技术栈

- **语言**：Kotlin
- **构建**：Gradle 8.2 (Kotlin DSL)
- **架构**：多模块 + MVVM
- **UI**：ViewBinding + RecyclerView
- **最低 SDK**：Android 7.0 (API 24)

## 构建与运行

### 环境要求

- Android Studio Hedgehog 或更新版本
- JDK 17+
- Android SDK 34

### 构建命令

```bash
# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK（启用混淆）
./gradlew assembleRelease

# 安装到设备
./gradlew installDebug

# 运行仪器化测试
./gradlew connectedAndroidTest
```

### 在 Android Studio 中运行

1. 用 Android Studio 打开项目根目录
2. 等待 Gradle 同步完成
3. 选择 `app` 作为运行模块
4. 点击运行按钮

## APK 下载

Release 包位于 `releases/` 目录：

- **PDF-Adder-v1.0.apk** — 当前版本

## 功能特性

- 选择多个 PDF 文件
- 拖动排序文件顺序
- 合并为单个 PDF 文件
- SPI 可扩展架构，便于后续添加新功能模块

## 许可证

MIT
