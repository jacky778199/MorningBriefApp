# Morning Brief 🌅 (晨間智慧通勤助理)

[![Android](https://img.shields.io/badge/Platform-Android-green.svg?logo=android)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg?logo=kotlin)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material%203-7C4DFF.svg)](https://m3.material.io/)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-35-orange.svg)](https://developer.android.com/)

**Morning Brief** 是一款專為通勤者打造的 Android 晨間智慧生活與捷運通勤助理。整合了 **Google 行事曆行程排程**、**即時 GPS 定位** 與 **台北捷運即時班次預報**，自動辨識行程地點最近的捷運站點，為你省去繁瑣查詢，一開機即可掌握今日行事曆與出門交通！

---

## ✨ 核心特色 (Key Features)

### 1. 📅 智慧行事曆整合 (Google Calendar Sync)
- **當日行程自動彙整**：直接透過 Android Calendar Provider 讀取當天 Google Calendar 行程。
- **無縫權限請求**：支援動態授權引導，並提供權限未開時的友善提示。

### 2. 🚇 台北捷運即時通勤看板 (Smart Taipei Metro Board)
- **智慧起點定位**：利用裝置 GPS 定位（支援精確與概略定位），預設起點為最接近手機當前位置的捷運站，並提供一鍵「重設為 GPS 位置」功能。
- **智慧終點預測**：依據當前時間，自動計算**下一個即將到來的行事曆行程**，並自動推算該目的地最近的捷運站點作為預設終點站。
- **即時列車倒數**：顯示接下來多個班次的發車倒數時間，並在背景每 30 秒自動排程更新。
- **彈性手動切換**：可隨時自選台北捷運各路線（中和新蘆線、淡水信義線、松山新店線、板南線、文湖線、環狀線等）之任一站點作為起訖站。

### 3. 🗺️ 視覺化地點卡片與精確導航 (Visual Location & Address Normalization)
- **台灣地址正規化引擎**：
  - 自動轉換阿拉伯數字段落（如 `1段` ➔ `一段`、`2段` ➔ `二段`）。
  - 自動剝除樓層、室號與備註資訊（如 `35樓 (Office)`、`會議室B`），大幅提高地圖辨識成功率。
- **地址歧義偵測與候選確認**：若地址包含多個可能地點，系統會彈出防呆警示並列出候選地址供使用者點擊選擇。
- **自訂行程地點覆寫**：可手動編輯與儲存個別行程的地點資訊，並透過 `SharedPreferences` 本地持久化保存。
- **一鍵 Google Maps 導航**：整合 Google 地圖精確導航 Intent（`google.navigation:q=...&mode=d`），一鍵即刻出發。
- **地點地圖預覽**：內建靜態地圖預覽卡片，地點環境一目了然。

### 4. 🎨 現代化 Material 3 設計與深色模式 (Modern Jetpack Compose UI)
- **Edge-to-Edge 全螢幕體驗**：適配 Android 15 現代化邊緣佈局。
- **深色 / 淺色主題自由切換**：頂部問候卡片支援即時切換深淺模式。
- **直覺手勢與下拉重新整理**：流暢滾動體驗與一鍵同步刷新。

---

## 🏗️ 系統架構 (Architecture)

本專案遵循 Google 推薦的 **Modern Android Architecture (MVVM)** 架構，確保職責分離與高可測試性：

```mermaid
flowchart TD
    subgraph UI_Layer [UI Layer (Jetpack Compose)]
        A[MainActivity] --> B[MorningBriefScreen]
        B --> C1[HeaderCard]
        B --> C2[MetroDepartureCard]
        B --> C3[CalendarAgendaCard]
        C3 --> C4[VisualLocationCard]
    end

    subgraph ViewModel_Layer [ViewModel Layer]
        VM[MorningBriefViewModel]
        State[MorningBriefUiState - StateFlow]
    end

    subgraph Data_Layer [Repository & Data Layer]
        CR[CalendarRepository]
        MR[MetroRepository]
        SP[(SharedPreferences Override)]
        CP[(Android Calendar Provider)]
        LM[Android LocationManager]
    end

    UI_Layer <-->|Collects State / Triggers Events| VM
    VM -->|Exposes| State
    VM --> CR
    VM --> MR
    CR --> CP
    CR --> LM
    VM --> SP
```

---

## 🛠️ 技術棧 (Tech Stack)

| 領域 | 技術 / 函式庫 | 說明 |
| :--- | :--- | :--- |
| **語言** | Kotlin 2.0+ (JVM 17) | 現代化強型別與協程支援 |
| **UI 框架** | Jetpack Compose (BOM) | 宣告式 UI |
| **設計規範** | Material 3 (`androidx.compose.material3`) | Material You 設計語言 |
| **狀態管理** | `ViewModel` + `StateFlow` | 生命週期感知與響應式資料流 |
| **非同步處理** | Kotlin Coroutines (`Dispatchers.IO`, `viewModelScope`) | 高效背景非同步作業 |
| **圖片載入** | Coil (`coil-compose`) | 輕量非同步地圖圖片渲染 |
| **網路傳輸** | Retrofit + OkHttp3 Logging Interceptor | REST API 串接與日誌調校 |
| **單元測試** | JUnit 4, Espresso, AndroidX Test | 地址正規化與邏輯單元測試 |

---

## 📁 專案結構 (Directory Structure)

```text
Android_App_Morning_Brief/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/morningbrief/app/
│   │   │   │   ├── model/
│   │   │   │   │   ├── CalendarEvent.kt        # 行事曆事件模型
│   │   │   │   │   └── MetroShift.kt           # 捷運班次與離站時間模型
│   │   │   │   ├── repository/
│   │   │   │   │   ├── CalendarRepository.kt   # 行事曆讀取、GPS定位與地址處理
│   │   │   │   │   └── MetroRepository.kt      # 台北捷運站點資料庫與班次演算法
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/
│   │   │   │   │   │   ├── CalendarAgendaCard.kt   # 當日行程列表元件
│   │   │   │   │   │   ├── HeaderCard.kt           # 日期問候與深色模式切換
│   │   │   │   │   │   ├── MetroDepartureCard.kt   # 捷運看板與站點切換
│   │   │   │   │   │   └── VisualLocationCard.kt   # 地點卡片、導航與地址對話框
│   │   │   │   │   ├── theme/                  # 主題配色、字型排版規範
│   │   │   │   │   └── viewmodel/
│   │   │   │   │       └── MorningBriefViewModel.kt # 核心業務邏輯與狀態流
│   │   │   │   └── MainActivity.kt             # 應用程式進入點與權限請求
│   │   │   ├── res/                            # 圖示與字串資源
│   │   │   └── AndroidManifest.xml             # 應用程式宣告與權限配置
│   │   └── test/                               # 單元測試 (含地址正規化驗證)
│   └── build.gradle.kts                        # App 模組建置腳本
├── gradle/                                     # Gradle Wrapper 與 Version Catalog
├── build.gradle.kts                            # 根專案建置腳本
├── settings.gradle.kts                         # 模組宣告與插件管理
└── README.md                                   # 專案說明文件
```

---

## 🔒 權限說明 (Permissions)

本應用程式僅存取必要之裝置權限以提供通勤與行程功能，無任何後台隱私上傳行為：

- `android.permission.READ_CALENDAR`: 讀取使用者當日行事曆事件，以預測通勤目的地。
- `android.permission.ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`: 取得裝置當前位置，以自動挑選最近捷運起點站。
- `android.permission.INTERNET`: 載入地圖圖片與即時交通資訊。

---

## 🚀 快速上手 (Getting Started)

### 環境需求
- **Android Studio**: Ladybug / Koala 或最新版本
- **JDK**: Java 17 或以上
- **Android SDK**: `compileSdk = 35`, `minSdk = 26`

### 建置步驟
1. **Clone 專案**:
   ```bash
   git clone git@github.com:jacky778199/MorningBriefApp.git
   cd MorningBriefApp
   ```
2. **使用 Android Studio 開啟專案**:
   - 等待 Gradle Sync 完成。
3. **執行測試**:
   ```bash
   ./gradlew test
   ```
4. **安裝至裝置或模擬器**:
   - 點擊 Android Studio 工具列的 **Run 'app'** (Shift + F10)。

---

## 📄 授權協議 (License)

本專案採用 [MIT License](LICENSE) 進行授權。歡迎自由 Fork、提出 Issue 或發送 Pull Request！
