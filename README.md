# PDF Adder

> 一款 Android 应用，支持选择多个 PDF 文件、拖拽排序后合并为单个 PDF。

## 简介

PDF Adder 是一款 Android 工具 App，主要解决以下问题：

- 将分散的多个 PDF 文件合并成一个
- 拖拽调整文件顺序
- 一键合并，本地完成

**适用系统：Android 7.0（API 24）以上。

## 功能

- 从设备中多选 PDF 文件
- 拖拽列表项调整文件顺序
- 一键合并，输出单个 PDF
- 基于 SPI 可扩展架构

## 安装方式

1. 下载 releases 目录下的最新 APK
2. 传输到 Android 设备
3. 允许未知来源安装
4. 点击 APK 完成安装

## 版本更新记录

### v1.3（最新）

- 切换到 MuPDF 合并引擎，解决闪退、模糊、卡顿问题
- 新增 MuPDF native 库（4 种 ABI）
- 新增 Bug 报告 Excel 模板

### v1.2

- Material Design UI 改版
- CardView 卡片列表首页
- 文件列表删除按钮
- 虚线边框添加区域

### v1.1

- 修复 PDF 合并模糟问题

### v1.0

- 初始版本

## 构建

### 环境要求

- Android Studio Hedgehog+
- JDK 17+
- Android SDK 34

### 常用命令

    gradlew assembleDebug          # 构建 Debug APK
    gradlew assembleRelease        # 构建 Release APK
    gradlew installDebug           # 安装到设备

## 项目结构

    pdf-adder/
    app/                    # 主应用
    core-api/               # 共享库
    feature-merge/          # PDF 合并模块
    releases/               # 发布版 APK

## 技术栈

- Kotlin / Android
- Gradle 8.2（MVVM）
- ViewBinding + RecyclerView
- Material Components 1.9.0

## 许可证

MIT
