package com.morningbrief.app.repository

import com.morningbrief.app.model.MetroShift
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.*

data class MrtStation(
    val name: String,
    val nameEn: String,
    val code: String,
    val line: String,
    val travelTimeFromO19: Int,
    val latitude: Double,
    val longitude: Double
)

class MetroRepository {

    val allStations = listOf(
        // Orange Line (中和新蘆線)
        MrtStation("蘆洲", "Luzhou", "O54", "中和新蘆線", 2, 25.0915, 121.4645),
        MrtStation("三民高中", "Sanmin Senior High School", "O53 / O19", "中和新蘆線", 0, 25.0858, 121.4728),
        MrtStation("徐匯中學", "Saint Ignatius High School", "O52", "中和新蘆線", 2, 25.0805, 121.4800),
        MrtStation("三和國中", "Sanhe Junior High School", "O51", "中和新蘆線", 4, 25.0760, 121.4870),
        MrtStation("三重國小", "Sanchong Elementary School", "O50", "中和新蘆線", 6, 25.0700, 121.4975),
        MrtStation("大橋頭", "Daqiaotou", "O12", "中和新蘆線", 8, 25.0630, 121.5130),
        MrtStation("民權西路", "Minquan West Road", "O11 / R13", "中和新蘆線 / 淡水信義線", 10, 25.0618, 121.5198),
        MrtStation("中山國小", "Zhongshan Elementary School", "O10", "中和新蘆線", 12, 25.0630, 121.5265),
        MrtStation("行天宮", "Xingtian Temple", "O09", "中和新蘆線", 14, 25.0598, 121.5332),
        MrtStation("松江南京", "Songjiang Nanjing", "O08 / G15", "中和新蘆線 / 松山新店線", 16, 25.0531, 121.5330),
        MrtStation("忠孝新生", "Zhongxiao Xinsheng", "O07 / BL14", "中和新蘆線 / 板南線", 18, 25.0425, 121.5330),
        MrtStation("東門", "Dongmen", "O06 / R07", "中和新蘆線 / 淡水信義線", 21, 25.0339, 121.5284),
        MrtStation("古亭", "Guting", "O05 / G09", "中和新蘆線 / 松山新店線", 24, 25.0270, 121.5228),
        MrtStation("頂溪", "Dingxi", "O04", "中和新蘆線", 27, 25.0135, 121.5152),
        MrtStation("永安市場", "Yongan Market", "O03", "中和新蘆線", 29, 25.0022, 121.5110),
        MrtStation("景安", "Jingan", "O02 / Y11", "中和新蘆線 / 環狀線", 31, 24.9938, 121.5050),
        MrtStation("南勢角", "Nanshijiao", "O01", "中和新蘆線", 33, 24.9898, 121.5165),
        MrtStation("台北橋", "Taipei Bridge", "O13", "中和新蘆線", 9, 25.0633, 121.5020),
        MrtStation("菜寮", "Cailiao", "O14", "中和新蘆線", 11, 25.0607, 121.4925),
        MrtStation("三重", "Sanchong", "O15 / A2", "中和新蘆線 / 機場線", 13, 25.0558, 121.4842),
        MrtStation("先嗇宮", "Xianse Temple", "O16", "中和新蘆線", 15, 25.0468, 121.4720),
        MrtStation("頭前庄", "Touqianzhuang", "O17 / Y18", "中和新蘆線 / 環狀線", 17, 25.0401, 121.4608),
        MrtStation("新莊", "Xinzhuang", "O18", "中和新蘆線", 19, 25.0362, 121.4532),
        MrtStation("輔大", "Fu Jen University", "O19", "中和新蘆線", 21, 25.0328, 121.4363),
        MrtStation("丹鳳", "Danfeng", "O20", "中和新蘆線", 23, 25.0292, 121.4233),
        MrtStation("迴龍", "Huilong", "O21", "中和新蘆線", 25, 25.0218, 121.4116),

        // Green Line (松山新店線)
        MrtStation("小南門", "Xiaonanmen", "G11", "松山新店線", 19, 25.0354, 121.5097),
        MrtStation("西門", "Ximen", "G12 / BL11", "松山新店線 / 板南線", 18, 25.0422, 121.5085),
        MrtStation("北門", "Beimen", "G13 / A1", "松山新店線 / 機場線", 16, 25.0494, 121.5105),
        MrtStation("中山", "Zhongshan", "G14 / R11", "松山新店線 / 淡水信義線", 13, 25.0530, 121.5204),
        MrtStation("松江南京", "Songjiang Nanjing", "G15 / O08", "松山新店線 / 中和新蘆線", 16, 25.0531, 121.5330),
        MrtStation("南京復興", "Nanjing Fuxing", "G16 / BR11", "松山新店線 / 文湖線", 19, 25.0523, 121.5440),
        MrtStation("台北小巨蛋", "Taipei Arena", "G17", "松山新店線", 22, 25.0518, 121.5497),
        MrtStation("南京三民", "Nanjing Sanmin", "G18", "松山新店線", 24, 25.0515, 121.5606),
        MrtStation("松山", "Songshan", "G19", "松山新店線", 26, 25.0501, 121.5779),
        MrtStation("中正紀念堂", "Chiang Kai-shek Memorial Hall", "G10 / R08", "松山新店線 / 淡水信義線", 20, 25.0326, 121.5185),
        MrtStation("古亭", "Guting", "G09 / O05", "松山新店線 / 中和新蘆線", 24, 25.0270, 121.5228),
        MrtStation("台電大樓", "Taipower Building", "G08", "松山新店線", 26, 25.0199, 121.5284),
        MrtStation("公館", "Gongguan", "G07", "松山新店線", 28, 25.0137, 121.5340),
        MrtStation("萬隆", "Wanlong", "G06", "松山新店線", 31, 25.0019, 121.5385),
        MrtStation("景美", "Jingmei", "G05", "松山新店線", 33, 24.9934, 121.5408),
        MrtStation("大坪林", "Dapinglin", "G04 / Y07", "松山新店線 / 環狀線", 36, 24.9829, 121.5414),
        MrtStation("七張", "Qizhang", "G03", "松山新店線", 38, 24.9751, 121.5427),
        MrtStation("新店區公所", "Xindian District Office", "G02", "松山新店線", 40, 24.9673, 121.5417),
        MrtStation("新店", "Xindian", "G01", "松山新店線", 43, 24.9578, 121.5375),
        MrtStation("小碧潭", "Xiaobitan", "G03A", "松山新店線", 41, 24.9723, 121.5303),

        // Red Line (淡水信義線)
        MrtStation("台北車站", "Taipei Main Station", "R10 / BL12", "淡水信義線 / 板南線", 15, 25.0478, 121.5170),
        MrtStation("台大醫院", "NTU Hospital", "R09", "淡水信義線", 17, 25.0413, 121.5160),
        MrtStation("雙連", "Shuanglian", "R12", "淡水信義線", 12, 25.0578, 121.5207),
        MrtStation("圓山", "Yuanshan", "R14", "淡水信義線", 14, 25.0713, 121.5202),
        MrtStation("劍潭", "Jiantan", "R15", "淡水信義線", 16, 25.0848, 121.5250),
        MrtStation("士林", "Shilin", "R16", "淡水信義線", 18, 25.0934, 121.5262),
        MrtStation("芝山", "Zhishan", "R17", "淡水信義線", 20, 25.1028, 121.5226),
        MrtStation("明德", "Mingde", "R18", "淡水信義線", 22, 25.1098, 121.5188),
        MrtStation("石牌", "Shipai", "R19", "淡水信義線", 24, 25.1147, 121.5158),
        MrtStation("唭哩岸", "Qilian", "R20", "淡水信義線", 26, 25.1208, 121.5061),
        MrtStation("奇岩", "Qiyan", "R21", "淡水信義線", 28, 25.1256, 121.5011),
        MrtStation("北投", "Beitou", "R22", "淡水信義線", 30, 25.1319, 121.4986),
        MrtStation("新北投", "Xinbeitou", "R22A", "淡水信義線", 33, 25.1369, 121.5026),
        MrtStation("復興崗", "Fuxinggang", "R23", "淡水信義線", 32, 25.1374, 121.4854),
        MrtStation("忠義", "Zhongyi", "R24", "淡水信義線", 34, 25.1308, 121.4735),
        MrtStation("關渡", "Guandu", "R25", "淡水信義線", 36, 25.1256, 121.4671),
        MrtStation("竹圍", "Zhuwei", "R26", "淡水信義線", 39, 25.1368, 121.4593),
        MrtStation("紅樹林", "Hongshulin", "R27", "淡水信義線", 42, 25.1540, 121.4589),
        MrtStation("淡水", "Tamsui", "R28", "淡水信義線", 45, 25.1678, 121.4455),
        MrtStation("大安森林公園", "Daan Park", "R06", "淡水信義線", 22, 25.0333, 121.5349),
        MrtStation("大安", "Daan", "R05 / BR09", "淡水信義線 / 文湖線", 24, 25.0329, 121.5434),
        MrtStation("信義安和", "Xinyi Anhe", "R04", "淡水信義線", 25, 25.0332, 121.5529),
        MrtStation("台北101/世貿", "Taipei 101 / World Trade Center", "R03", "淡水信義線", 27, 25.0330, 121.5644),
        MrtStation("象山", "Xiangshan", "R02", "淡水信義線", 29, 25.0328, 121.5702),

        // Blue Line (板南線)
        MrtStation("善導寺", "Shandao Temple", "BL13", "板南線", 16, 25.0449, 121.5233),
        MrtStation("忠孝復興", "Zhongxiao Fuxing", "BL15 / BR10", "板南線 / 文湖線", 20, 25.0416, 121.5438),
        MrtStation("忠孝敦化", "Zhongxiao Dunhua", "BL16", "板南線", 22, 25.0416, 121.5505),
        MrtStation("國父紀念館", "Sun Yat-Sen Memorial Hall", "BL17", "板南線", 24, 25.0413, 121.5576),
        MrtStation("市政府", "Taipei City Hall", "BL18", "板南線", 26, 25.0411, 121.5651),
        MrtStation("永春", "Yongchun", "BL19", "板南線", 28, 25.0407, 121.5762),
        MrtStation("後山埤", "Houshanpi", "BL20", "板南線", 30, 25.0450, 121.5822),
        MrtStation("昆陽", "Kunyang", "BL21", "板南線", 32, 25.0504, 121.5932),
        MrtStation("南港", "Nangang", "BL22", "板南線", 34, 25.0531, 121.6070),
        MrtStation("南港展覽館", "Taipei Nangang Exhibition Center", "BL23 / BR24", "板南線 / 文湖線", 36, 25.0553, 121.6174),
        MrtStation("龍山寺", "Longshan Temple", "BL10", "板南線", 20, 25.0366, 121.4998),
        MrtStation("江子翠", "Jiangzicui", "BL09", "板南線", 22, 25.0305, 121.4727),
        MrtStation("新埔", "Xinpu", "BL08", "板南線", 24, 25.0229, 121.4682),
        MrtStation("板橋", "Banqiao", "BL07 / Y16", "板南線 / 環狀線", 27, 25.0135, 121.4637),
        MrtStation("府中", "Fuzhong", "BL06", "板南線", 29, 25.0084, 121.4593),
        MrtStation("亞東醫院", "Far Eastern Hospital", "BL05", "板南線", 32, 24.9982, 121.4526),
        MrtStation("海山", "Haishan", "BL04", "板南線", 35, 24.9854, 121.4488),
        MrtStation("土城", "Tucheng", "BL03", "板南線", 37, 24.9731, 121.4444),
        MrtStation("永寧", "Yongning", "BL02", "板南線", 39, 24.9669, 121.4361),
        MrtStation("頂埔", "Dingpu", "BL01", "板南線", 42, 24.9598, 121.4194),

        // Brown Line (文湖線)
        MrtStation("科技大樓", "Technology Building", "BR08", "文湖線", 26, 25.0261, 121.5434),
        MrtStation("六張犁", "Liuzhangli", "BR07", "文湖線", 28, 25.0238, 121.5531),
        MrtStation("麟光", "Linguang", "BR06", "文湖線", 30, 25.0185, 121.5587),
        MrtStation("辛亥", "Xinhai", "BR05", "文湖線", 33, 25.0055, 121.5570),
        MrtStation("萬芳醫院", "Wanfang Hospital", "BR04", "文湖線", 35, 24.9985, 121.5583),
        MrtStation("萬芳社區", "Wanfang Community", "BR03", "文湖線", 37, 24.9985, 121.5683),
        MrtStation("木柵", "Muzha", "BR02", "文湖線", 39, 24.9982, 121.5732),
        MrtStation("動物園", "Taipei Zoo", "BR01", "文湖線", 42, 24.9982, 121.5796),
        MrtStation("中山國中", "Zhongshan Junior High School", "BR12", "文湖線", 20, 25.0608, 121.5442),
        MrtStation("松山機場", "Songshan Airport", "BR13", "文湖線", 22, 25.0630, 121.5515),
        MrtStation("大直", "Dazhi", "BR14", "文湖線", 24, 25.0795, 121.5469),
        MrtStation("劍南路", "Jiannan Rd.", "BR15", "文湖線", 26, 25.0848, 121.5555),
        MrtStation("西湖", "Xihu", "BR16", "文湖線", 28, 25.0821, 121.5671),
        MrtStation("港墘", "Gangqian", "BR17", "文湖線", 30, 25.0800, 121.5750),
        MrtStation("文德", "Wende", "BR18", "文湖線", 32, 25.0784, 121.5847),
        MrtStation("內湖", "Neihu", "BR19", "文湖線", 34, 25.0836, 121.5944),
        MrtStation("大湖公園", "Dahu Park", "BR20", "文湖線", 36, 25.0837, 121.6023),
        MrtStation("葫洲", "Huzhou", "BR21", "文湖線", 38, 25.0734, 121.6074),
        MrtStation("東湖", "Donghu", "BR22", "文湖線", 40, 25.0672, 121.6115),
        MrtStation("南港軟體園區", "Nangang Software Park", "BR23", "文湖線", 42, 25.0599, 121.6160),

        // Circular Line (環狀線)
        MrtStation("新北產業園區", "New Taipei Industrial Park", "Y20 / A3", "環狀線 / 機場線", 16, 25.0615, 121.4598),
        MrtStation("幸福", "Xingfu", "Y19", "環狀線", 18, 25.0498, 121.4601),
        MrtStation("中和", "Zhonghe", "Y12", "環狀線", 34, 25.0003, 121.4947),
        MrtStation("秀朗橋", "Xiulang Bridge", "Y09", "環狀線", 38, 24.9926, 121.5298)
    )

