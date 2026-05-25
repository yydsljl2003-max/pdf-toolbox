# PDF Adder

> 一款 Android 应用，支持选择多个 PDF 文件、拖拽排序后合并为单个 PDF。

## 简介

PDF Adder 是一款轻量级 Android 工具 App，主要解决以下问题：

- 将分散的多个 PDF 文件合并成一个
- 通过拖拽自由调整文件的合并顺序
- 无需联网，所有操作在本地完成

**适用系统**：Android 7.0（API 24）及以上版本。

## 功能

- 从设备中多选 PDF 文件
- 拖拽列表项调整文件顺序
- 一键合并，输出单个 PDF
- 基于 SPI 的可扩展架构，方便后续添加新功能

## 下载 APK

Release 版本 APK 存放在项目的 `releases/` 文件夹下：

| 文件 | 说明 |
|---|---|
| `releases/PDF-Adder-v1.0.apk` | 当前发布版本 |

下载后直接在 Android 设备上安装即可使用。

## 构建

### 环境要求

- Android Studio Hedgehog 或更新版本
- JDK 17+
- Android SDK 34

### 常用命令

```bash
./gradlew assembleDebug          # 构建 Debug APK
./gradlew assembleRelease        # 构建 Release APK
./gradlew installDebug           # 安装到设备
```

也可以在 Android Studio 中直接选择 `app` 模块并点击运行。

## 项目结构

```
pdf-adder/
├── app/                    # 主应用
├── core-api/               # 共享库（数据模型、SPI 接口）
├── feature-merge/          # 功能模块（PDF 合并）
├── releases/               # 发布版 APK
└── docs/                   # 设计文档
```

## 技术栈

- Kotlin / Android
- Gradle 8.2（多模块 + MVVM）
- ViewBinding + RecyclerView

## 许可证

MIT
