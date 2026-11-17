package vn.npstudio.deviceinfo.feature.system

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.text.UiText
import vn.npstudio.deviceinfo.testing.byLabel
import vn.npstudio.deviceinfo.testing.sampleSystemInfo
import vn.npstudio.deviceinfo.ui.list.RowAction
import vn.npstudio.deviceinfo.ui.list.reportRows
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

class SystemRowMapperTest {

    private val now = Instant.parse("2024-01-01T00:00:00Z")

    private fun rows(info: vn.npstudio.deviceinfo.domain.model.SystemInfo = sampleSystemInfo) =
        SystemRowMapper.sections(info, 3_723_000L, Locale.US, ZoneId.of("UTC"), now).reportRows()

    @Test
    fun hero_showsApiCodenameAndReleaseDate() {
        val hero = SystemRowMapper.hero(sampleSystemInfo.build, Locale.US)

        assertEquals("28", hero.badge)
        assertEquals(UiText.res(R.string.value_android_version, "9"), hero.title)
        assertEquals(UiText.Dynamic("Pie"), hero.subtitle)
        assertEquals(UiText.res(R.string.value_release_date, "Aug 6, 2018"), hero.detail)
    }

    @Test
    fun hero_unknownApiHasNoCodename() {
        val hero = SystemRowMapper.hero(sampleSystemInfo.build.copy(apiLevel = 99, release = null), Locale.US)

        assertEquals(UiText.res(R.string.value_android_version, "99"), hero.title)
        assertNull(hero.subtitle)
        assertNull(hero.detail)
    }

    @Test
    fun sections_formatsSystemRows() {
        val values = rows().byLabel()

        assertEquals(UiText.Dynamic("Mar 1, 2021"), values[UiText.res(R.string.label_system_security_patch)])
        assertEquals(UiText.res(R.string.value_selinux_enforcing), values[UiText.res(R.string.label_system_selinux)])
        assertEquals(
            UiText.res(R.string.value_treble_vndk, UiText.res(R.string.common_supported), "28"),
            values[UiText.res(R.string.label_system_treble)],
        )
        assertEquals(
            UiText.res(R.string.value_duration_dhms, 0L, 1L, 2L, 3L),
            values[UiText.res(R.string.label_system_uptime)],
        )
        assertEquals(UiText.Dynamic("Vietnamese (Vietnam)"), values[UiText.res(R.string.label_system_language)])
        assertEquals(
            UiText.res(R.string.value_widevine_not_supported),
            values[UiText.res(R.string.label_drm_widevine)],
        )
    }

    @Test
    fun sections_systemFeaturesOpenList() {
        val row = rows().first { it.label == UiText.res(R.string.label_system_features) }

        assertEquals(UiText.Dynamic("2"), row.value)
        assertEquals(
            listOf("android.hardware.nfc", "android.software.webview"),
            (row.action as RowAction.ShowList).entries.map { it.title },
        )
    }

    @Test
    fun timeZone_includesOffset() {
        assertEquals(
            UiText.Dynamic("Indochina Time (GMT+07:00)"),
            SystemRowMapper.timeZone("Asia/Ho_Chi_Minh", Locale.US, now),
        )
        assertEquals(UiText.Dynamic("Not/AZone"), SystemRowMapper.timeZone("Not/AZone", Locale.US, now))
    }
}