    /**
     * Finds the nearest MRT station based on GPS coordinates or landmark keywords.
     */
    fun findNearestMetroStation(lat: Double?, lon: Double?, locationName: String): String {
        if (lat != null && lon != null) {
            var closestStation: MrtStation? = null
            var minDistanceKm = Double.MAX_VALUE

            for (station in allStations) {
                val d = calculateDistanceKm(lat, lon, station.latitude, station.longitude)
                if (d < minDistanceKm) {
                    minDistanceKm = d
                    closestStation = station
                }
            }

            if (closestStation != null) {
                val walkMins = maxOf(2, (minDistanceKm * 1000 / 80).toInt())
                return if (minDistanceKm <= 1.2) {
                    "${closestStation.name}站 (${closestStation.code}) • 步行約 ${walkMins} 分鐘"
                } else {
                    val distFormatted = String.format(Locale.ENGLISH, "%.1f", minDistanceKm)
                    "${closestStation.name}站 (${closestStation.code}) • 約 ${distFormatted} km"
                }
            }
        }

        // Keyword based fallback
        val lower = locationName.lowercase(Locale.ROOT)
        return when {
            lower.contains("101") || lower.contains("世貿") -> "台北101/世貿站 (R03) • 步行約 3 分鐘"
            lower.contains("忠孝敦化") || lower.contains("din tai fung") || lower.contains("鼎泰豐") -> "忠孝敦化站 (BL16) • 步行約 2 分鐘"
            lower.contains("內湖") || lower.contains("neihu") || lower.contains("園區") -> "港墘站 (BR17) • 步行約 5 分鐘"
            lower.contains("台北車站") || lower.contains("main station") -> "台北車站 (R10/BL12) • 步行約 3 分鐘"
            lower.contains("西門") || lower.contains("ximen") -> "西門站 (BL11/G12) • 步行約 4 分鐘"
            lower.contains("市府") || lower.contains("市政府") || lower.contains("信義") -> "市政府站 (BL18) • 步行約 4 分鐘"
            lower.contains("大安") -> "大安站 (R05/BR09) • 步行約 3 分鐘"
            lower.contains("東門") || lower.contains("永康") -> "東門站 (O06/R07) • 步行約 2 分鐘"
            lower.contains("松山") || lower.contains("饒河") -> "松山站 (G19) • 步行約 3 分鐘"
            lower.contains("三民") || lower.contains("蘆洲") -> "三民高中站 (O19) • 步行約 2 分鐘"
            else -> "捷運站步行範圍內 (Nearby Metro Station)"
        }
    }

