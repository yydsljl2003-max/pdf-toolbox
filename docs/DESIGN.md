# PDF Adder — Android App 设计文档

## 1. 项目概览

| 项目 | 说明 |
|---|---|
| **包名** | com.pdfadder.app |
| **最低 SDK** | Android 7.0 (API 24) |
| **目标 SDK** | API 34 |
| **开发语言** | Kotlin |
| **PDF 引擎** | PdfBox-Android |
| **架构** | MVVM + 功能模块化 |
| **UI 风格** | 极简、大按钮(≥56dp)、大字体(≥18sp) |

---

## 2. Gradle 多模块结构

`
pdf-adder/
├── settings.gradle.kts          # 声明模块
├── build.gradle.kts             # 顶层插件管理
├── gradle.properties            # 全局版本配置
├── app/                         # 壳工程（零业务逻辑）
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── di/                  # 依赖注入组装
│       ├── MainActivity.kt      # 功能入口页
│       └── PdfAdderApp.kt       # Application
├── core-api/                    # 接口定义（无实现）
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/pdfadder/core/
│       ├── model/
│       │   ├── PdfFile.kt       # 数据模型
│       │   └── MergeResult.kt   # 合并结果
│       ├── spi/
│       │   └── PdfFeatureSpi.kt # 功能插件接口
│       └── util/
│           └── FileExt.kt       # 通用工具
├── feature-merge/               # PDF 合并功能模块
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml  # 声明自己的 Activity
│       └── src/main/kotlin/com/pdfadder/feature/merge/
│           ├── MergeSpiImpl.kt  # 实现核心接口
│           ├── ui/
│           │   ├── MergeActivity.kt
│           │   ├── MergeFragment.kt
│           │   └── MergeAdapter.kt  # RecyclerView 适配器
│           ├── vm/
│           │   └── MergeViewModel.kt
│           ├── repo/
│           │   └── MergeRepository.kt
│           └── pdf/
│               └── PdfMerger.kt     # PdfBox 调用层
└── buildSrc/                    # 版本统一管理（可选）
    └── src/main/kotlin/Versions.kt
`

---

## 3. 模块间接口定义

### 3.1 PdfFeatureSpi — 功能插件接口

`kotlin
// core-api/src/main/kotlin/com/pdfadder/core/spi/PdfFeatureSpi.kt

/**
 * 每个 PDF 功能模块必须实现的接口。
 * 新增功能：新建模块 + 实现此接口 + 在 app 模块注册，无需改已有代码。
 */
interface PdfFeatureSpi {

    /** 功能显示名称，用于主界面入口按钮 */
    val featureName: String

    /** 功能图标资源 ID（可选，默认用通用图标） */
    val featureIconResId: Int?

    /** 功能描述文字，显示在按钮下方 */
    val featureDescription: String

    /** 启动该功能的 Activity */
    val launchActivity: Class<*>

    /**
     * 执行功能（从 Activity 的 intent 携带数据）
     * @param context 应用上下文
     * @param requestCode 请求码，用于回调结果
     */
    fun launch(context: Context, requestCode: Int)
}
`

### 3.2 数据模型

`kotlin
// core-api/src/main/kotlin/com/pdfadder/core/model/PdfFile.kt

/** 用户选择的单个 PDF 文件 */
data class PdfFile(
    val uri: Uri,                    // 文件 URI
    val displayName: String,         // 显示文件名
    val fileSize: Long,              // 文件大小（字节）
    val pageCount: Int,              // 页数（懒加载）
    var sortOrder: Int               // 排序序号
)

// core-api/src/main/kotlin/com/pdfadder/core/model/MergeResult.kt

/** 合并结果 */
data class MergeResult(
    val success: Boolean,
    val outputFileUri: Uri? = null,  // 输出文件 URI（成功时）
    val errorMessage: String? = null // 失败原因
)
`

### 3.3 结果回调约定

`kotlin
// 合并模块向主界面回传结果
// 使用 Activity.onActivityResult 或 ActivityResultLauncher
// resultCode = RESULT_OK → 合并成功，data 中携带输出 URI
// resultCode = RESULT_CANCELED → 用户取消
// resultCode = RESULT_ERROR → 合并失败，data 中携带错误消息
`

---

## 4. 核心类设计

### 4.1 app 模块 — 壳工程职责

