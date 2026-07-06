package org.owntracks.android.lifelog

import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber

/** 지정 WiFi 한 곳 = SSID + 그 위치의 고정 좌표 + 표시 이름. */
data class KnownWifi(
    val ssid: String,
    val lat: Double,
    val lon: Double,
    val label: String = "",
)

/**
 * Lifelog 설정 파서. 지정 WiFi 목록은 preference(JSON 문자열)로 저장되며,
 * 앱 내 설정 편집기(Editor)에서 리빌드 없이 수정할 수 있다.
 *
 * JSON 형식:
 * [{"ssid":"knights5G","lat":35.86407,"lon":128.54661,"label":"집"}, ...]
 */
object LifelogConfig {
  /** preference가 비었거나 파싱 실패 시 폴백. */
  val defaults =
      listOf(
          KnownWifi("knightHome5G", 35.863823, 128.546645, "엑소텍 대구지사"),
          KnownWifi("knights5G", 35.86407, 128.54661, "평리동 본가"),
          KnownWifi("KT_GiGA_5G_48F5", 35.89432, 128.56335, "엑소텍 대구지사"),
      )

  fun parse(jsonStr: String?): List<KnownWifi> {
    if (jsonStr.isNullOrBlank()) return defaults
    return try {
      val arr = JSONArray(jsonStr)
      (0 until arr.length())
          .map { i ->
            val o = arr.getJSONObject(i)
            KnownWifi(
                o.getString("ssid"),
                o.getDouble("lat"),
                o.getDouble("lon"),
                o.optString("label", ""))
          }
          .ifEmpty { defaults }
    } catch (e: Exception) {
      Timber.w(e, "LIFELOG: failed to parse knownWifis JSON, using defaults")
      defaults
    }
  }

  fun match(jsonStr: String?, ssid: String?): KnownWifi? =
      ssid?.let { s -> parse(jsonStr).firstOrNull { it.ssid == s } }

  fun toJson(list: List<KnownWifi>): String {
    val arr = JSONArray()
    list.forEach { w ->
      arr.put(
          JSONObject()
              .put("ssid", w.ssid)
              .put("lat", w.lat)
              .put("lon", w.lon)
              .put("label", w.label))
    }
    return arr.toString()
  }
}