    private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /**
     * Finds the closest MRT station based on GPS coordinates.
     */
    fun findClosestStation(lat: Double, lon: Double): MrtStation? {
        var closestStation: MrtStation? = null
        var minDistanceKm = Double.MAX_VALUE
        for (station in allStations) {
            val d = calculateDistanceKm(lat, lon, station.latitude, station.longitude)
            if (d < minDistanceKm) {
                minDistanceKm = d
                closestStation = station
            }
        }
        return closestStation
    }

    private fun computePlatformDirection(origin: MrtStation, dest: MrtStation?): String {
        if (dest == null) return "1號月台 (往 市中心方向)"
        if (origin.name == dest.name) return "已抵達該站"

        val isSameLine = origin.line == dest.line ||
            (origin.line.contains("中和新蘆") && dest.line.contains("中和新蘆")) ||
            (origin.line.contains("淡水信義") && dest.line.contains("淡水信義")) ||
            (origin.line.contains("松山新店") && dest.line.contains("松山新店")) ||
            (origin.line.contains("板南") && dest.line.contains("板南")) ||
            (origin.line.contains("文湖") && dest.line.contains("文湖"))

        if (isSameLine) {
            return "1號月台 (往 ${dest.name})"
        }

        // Transfer guidance
        val transferStation = when {
            origin.line.contains("中和新蘆") && dest.line.contains("淡水信義") -> "東門 / 民權西路"
            origin.line.contains("中和新蘆") && dest.line.contains("板南") -> "忠孝新生"
            origin.line.contains("中和新蘆") && dest.line.contains("松山新店") -> "松江南京 / 古亭"
            origin.line.contains("中和新蘆") && dest.line.contains("文湖") -> "忠孝新生 / 松江南京"
            origin.line.contains("板南") && dest.line.contains("淡水信義") -> "台北車站"
            origin.line.contains("板南") && dest.line.contains("文湖") -> "忠孝復興"
            origin.line.contains("淡水信義") && dest.line.contains("板南") -> "台北車站"
            origin.line.contains("淡水信義") && dest.line.contains("松山新店") -> "中山 / 中正紀念堂"
            else -> "主要轉乘站"
        }

        val mainlineDirection = when {
            origin.line.contains("中和新蘆") -> "往 南勢角"
            origin.line.contains("淡水信義") -> if (origin.latitude > dest.latitude) "往 象山" else "往 淡水"
            origin.line.contains("板南") -> if (origin.longitude < dest.longitude) "往 南港展覽館" else "往 頂埔"
            origin.line.contains("松山新店") -> if (origin.longitude < dest.longitude) "往 松山" else "往 新店"
            origin.line.contains("文湖") -> if (origin.latitude < dest.latitude) "往 南港展覽館" else "往 動物園"
            else -> "往 ${dest.name}"
        }

        return "1號月台 ($mainlineDirection • 於 $transferStation 轉乘)"
    }

