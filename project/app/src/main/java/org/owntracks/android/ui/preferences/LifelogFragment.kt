package org.owntracks.android.ui.preferences

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import org.owntracks.android.R
import org.owntracks.android.lifelog.KnownWifi
import org.owntracks.android.lifelog.LifelogConfig
import org.owntracks.android.location.LocationProviderClient
import org.owntracks.android.net.WifiInfoProvider

@AndroidEntryPoint
class LifelogFragment : AbstractPreferenceFragment() {
  @Inject lateinit var wifiInfoProvider: WifiInfoProvider

  @Inject lateinit var locationProviderClient: LocationProviderClient

  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    super.onCreatePreferences(savedInstanceState, rootKey)
    setPreferencesFromResource(R.xml.preferences_lifelog, rootKey)
    findPreference<Preference>("lifelogAddCurrentWifi")?.setOnPreferenceClickListener {
      addCurrentWifi()
      true
    }
    updateListSummary()
  }

  private fun toast(msg: String) = Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()

  private fun addCurrentWifi() {
    val ssid = wifiInfoProvider.getSSID()
    if (ssid.isNullOrBlank()) {
      toast("WiFi에 연결돼 있지 않습니다")
      return
    }
    val loc = locationProviderClient.getLastLocation()
    if (loc == null) {
      toast("현재 위치를 가져올 수 없습니다 (잠시 후 다시 시도)")
      return
    }
    val input = EditText(requireContext()).apply { setText(ssid) }
    AlertDialog.Builder(requireContext())
        .setTitle("이 장소 이름")
        .setMessage(
            "$ssid\n(${String.format(Locale.US, "%.5f", loc.latitude)}, " +
                "${String.format(Locale.US, "%.5f", loc.longitude)})")
        .setView(input)
        .setPositiveButton("추가") { _, _ ->
          val label = input.text.toString().ifBlank { ssid }
          val list = LifelogConfig.parse(preferences.lifelogKnownWifis).toMutableList()
          list.removeAll { it.ssid == ssid }
          list.add(KnownWifi(ssid, loc.latitude, loc.longitude, label))
          preferences.lifelogKnownWifis = LifelogConfig.toJson(list)
          updateListSummary()
          toast("추가됨: $label ($ssid)")
        }
        .setNegativeButton("취소", null)
        .show()
  }

  private fun updateListSummary() {
    val list = LifelogConfig.parse(preferences.lifelogKnownWifis)
    findPreference<Preference>("lifelogKnownWifis")?.summary =
        if (list.isEmpty()) "없음"
        else list.joinToString("\n") { "• ${it.label.ifBlank { "(이름없음)" }} — ${it.ssid}" }
  }
}