| 类 | 职责 |
|---|---|
| PdfAdderApp | Application 子类，初始化 DI 容器，注册所有 PdfFeatureSpi |
| MainActivity | 主界面，从 PdfAdderApp 获取所有已注册的功能入口，渲染按钮列表 |
| FeatureRegistry | 单例，存储 List<PdfFeatureSpi>，供主界面读取 |

**关键伪代码 — 功能注册：**

`kotlin
// app/src/main/kotlin/com/pdfadder/app/PdfAdderApp.kt

class PdfAdderApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FeatureRegistry.registerAll(listOf(
            MergeSpiImpl(this)  // 合并功能模块
            // 后续添加: WordConvertSpiImpl(), PageRotateSpiImpl()...
        ))
    }
}

// app/src/main/kotlin/com/pdfadder/app/di/FeatureRegistry.kt

object FeatureRegistry {
    private val _features = mutableListOf<PdfFeatureSpi>()
    val features: List<PdfFeatureSpi> get() = _features

    fun registerAll(newFeatures: List<PdfFeatureSpi>) {
        _features.addAll(newFeatures)
    }
}
`

### 4.2 feature-merge 模块 — 合并功能

| 类 | 职责 |
|---|---|
| MergeSpiImpl | 实现 PdfFeatureSpi，启动 MergeActivity |
| MergeActivity | 合并功能主页面（可选，也可用 Fragment） |
| MergeFragment | UI 承载，包含三个区域：文件选择、排序列表、合并按钮 |
| MergeAdapter | RecyclerView 适配器，支持拖拽排序 |
| MergeViewModel | MVVM 的 VM，持有 ObservableList<PdfFile> |
| MergeRepository | 文件读取 + PdfBox 合并逻辑 |
| PdfMerger | 纯业务类，封装 PdfBox 所有操作 |

---

## 5. MVVM 数据流 & 交互逻辑

`
┌──────────────────────────────────────────────────────┐
│                   MergeFragment (UI)                  │
│                                                       │
│  ┌──────────┐    ①用户点击    ┌──────────────────┐   │
│  │  选择文件  │ ──────────────→│ MergeViewModel   │   │
│  │  大按钮   │                 │ pdfFiles:        │   │
│  │  (56dp)  │                 │ ObservableList   │   │
│  └──────────┘                 │ <PdfFile>        │   │
│                               └────────┬─────────┘   │
│                                        │ ②数据变更   │
│                                        ↓              │
│                               ┌──────────────────┐   │
│                               │  MergeAdapter    │   │
│                               │  (RecyclerView)  │   │
│                               │  ItemTouchHelper │   │
│                               │  (拖拽排序)       │   │
│                               └────────┬─────────┘   │
│                                        │ ③拖拽更新   │
│                                        ↓              │
│  ┌──────────┐    ④用户点击    ┌──────────────────┐   │
│  │  合并按钮  │ ──────────────→│ MergeViewModel   │   │
│  │  (56dp)   │                 │ merge()          │   │
│  └──────────┘                 └────────┬─────────┘   │
│                                        │ ⑤调用仓库   │
│                                        ↓              │
│                               ┌──────────────────┐   │
│                               │ MergeRepository  │   │
│                               │ readAll() +      │   │
│                               │ mergePdfBoxes()  │   │
│                               └────────┬─────────┘   │
│                                        │ ⑥返回结果   │
│                                        ↓              │
│                               ┌──────────────────┐   │
│                               │ MergeActivity    │   │
│                               │ setResult()      │   │
│                               └──────────────────┘   │
└──────────────────────────────────────────────────────┘
`

---

## 6. UI 原型描述

### 6.1 主界面（MainActivity）

`
┌─────────────────────────────────┐
│                                 │
│       📄  PDF Adder              │
│                                 │
│          ┌──────────────────┐   │
│          │    📑 合并PDF     │   │  ← 大按钮(56dp, 24sp)
│          │   多个PDF合成一个   │   │
│          └──────────────────┘   │
│                                 │
│     （未来可扩展更多功能按钮）     │
│                                 │
│                                 │
└─────────────────────────────────┘

配色：白色背景 + 蓝色主题(#2196F3) + 圆角卡片
间距：按钮间 16dp，内容左右边距 24dp
`

### 6.2 合并界面（MergeFragment）