    private val tdxRepository = com.morningbrief.app.repository.tdx.TdxRepository()

    /**
     * Station Info: Dynamic Origin -> Dynamic Destination
     * Priority: Fetches real official timetable from TDX API if Client ID/Secret configured.
     * Fallback: High-precision aligned timetable calculation if offline or unconfigured.
     */
    suspend fun getUpcomingShifts(
        origin: String = "三民高中",
        destination: String = "南勢角",
        count: Int = 4
    ): List<MetroShift> {
        val originClean = origin.removeSuffix("站").trim()
        val destClean = destination.removeSuffix("站").trim()

        val originStation = allStations.find { it.name == originClean || it.name.contains(originClean) || originClean.contains(it.name) }
            ?: allStations.first { it.name == "三民高中" }

        val destStation = allStations.find { it.name == destClean || it.name.contains(destClean) || destClean.contains(it.name) }

        val travelTimeMinutes = if (originStation.name == destStation?.name) {
            0
        } else if (originStation.name == "三民高中" && destStation != null) {
            destStation.travelTimeFromO19
        } else if (destStation != null) {
            val dist = calculateDistanceKm(originStation.latitude, originStation.longitude, destStation.latitude, destStation.longitude)
            val isTransfer = originStation.line != destStation.line
            val est = (dist * 2.2).toInt() + (if (isTransfer) 5 else 0)
            maxOf(3, est)
        } else {
            20
        }

        val lineColorHex = when {
            originStation.line.contains("松山新店") -> "#10B981"
            originStation.line.contains("淡水信義") -> "#EF4444"
            originStation.line.contains("板南") -> "#2563EB"
            originStation.line.contains("文湖") -> "#8B5CF6"
            originStation.line.contains("環狀") -> "#EAB308"
            else -> "#F8961E"
        }

        val platform = computePlatformDirection(originStation, destStation)

        // 1. Try real TDX API timetable if credentials configured
        try {
            val realShifts = tdxRepository.getRealUpcomingShifts(
                originStation = originStation,
                destStation = destStation,
                travelTimeMinutes = travelTimeMinutes,
                lineColorHex = lineColorHex,
                platformDesc = platform,
                count = count
            )
            if (!realShifts.isNullOrEmpty()) {
                return realShifts
            }
        } catch (e: Exception) {
            android.util.Log.e("MetroRepository", "TDX fetch fallback: ${e.message}")
        }

        // 2. Fallback to precise local timetable
        return computeLocalUpcomingShifts(originStation, destStation, travelTimeMinutes, lineColorHex, platform, count)
    }

