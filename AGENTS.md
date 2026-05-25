# 仓库指南

## 项目结构与模块组织

这是一个基于 Kotlin 的多模块 Android 项目。目录结构如下：

- **`app/`** — 主应用模块（`com.pdfadder.app`）。入口文件：`MainActivity.kt`；应用外壳：`PdfAdderApp.kt`。
- **`core-api/`** — 共享库模块（`com.pdfadder.core`），提供通用接口。
- **`feature-merge/`** — 功能库模块（`com.pdfadder.feature.merge`），实现 PDF 合并功能。采用 MVVM 架构：
  - `pdf/PdfMerger.kt` — 核心合并逻辑。
  - `vm/MergeViewModel.kt` — UI 状态协调器。
  - `ui/MergeActivity.kt`、`MergeAdapter.kt` — 使用 ViewBinding 的 UI 层。
  - `repo/` — 数据访问层。
  - `MergeSpiImpl.kt` — SPI 实现，用于运行时功能发现。

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

- **语言**：Kotlin（JVM 1.8）。无独立 Linter，使用 Android Studio 的 **Reformat Code** 和 **Inspect Code**。
- **缩进**：4 个空格。**命名**：类名使用 `PascalCase`，成员名使用 `camelCase`。
- **一个类一个文件**，文件名与类名一致。
- **架构**：功能模块采用 MVVM。使用 ViewBinding，避免 `findViewById`。
- **功能注册**：新功能通过 `app` 模块中的 `FeatureRegistry.kt` 注册。

## 测试指南

当前代码库尚无自动化测试。添加测试时：

- 单元测试 → `src/test/kotlin/`（JUnit 4/5）。
- 仪器化测试 → `src/androidTest/kotlin/`（AndroidX Test）。
- 命名与生产代码对应：如 `PdfMerger.kt` 对应 `PdfMergerTest.kt`。

## 构建配置提示

- **最低 SDK**：24。**编译 SDK**：34。
- **ProGuard**：`app` 模块 Release 构建启用；库模块使用 consumer rules。
- **Maven 镜像**：已配置阿里云镜像。除非遇到网络问题，请保持。

## 本地环境路径

- JDK 17：`D:\java版本\jdk\jdk-17.0.12_windows-x64_bin\jdk-17.0.12`
- Android SDK：`C:\Users\lijiale\AppData\Local\Android\Sdk`
- 构建前设置：`$env:JAVA_HOME = "JDK路径"; .\gradlew assembleDebug`

## 文件操作规则

- **新建文件**：使用 Python 脚本写入，确保 UTF-8 编码无 BOM

  ```python
  import pathlib
  pathlib.Path("完整路径").write_text("""文件内容""", encoding="utf-8")
  ```

- **修改已有文件**：优先使用 `apply_patch` 生成 unified diff，只改目标行

  - 绝对禁止用 PowerShell 的 `Out-File` 或 `>` 重定向写入代码文件
  - 禁止用 `Set-Content` 写入 .kt / .kts / .xml 文件
  - 如果无法使用 `apply_patch`，直接把修改后的完整文件内容贴出来，让用户手动替换

- **读取文件**：使用 Python 脚本读取，确保正确处理 UTF-8 编码

  ```python
  import pathlib
  content = pathlib.Path("完整路径").read_text(encoding="utf-8")
  print(content)
  ```

​	禁止：使用 PowerShell 的 Get-Content 或 cat 读取 .kt、.kts、.xml 文件（会导致中文乱码）

## 禁止项

**禁止使用 PDFBox 或任何第三方 PDF 库**，PDF 处理仅限 Android 原生 API
