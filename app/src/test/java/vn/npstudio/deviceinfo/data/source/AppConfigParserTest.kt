package vn.npstudio.deviceinfo.data.source

import org.junit.Assert.assertEquals
import org.junit.Test
import vn.npstudio.deviceinfo.domain.model.DarkMode
import vn.npstudio.deviceinfo.domain.model.TabId
import vn.npstudio.deviceinfo.domain.model.TabPreferences

class AppConfigParserTest {

    @Test
    fun enumOrDefault_matchesNameOrFallsBack() {
        assertEquals(DarkMode.ON, AppConfigParser.enumOrDefault("ON", DarkMode.SYSTEM))
        assertEquals(DarkMode.SYSTEM, AppConfigParser.enumOrDefault("BOGUS", DarkMode.SYSTEM))
        assertEquals(DarkMode.SYSTEM, AppConfigParser.enumOrDefault(null, DarkMode.SYSTEM))
    }

    @Test
    fun parseTabs_missingValues_returnsDefault() {
        assertEquals(TabPreferences(), AppConfigParser.parseTabs(order = null, hidden = null))
    }

    @Test
    fun parseTabs_dropsUnknownAndDuplicates_appendsMissingAsVisible() {
        val tabs = AppConfigParser.parseTabs(
            order = "MEMORY,UNKNOWN,CPU,MEMORY",
            hidden = "CPU,DISPLAY,UNKNOWN",
        )

        assertEquals(
            listOf(
                TabId.MEMORY,
                TabId.CPU,
                TabId.DEVICE,
                TabId.SYSTEM,
                TabId.BATTERY,
                TabId.DISPLAY,
                TabId.SCREEN_TEST,
            ),
            tabs.order,
        )
        assertEquals(setOf(TabId.CPU), tabs.hidden)
    }

    @Test
    fun normalize_allHidden_showsAll() {
        val tabs = AppConfigParser.normalize(TabPreferences(hidden = TabId.entries.toSet()))

        assertEquals(emptySet<TabId>(), tabs.hidden)
    }

    @Test
    fun joinTabIds_roundTrips() {
        val order = listOf(
            TabId.SCREEN_TEST,
            TabId.DEVICE,
            TabId.CPU,
            TabId.BATTERY,
            TabId.DISPLAY,
            TabId.MEMORY,
            TabId.SYSTEM,
        )
        val joined = AppConfigParser.joinTabIds(order)

        assertEquals("SCREEN_TEST,DEVICE,CPU,BATTERY,DISPLAY,MEMORY,SYSTEM", joined)
        assertEquals(order, AppConfigParser.parseTabs(joined, hidden = "").order)
    }
}