    fun getLocalUpcomingShifts(
        origin: String = "三民高中",
        destination: String = "南勢角",
        count: Int = 4
    ): List<MetroShift> {
        val originClean = origin.removeSuffix("站").trim()
        val destClean = destination.removeSuffix("站").trim()

        val originStation = allStations.find { it.name == originClean || it.name.contains(originClean) || originClean.contains(it.name) }
            ?: allStations.first { it.name == "三民高中" }

        val destStation = allStations.find { it.name == destClean || it.name.contains(destClean) || destClean.contains(it.name) }

        val travelTimeMinutes = if (originStation.name == destStation?.name) {
            0
        } else if (originStation.name == "三民高中" && destStation != null) {
            destStation.travelTimeFromO19
        } else if (destStation != null) {
            val dist = calculateDistanceKm(originStation.latitude, originStation.longitude, destStation.latitude, destStation.longitude)
            val isTransfer = originStation.line != destStation.line
            val est = (dist * 2.2).toInt() + (if (isTransfer) 5 else 0)
            maxOf(3, est)
        } else {
            20
        }

        val lineColorHex = when {
            originStation.line.contains("松山新店") -> "#10B981"
            originStation.line.contains("淡水信義") -> "#EF4444"
            originStation.line.contains("板南") -> "#2563EB"
            originStation.line.contains("文湖") -> "#8B5CF6"
            originStation.line.contains("環狀") -> "#EAB308"
            else -> "#F8961E"
        }

        val platform = computePlatformDirection(originStation, destStation)
        return computeLocalUpcomingShifts(originStation, destStation, travelTimeMinutes, lineColorHex, platform, count)
    }

