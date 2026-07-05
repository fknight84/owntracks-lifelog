package org.owntracks.android.lifelog

/** 지정 WiFi 한 곳 = SSID + 그 위치의 고정 좌표 + 표시 이름. */
data class KnownWifi(
    val ssid: String,
    val lat: Double,
    val lon: Double,
    val label: String,
)

/**
 * Lifelog 설정. 지정 WiFi에 접속하면 GPS를 끄고, 아래 고정 좌표로
 * presenceIntervalMinutes 마다 "존재" 위치를 발행한다.
 *
 * 새 WiFi를 추가하려면 knownWifis 목록에 항목을 넣고 다시 빌드하면 된다.
 */
object LifelogConfig {
  val knownWifis =
      listOf(
          KnownWifi("knights5G", 35.86407, 128.54661, "집"),
          KnownWifi("knights", 35.86407, 128.54661, "집"),
          KnownWifi("KT_GiGA_5G_48F5", 35.89432, 128.56335, "직장"),
      )

  const val presenceIntervalMinutes: Long = 10L

  fun match(ssid: String?): KnownWifi? =
      ssid?.let { s -> knownWifis.firstOrNull { it.ssid == s } }
}
