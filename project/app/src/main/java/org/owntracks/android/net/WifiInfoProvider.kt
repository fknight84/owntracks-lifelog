package org.owntracks.android.net

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import org.owntracks.android.model.messages.MessageStatus

@Singleton
class WifiInfoProvider @Inject constructor(@ApplicationContext context: Context) {
  @SuppressLint("WifiManagerPotentialLeak")
  private val wifiManager: WifiManager =
      context.getSystemService(Context.WIFI_SERVICE) as WifiManager

  private var ssid: String? = null
  private var bssid: String? = null

  init {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val connectivityManager: ConnectivityManager =
          context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

      connectivityManager.registerDefaultNetworkCallback(
          object : ConnectivityManager.NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
              if (networkCapabilities.transportInfo is WifiInfo) {
                ssid = (networkCapabilities.transportInfo as WifiInfo).getUnquotedSSID()
                bssid = (networkCapabilities.transportInfo as WifiInfo).bssid
              } else {
                ssid = null
                bssid = null
              }
              super.onCapabilitiesChanged(network, networkCapabilities)
            }
          })
    }
  }

  fun getBSSID(): String? =
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        @Suppress("DEPRECATION") wifiManager.connectionInfo.bssid
      } else {
        bssid
      }

  fun getSSID(): String? {
    // LIFELOG: 콜백 캐시(ssid)는 기본 네트워크가 모바일로 바뀌면 null로 뒤집힌다
    // (삼성폰은 WiFi+모바일 동시 활성이 흔함). 실제 WiFi 연결을 직접 읽어
    // (위치권한+위치서비스 필요) 안정적으로 SSID를 얻고, 캐시는 폴백으로 둔다.
    @Suppress("DEPRECATION")
    val direct = wifiManager.connectionInfo?.getUnquotedSSID()
    if (!direct.isNullOrBlank() && direct != WifiManager.UNKNOWN_SSID) return direct
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ssid else direct
  }

  fun isConnected(): Boolean = getBSSID() != null

  fun isWiFiEnabled(): Int =
      if (wifiManager.isWifiEnabled) {
        MessageStatus.STATUS_WIFI_ENABLED
      } else {
        MessageStatus.STATUS_WIFI_DISABLED
      }
}

fun WifiInfo.getUnquotedSSID(): String = this.ssid.replace(Regex("^\"(.*)\"$"), "$1")