    private fun computeLocalUpcomingShifts(
        originStation: MrtStation,
        destStation: MrtStation?,
        travelTimeMinutes: Int,
        lineColorHex: String,
        platform: String,
        count: Int
    ): List<MetroShift> {
        val nowMillis = System.currentTimeMillis()
        val nowCal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val currentHour = nowCal.get(Calendar.HOUR_OF_DAY)
        val currentMinute = nowCal.get(Calendar.MINUTE)
        val currentSecond = nowCal.get(Calendar.SECOND)

        val isPeakHour = (currentHour in 7..9) || (currentHour in 17..19)
        val headwayMinutes = when {
            isPeakHour -> 4
            currentHour == 23 -> 8
            else -> 6
        }

        // Real station timing offset (e.g. Sanmin Senior High School first train is 06:02, offset = 2)
        val stationOffset = (originStation.travelTimeFromO19 + 2) % headwayMinutes

        // Taipei Metro Operating hours: 06:00 ~ 24:00 (with last train around 00:03)
        val isOperating = (currentHour in 6..23) || (currentHour == 0 && currentMinute <= stationOffset + 1)

        val destClean = destStation?.name?.removeSuffix("站")?.trim() ?: "南勢角"
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val shifts = mutableListOf<MetroShift>()

        // Anchor seconds and milliseconds to 0 for exact schedule slot alignment
        val baseCal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (!isOperating) {
            // First morning train at 06:00 + stationOffset (e.g. 06:02 for Sanmin High School)
            val morningCal = (baseCal.clone() as Calendar).apply {
                if (currentHour >= 6) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
                set(Calendar.HOUR_OF_DAY, 6)
                set(Calendar.MINUTE, stationOffset)
            }

            for (i in 0 until count) {
                val departureMillis = morningCal.timeInMillis
                val departureStr = timeFormat.format(Date(departureMillis))
                val diffMillis = departureMillis - nowMillis
                val minsUntil = maxOf(0, ((diffMillis + 30_000L) / 60_000L).toInt())

                val etaCal = (morningCal.clone() as Calendar).apply {
                    add(Calendar.MINUTE, travelTimeMinutes)
                }
                val etaStr = timeFormat.format(Date(etaCal.timeInMillis))
                val destStationName = if (destClean.endsWith("站")) destClean else "${destClean}站"

                shifts.add(
                    MetroShift(
                        shiftId = "${originStation.code}_${destClean}_${departureStr}_$i",
                        stationName = "${originStation.name}站",
                        stationCode = originStation.code,
                        lineName = originStation.line,
                        lineColorHex = lineColorHex,
                        destination = destClean,
                        departureTimeFormatted = departureStr,
                        departureEpochMillis = departureMillis,
                        minutesUntilDeparture = minsUntil,
                        platform = platform,
                        destinationStationName = destStationName,
                        etaTimeFormatted = etaStr,
                        travelTimeMinutes = travelTimeMinutes,
                        etaToDestinationFormatted = "$destStationName ETA $etaStr 約 $travelTimeMinutes 分鐘",
                        headwayFromPreviousMinutes = headwayMinutes,
                        isOperating = false
                    )
                )
                morningCal.add(Calendar.MINUTE, headwayMinutes)
            }
            return shifts
        }

        // Active Operating hours with station offset
        val baseMinute = currentMinute - stationOffset
        val slotMinute = ((baseMinute + headwayMinutes * 60) / headwayMinutes) * headwayMinutes + stationOffset
        val elapsedSecondsSinceSlot = (currentMinute - slotMinute) * 60 + currentSecond

        // If current train just arrived (within boarding window < 40 seconds), it is still boarding at platform
        val minutesToFirstShift = if (elapsedSecondsSinceSlot < 40) {
            slotMinute - currentMinute
        } else {
            (slotMinute + headwayMinutes) - currentMinute
        }

        val runningCal = (baseCal.clone() as Calendar).apply {
            add(Calendar.MINUTE, minutesToFirstShift)
        }

        for (i in 0 until count) {
            val departureMillis = runningCal.timeInMillis
            val departureStr = timeFormat.format(Date(departureMillis))

            val diffMillis = departureMillis - nowMillis
            val minsUntil = if (diffMillis <= 40_000L) {
                0
            } else {
                maxOf(1, ((diffMillis + 20_000L) / 60_000L).toInt())
            }

            val etaCal = (runningCal.clone() as Calendar).apply {
                add(Calendar.MINUTE, travelTimeMinutes)
            }
            val etaStr = timeFormat.format(Date(etaCal.timeInMillis))

            val destStationName = if (destClean.endsWith("站")) destClean else "${destClean}站"
            shifts.add(
                MetroShift(
                    shiftId = "${originStation.code}_${destClean}_${departureStr}_$i",
                    stationName = "${originStation.name}站",
                    stationCode = originStation.code,
                    lineName = originStation.line,
                    lineColorHex = lineColorHex,
                    destination = destClean,
                    departureTimeFormatted = departureStr,
                    departureEpochMillis = departureMillis,
                    minutesUntilDeparture = minsUntil,
                    platform = platform,
                    destinationStationName = destStationName,
                    etaTimeFormatted = etaStr,
                    travelTimeMinutes = travelTimeMinutes,
                    etaToDestinationFormatted = "$destStationName ETA $etaStr 約 $travelTimeMinutes 分鐘",
                    headwayFromPreviousMinutes = headwayMinutes,
                    isOperating = true
                )
            )

            runningCal.add(Calendar.MINUTE, headwayMinutes)
        }

        return shifts
    }

    suspend fun getUpcomingShiftsForDestination(
        destination: String = "南勢角",
        count: Int = 4
    ): List<MetroShift> {
        return getUpcomingShifts("三民高中", destination, count)
    }

    suspend fun getUpcomingShiftsForNanshijiao(count: Int = 4): List<MetroShift> {
        return getUpcomingShiftsForDestination("南勢角", count)
    }

    /**
     * Extracts canonical MRT station name from nearest Metro descriptive string.
     */
    fun extractStationName(nearestMetroText: String?): String? {
        if (nearestMetroText.isNullOrBlank()) return null
        val match = allStations.find { nearestMetroText.contains(it.name) }
        if (match != null) return match.name
        val cleaned = nearestMetroText.substringBefore("站").substringBefore("(").trim()
        return cleaned.ifBlank { null }
    }
}
