# Morning Brief 🌅 (晨間智慧通勤助理)

[![Android](https://img.shields.io/badge/Platform-Android-green.svg?logo=android)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg?logo=kotlin)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material%203-7C4DFF.svg)](https://m3.material.io/)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-35-orange.svg)](https://developer.android.com/)

**Morning Brief** 是一款專為通勤者打造的 Android 晨間智慧生活與捷運通勤助理。整合了 **Google 行事曆行程排程**、**即時 GPS 定位** 與 **交通部 TDX 台北捷運即時時刻表**，自動辨識行程地點最近的捷運站點，為你省去繁瑣查詢，一開機即可掌握今日行事曆與出門交通！

---

## ✨ 核心特色 (Key Features)

### 1. 🚇 台北捷運即時通勤看板 (Smart Taipei Metro Board & TDX Integration)
- **交通部 TDX 官方真實時刻表串接**：
  - 支援串接 TDX (Transport Data eXchange) 官方時刻表 API 與 OAuth2 憑證快取。
  - **自動雙軌容錯 (Smart Fallback)**：連線正常時顯示 TDX 即時時刻；若離線或未配置金鑰，無縫切換為高精準度站點時距演算法，並在時間後方自動附註 `(計算)` 提醒。
  - **夜間跨日運轉處理**：深夜收班時段能自動銜接翌日清晨 06:00 首班車時刻與剩餘倒數。
- **簡約列車方向顯示**：去除多餘月台號碼與複雜轉乘提示，純粹呈現列車運行方向（例如：`往 南勢角`、`往 象山`）。
- **智慧起點定位**：利用裝置 GPS 定位（支援精確與概略定位），預設起點為最接近手機當前位置的捷運站，並提供一鍵「重設為 GPS 位置」功能。
- **智慧終點預測**：依據當前時間，自動計算**下一個即將到來的行事曆行程**，並自動推算該目的地最近的捷運站點作為預設終點站。
- **即時列車倒數**：每 60 秒自動排程更新，在節省 API 使用量與掌握最新班次間取得最佳平衡。
- **彈性手動切換**：可隨時自選台北捷運各路線（中和新蘆線、淡水信義線、松山新店線、板南線、文湖線、環狀線等）之任一站點作為起訖站。

### 2. 📅 智慧行事曆與行程忽略管理 (Google Calendar Sync & Agenda Filtering)
- **當日行程自動彙整**：直接透過 Android Calendar Provider 讀取當天 Google Calendar 行程。
- **🚫 行程彈性忽略（不參加略過）**：
  - 活動不克出席時，可於行程卡片點擊「忽略」。
  - **捷運智慧連動**：被忽略的行程將自動排除於捷運目的地下載計算之外，**捷運會自動改用「下一個行程」的地點作為目的地**！
  - **已忽略行程收合與一鍵還原**：提供底部收合專區，可隨時展開並一鍵還原行程，狀態透過 `SharedPreferences` 持久化保存。

### 3. 🗺️ 視覺化地點卡片與精確導航 (Visual Location & Address Normalization)
- **台灣地址正規化引擎**：
  - 自動轉換阿拉伯數字段落（如 `1段` ➔ `一段`、`2段` ➔ `二段`）。
  - 自動剝除樓層、室號與備註資訊（如 `35樓 (Office)`、`會議室B`），大幅提高地圖辨識成功率。
- **智慧消歧義與門牌識別**：精確辨識包含「路／街／大道／段」與門牌「號」之確切地址，避免日常地址被誤判為模糊地點。
- **自訂行程地點覆寫**：可手動編輯與儲存個別行程的地點資訊，並透過 `SharedPreferences` 本地持久化保存。
- **一鍵 Google Maps 導航**：整合 Google 地圖精確導航 Intent（`google.navigation:q=...&mode=d`），一鍵即刻出發。
- **地點地圖預覽**：內建靜態地圖預覽卡片，地點環境一目了然。

### 4. 🎨 現代化 Material 3 設計與深色模式保存 (Modern UI & Theme Persistence)
- **深色模式偏好記憶**：深淺模式切換具備本地保存機制，重啟 App 自動恢復先前所選主題，亦能自動相容系統外觀。
- **Edge-to-Edge 全螢幕體驗**：完美適配 Android 15 現代化邊緣沉浸佈局。
- **精美卡片與微動畫**：清爽流暢的 Compose 現代元件，提升通勤資訊閱讀效率。

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
        TR[TdxRepository]
        SP[(SharedPreferences - Settings & Overrides)]
        CP[(Android Calendar Provider)]
        LM[(Android LocationManager)]
        TDX[(TDX Open API)]
    end

    UI_Layer <-->|Collects State / Triggers Events| VM
    VM -->|Exposes| State
    VM --> CR
    VM --> MR
    MR --> TR
    TR --> TDX
    CR --> CP
    CR --> LM
    VM --> SP
```

---

## 🛠️ 技術棧 (Tech Stack)

| 領域 | 技術 / 函式庫 | 說明 |
| :--- | :--- | :--- |
| **語言** | Kotlin 2.0+ (JVM 17) | 現代化強型別與協程支援 |
| **UI 框架** | Jetpack Compose (BOM 2024.09.00) | 宣告式 UI 與 Material 3 元件 |
| **設計規範** | Material 3 (`androidx.compose.material3`) | Material You 現代設計語言 |
| **狀態管理** | `ViewModel` + `StateFlow` | 生命週期感知與響應式資料流 |
| **非同步處理** | Kotlin Coroutines (`Dispatchers.IO`, `viewModelScope`) | 高效背景非同步作業 |
| **網路傳輸** | Retrofit 2 + Gson Converter | TDX 交通部 API 串接與 Token 管理 |
| **圖片載入** | Coil (`coil-compose`) | 輕量非同步地圖圖片渲染 |
| **單元測試** | JUnit 4, AndroidX Test | 班次計算、地址正規化與邏輯驗證 |

---

## 📁 專案結構 (Directory Structure)

```text
Android_App_Morning_Brief/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/morningbrief/app/
│   │   │   │   ├── model/
│   │   │   │   │   ├── CalendarEvent.kt        # 行事曆事件模型與模糊地點檢查
│   │   │   │   │   └── MetroShift.kt           # 捷運班次、離站時間與即時/推算狀態
│   │   │   │   ├── repository/
│   │   │   │   │   ├── tdx/                    # TDX 官方交通資料 API 模組
│   │   │   │   │   │   ├── TdxApiService.kt    # Retrofit API 介面定義
│   │   │   │   │   │   ├── TdxConfig.kt        # BuildConfig 憑證配置解析
│   │   │   │   │   │   ├── TdxModels.kt        # Token 與時刻表資料結構
│   │   │   │   │   │   └── TdxRepository.kt    # OAuth2 快取、TDX 時刻請求與深夜跨日
│   │   │   │   │   ├── CalendarRepository.kt   # 行事曆讀取、GPS 定位與地址消歧義
│   │   │   │   │   └── MetroRepository.kt      # 台北捷運站點資料庫、即時/演算法雙軌排程
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/
│   │   │   │   │   │   ├── CalendarAgendaCard.kt   # 當日行程列表與行程忽略/還原管理
│   │   │   │   │   │   ├── HeaderCard.kt           # 日期問候、深色模式切換與更新按鈕
│   │   │   │   │   │   ├── MetroDepartureCard.kt   # 捷運看板、方向提示與起訖切換
│   │   │   │   │   │   └── VisualLocationCard.kt   # 地點卡片、導航與地址對話框
│   │   │   │   │   ├── theme/                  # 主題配色、深淺配色表與字型排版
│   │   │   │   │   └── viewmodel/
│   │   │   │   │       └── MorningBriefViewModel.kt # 狀態管理、偏好持久化與導航切換
│   │   │   │   └── MainActivity.kt             # 應用程式進入點與權限動態請求
│   │   │   ├── res/                            # 圖示與字串資源
│   │   │   └── AndroidManifest.xml             # 應用程式宣告與權限配置
│   │   └── test/                               # 單元測試 (含地址正規化與班次驗證)
│   └── build.gradle.kts                        # App 模組建置腳本 (含 BuildConfig 注入)
├── gradle/                                     # Gradle Wrapper 與 Version Catalog
├── build.gradle.kts                            # 根專案建置腳本
├── settings.gradle.kts                         # 模組宣告與插件管理
├── local.properties                            # 本地配置 (受 .gitignore 保護，含 TDX 金鑰)
└── README.md                                   # 專案說明文件
```

---

## 🔒 金鑰與安全性說明 (Security & TDX Credentials)

本專案將 API 金鑰安全隔離在本地 [`local.properties`](local.properties) 中，**已由 `.gitignore` 完全忽略**，絕對不會被 commit 或推送到 GitHub 遠端儲存庫。

### TDX 金鑰設定方式
若需啟用 TDX 即時捷運時刻表功能，請在專案根目錄的 `local.properties` 檔案中加入：

```properties
tdx.clientId=YOUR_TDX_CLIENT_ID
tdx.clientSecret=YOUR_TDX_CLIENT_SECRET
```

> **提示**：若未填寫 TDX 金鑰，App 仍會**自動以離線精確演算法推算班次**並標註 `(計算)`，完全不影響核心功能使用！

---

## 🔒 權限說明 (Permissions)

本應用程式僅存取必要之裝置權限以提供通勤與行程功能，無任何後台隱私外洩行為：

- `android.permission.READ_CALENDAR`: 讀取使用者當日行事曆事件，以預測通勤目的地。
- `android.permission.ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`: 取得裝置當前位置，以自動挑選最近捷運起點站。
- `android.permission.INTERNET`: 載入地圖圖片與即時 TDX 捷運交通資訊。

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