`
┌─────────────────────────────────┐
│  ← 返回                         │
│                                 │
│     ┌──────────────────────┐    │
│     │  📂 选择PDF文件       │    │  ← 超大按钮(64dp, 20sp)
│     │  (点击打开文件选择器)  │    │
│     └──────────────────────┘    │
│                                 │
│     ┌──────────────────────┐    │
│     │  📄 file1.pdf  3页   │    │  ← RecyclerView 列表
│     │  📄 file2.pdf  5页   │    │     可上下拖拽排序
│     │  📄 file3.pdf  2页   │    │     每行高度 64dp
│     └──────────────────────┘    │
│                                 │
│     ┌──────────────────────┐    │
│     │   🔗 合并为1个PDF     │    │  ← 合并按钮(56dp, 22sp)
│     └──────────────────────┘    │
│                                 │
└─────────────────────────────────┘

文件为空时：显示提示"请先选择PDF文件"
文件不足时：合并按钮置灰不可点击
`

---

## 7. 关键代码框架

### 7.1 PdfMerger — PdfBox 封装（已修正逐页导入）

`kotlin
// feature-merge/src/main/kotlin/com/pdfadder/feature/merge/pdf/PdfMerger.kt

class PdfMerger {

    /**
     * 按指定顺序合并多个 PDF
     * 正确做法：遍历 sourceDoc.pages，逐页导入到新文档
     * @param pdfFiles 已排序的文件列表
     * @param outputUri 输出文件 URI
     * @return 成功/失败
     */
    fun merge(
        pdfFiles: List<PdfFile>,
        outputUri: Uri,
        context: Context
    ): MergeResult {
        return try {
            val outputDoc = PDDocument()
            pdfFiles.forEach { pdfFile ->
                val inputStream = context.contentResolver.openInputStream(pdfFile.uri)
                    ?: throw IOException(\"无法读取文件: \")
                val sourceDoc = PDDocument.load(inputStream)

                // 修正：遍历源文档的每一页，逐页导入
                for (page in sourceDoc.pages) {
                    outputDoc.importPage(page)
                }

                sourceDoc.close()
            }
            outputDoc.save(outputUri.toString())
            outputDoc.close()
            MergeResult(success = true, outputFileUri = outputUri)
        } catch (e: Exception) {
            MergeResult(success = false, errorMessage = e.message)
        }
    }
}
`

### 7.2 MergeViewModel

`kotlin
// feature-merge/src/main/kotlin/com/pdfadder/feature/merge/vm/MergeViewModel.kt

class MergeViewModel : ViewModel() {

    private val _pdfFiles = MutableLiveData<MutableList<PdfFile>>(mutableListOf())
    val pdfFiles: LiveData<MutableList<PdfFile>> = _pdfFiles

    private val _mergeResult = MutableLiveData<MergeResult>()
    val mergeResult: LiveData<MergeResult> = _mergeResult

    fun addFiles(uris: List<Uri>) {
        val newFiles = uris.mapIndexed { index, uri ->
            PdfFile(
                uri = uri,
                displayName = uri.getFileName(),
                fileSize = 0L,
                pageCount = 0,
                sortOrder = _pdfFiles.value!!.size + index
            )
        }
        _pdfFiles.value!!.addAll(newFiles)
    }

    fun repositionFiles(newOrder: List<PdfFile>) {
        _pdfFiles.value = newOrder.toMutableList()
    }

    fun merge(context: Context) {
        val files = _pdfFiles.value ?: return
        if (files.isEmpty()) return

        val outputUri = Uri.parse(
            context.filesDir.path + \"/merged_.pdf\"
        )

        viewModelScope.launch(Dispatchers.IO) {
            val result = PdfMerger().merge(files, outputUri, context)
            _mergeResult.postValue(result)
        }
    }

    val hasFiles: Boolean
        get() = _pdfFiles.value?.isNotEmpty() == true
}
`

### 7.3 MergeAdapter + ItemTouchHelper

`kotlin
// feature-merge/src/main/kotlin/com/pdfadder/feature/merge/ui/MergeAdapter.kt

class MergeAdapter(
    private val onItemLongPress: () -> Unit
) : ListAdapter<PdfFile, MergeAdapter.FileViewHolder>(DIFF_CALLBACK) {

    // 拖拽回调
    val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            source: FileViewHolder,
            target: FileViewHolder
        ): Boolean {
            val fromPos = source.adapterPosition
            val toPos = target.adapterPosition
            Collections.swap(currentList, fromPos, toPos)
            notifyItemMoved(fromPos, toPos)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

        override fun onSelectedChanged(
            viewHolder: RecyclerView.ViewHolder?,
            actionState: Int
        ) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                viewHolder?.itemView?.alpha = 0.6f
            }
        }

        override fun clearView(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ) {
            viewHolder.itemView.alpha = 1.0f
        }
    })

    // ... ViewHolder 实现省略
}
`

