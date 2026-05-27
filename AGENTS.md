# 仓库指南

## 项目结构与模块组织

这是一个基于 Kotlin 的多模块 Android 项目。目录结构如下：

- **`app/`** — 主应用模块（`com.pdfadder.app`）。入口文件：`MainActivity.kt`；应用外壳（Application）：`PdfAdderApp.kt`。
  - `MainActivity.kt` — 主界面，动态渲染功能入口按钮。
  - `di/FeatureRegistry.kt` — 功能注册中心，所有 SPI 实现在此注册。
  - 资源：`layout/activity_main.xml`、`layout/item_feature.xml`。

- **`core-api/`** — 共享库模块（`com.pdfadder.core`），提供通用接口与数据模型。
  - `spi/PdfFeatureSpi.kt` — SPI 接口，每个 PDF 功能模块必须实现。
  - `model/PdfFile.kt` — 用户选择的 PDF 文件信息（`PdfFile` 数据类）。
  - `model/MergeResult.kt` — 合并结果（`MergeResult`、`PdfInfo` 数据类）。
  - `util/FileExt.kt` — Uri 扩展函数（`getFileName`、`getFileSize`、`formatFileSize`）。

- **`feature-merge/`** — 功能库模块（`com.pdfadder.feature.merge`），实现 PDF 合并功能。采用 MVVM 架构：
  - `pdf/PdfMerger.kt` — 核心合并逻辑（使用 MuPDF Java API）。
  - `vm/MergeViewModel.kt` — UI 状态协调器，管理文件列表与合并操作。
  - `ui/MergeActivity.kt`、`MergeAdapter.kt` — 使用 ViewBinding 的 UI 层，支持拖拽排序与删除。
  - `MergeSpiImpl.kt` — SPI 实现，用于运行时功能发现。
  - 资源：`layout/activity_merge.xml`、`layout/item_file.xml`。

- **`releases/`** — 发布版 APK 目录（v1.0、v1.1、v1.2）。

源码位于 `src/main/kotlin/<包名>/`，资源文件位于 `src/main/res/`。


## 构建、测试与开发命令

| 命令 | 说明 |
|---|---|
| `.\gradlew assembleDebug` | 构建所有模块的 Debug APK。 |
| `.\gradlew assembleRelease` | 构建 Release APK（`app` 模块启用 R8 混淆）。 |
| `.\gradlew :feature-merge:assembleDebug` | 仅构建 feature-merge 库。 |
| `.\gradlew installDebug` | 将 Debug APK 安装到已连接的设备。 |
| `.\gradlew connectedAndroidTest` | 在已连接的设备上运行仪器化测试。 |

始终使用 Gradle 包装器（`gradlew`）以保证一致性（Gradle 8.2）。

## 编码风格与命名规范

- **语言**：Kotlin（JVM 1.8 / Kotlin 1.9.20）。无独立 Linter，使用 Android Studio 的 **Reformat Code** 和 **Inspect Code**。
- **缩进**：4 个空格。**命名**：类名使用 `PascalCase`，成员名使用 `camelCase`。
- **一个类一个文件**，文件名与类名一致。
- **架构**：功能模块采用 MVVM。使用 ViewBinding，避免 `findViewById`。
- **功能注册**：新功能通过 `app` 模块中的 `di/FeatureRegistry.kt` 注册。
- **协程**：异步操作使用 `viewModelScope.launch(Dispatchers.IO)`。

## 测试指南

当前代码库尚无自动化测试。添加测试时：

- 单元测试 → `src/test/kotlin/`（JUnit 4/5）。
- 仪器化测试 → `src/androidTest/kotlin/`（AndroidX Test）。
- 命名与生产代码对应：如 `PdfMerger.kt` 对应 `PdfMergerTest.kt`。


## 构建配置提示

- **最低 SDK**：24。**编译 SDK**：34。
- **ProGuard**：`app` 模块 Release 构建启用 R8 混淆（`isMinifyEnabled = true`；库模块（`core-api`、`feature-merge`）Release 构建未启用混淆（`isMinifyEnabled = false`）。
- **Maven 镜像**：已配置阿里云镜像 + Ghostscript Maven（MuPDF）。除非遇到网络问题，请保持。
- **Gradle Wrapper**：使用腾讯云镜像下载 Gradle（`mirrors.cloud.tencent.com`）。

## MuPDF 依赖说明

- `feature-merge/libs/` 包含：
  - `fitz-1.27.1.jar` — MuPDF Java 绑定（项目通过 flat jar 依赖）。
  - `fitz-1.27.1.aar` — MuPDF AAR 原始文件（备用）。
  - `classes.jar` — 提取的 classes 文件。
  - `fitz-extracted/` — AAR 解压目录（不纳入 Git）。
- `feature-merge/jni/` — 4 种 ABI 的 native 库（arm64-v8a、armeabi-v7a、x86、x86_64）。
- `feature-merge/src/main/jniLibs/` — Gradle 打包时使用的 native 库目录。

**MuPDF 使用规范**：
- `Context.init()` 调用需在合并前执行（幂等）。
- `PDFDocument.graftPage()` 用于页面合并（3 参数版本：`graftPage(int, PDFDocument, int)`）。
- `Document.save(path, pdf)` 仅支持文件路径，不支持普通 OutputStream；需先保存到临时文件再复制到输出 URI。
- 所有文档对象使用后需调用 `destroy()` 释放资源。

**禁止使用 PDFBox、iText 等第三方 PDF 库**，PDF 处理仅限 MuPDF。

## 本地环境路径

- JDK 17：`D:\java版本\jdk\jdk-17.0.12_windows-x64_bin\jdk-17.0.12`
- Android SDK：`D:\Android_Studio_SDK`
- 构建前设置：`$env:JAVA_HOME = "JDK路径"; .\gradlew assembleDebug`


## 文件操作规则

- **新建文件**：使用 Python 脚本写入，确保 UTF-8 编码无 BOM

  ```python
  import pathlib
  pathlib.Path("complete_path").write_text("file_content", encoding="utf-8")
  ```

- **修改已有文件**：优先使用 `apply_patch` 生成 unified diff，只改目标行

  - 绝对禁止用 PowerShell 的 `Out-File` 或 `>` 写定写代码文件
  - 禁用 `Set-Content` 写入 .kt / .kts / .xml 文件
  - 如果无法使用 `apply_patch`，直接把修改后的完整文件内容贴出来，让用户手动替换

- **读取文件**：使用 Python 脚本读取，确保正确处理 UTF-8 编码

  ```python
  import pathlib
  content = pathlib.Path("complete_path").read_text(encoding="utf-8-sig")
  print(content)
  ```

  禁止：使用 PowerShell 的 `Get-Content` 或 `cat` 读取 .kt、.kts、xml 文件（会导致中文乱码）