### 7.4 MergeRepository

`kotlin
// feature-merge/src/main/kotlin/com/pdfadder/feature/merge/repo/MergeRepository.kt

class MergeRepository {

    suspend fun getPdfInfo(uri: Uri, context: Context): PdfInfo {
        return withContext(Dispatchers.IO) {
            val stream = context.contentResolver.openInputStream(uri)!!
            val doc = PDDocument.load(stream)
            PdfInfo(
                pageCount = doc.numberOfPages,
                fileSize = doc.bytes.size.toLong()
            ).also { doc.close() }
        }
    }
}
`

---

## 8. 权限与存储

| 权限 | 用途 | 最低版本 |
|---|---|---|
| READ_EXTERNAL_STORAGE | 读取用户选择的 PDF | API 24-32 |
| WRITE_EXTERNAL_STORAGE | 保存合并结果 | API 24-32 |
| MANAGE_EXTERNAL_STORAGE | 可选：全文件管理 | API 30+ |

**兼容策略：**
- API 24-28：传统存储权限
- API 29：使用 MediaStore API
- API 30+：使用 Intent.ACTION_OPEN_DOCUMENT + 	akePersistableUriPermission
- 统一使用 ActivityResultContracts.OpenMultipleDocuments()

---

## 9. 依赖版本（建议）

`kotlin
// build.gradle.kts (顶层)
val androidGradle by extra(\"8.2.0\")
val kotlin by extra(\"1.9.22\")
val pdfbox by extra(\"3.0.0\")
val androidxCore by extra(\"1.12.0\")
val lifecycle by extra(\"2.7.0\")
val recyclerview by extra(\"1.3.2\")
`

`kotlin
// feature-merge/build.gradle.kts
dependencies {
    implementation(project(\":core-api\"))
    implementation(\"org.apache.pdfbox:pdfbox:\")
    implementation(\"androidx.recyclerview:recyclerview:\")
    implementation(\"androidx.lifecycle:lifecycle-viewmodel-ktx:\")
    implementation(\"androidx.lifecycle:lifecycle-livedata-ktx:\")
}
`

---

## 10. 后续扩展预留设计

`
新增功能模块的步骤：
┌────────────────────────────────────────┐
│ 1. 新建 Gradle 模块 feature-xxx        │
│ 2. 实现 PdfFeatureSpi 接口             │
│ 3. 在 PdfAdderApp.registerAll() 注册   │
│ 4. ✅ 完成，无需修改任何已有代码         │
└────────────────────────────────────────┘

预留扩展模块：
├── feature-word       → PDF 转 Word
├── feature-image      → PDF 转图片
├── feature-rotate     → 页面旋转
└── feature-delete     → 页面删除
`

---

## 11. 测试计划

| 测试类型 | 覆盖范围 |
|---|---|
| **单元测试** | PdfMerger.merge() — 合并逻辑、空列表、单文件、大文件 |
| **单元测试** | MergeViewModel — 添加文件、重排序、合并触发 |
| **UI 测试** | 文件选择 → 拖拽排序 → 合并 → 结果回调全流程 |
| **兼容性** | Android 7.0 真机/模拟器 — 权限、存储、PdfBox 加载 |
| **边界测试** | 0 文件、100+ 文件、超大 PDF(500MB+)、加密 PDF |

---

## 12. 关键假设与默认值

| 假设 | 说明 |
|---|---|
| 输出文件名 | merged_时间戳.pdf |
| 输出位置 | 应用私有目录/Files/（无需外部存储权限也可工作） |
| 拖拽方式 | 长按拖拽（中老年用户友好） |
| 文件选择 | 系统文件选择器，多选 PDF |
| 进度反馈 | 合并过程中显示 ProgressBar + 提示文字 |
| 错误提示 | Toast 简短提示 + 弹窗详细原因 |
| 无 DI 框架 | 初期不用 Hilt/Dagger，用手动注册保持简单 |
