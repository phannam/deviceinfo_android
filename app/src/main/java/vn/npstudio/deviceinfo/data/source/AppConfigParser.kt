package vn.npstudio.deviceinfo.data.source

import vn.npstudio.deviceinfo.domain.model.TabId
import vn.npstudio.deviceinfo.domain.model.TabPreferences

/** Pure parsing of stored config strings; tolerant of unknown or missing values. */
object AppConfigParser {

    private const val SEPARATOR = ","

    inline fun <reified T : Enum<T>> enumOrDefault(value: String?, default: T): T =
        enumValues<T>().firstOrNull { it.name == value } ?: default

    fun parseTabs(order: String?, hidden: String?): TabPreferences =
        normalize(TabPreferences(order = parseTabIds(order), hidden = parseTabIds(hidden).toSet()))

    /** Drops duplicates, appends missing tabs as visible, and shows all tabs if every tab is hidden. */
    fun normalize(tabs: TabPreferences): TabPreferences {
        val known = tabs.order.distinct()
        val missing = TabId.entries.filterNot { it in known }
        val order = known + missing
        val hidden = tabs.hidden - missing.toSet()
        return TabPreferences(order = order, hidden = if (hidden.containsAll(order)) emptySet() else hidden)
    }

    fun joinTabIds(ids: Collection<TabId>): String = ids.joinToString(SEPARATOR) { it.name }

    private fun parseTabIds(value: String?): List<TabId> =
        value.orEmpty().split(SEPARATOR).mapNotNull { name -> TabId.entries.firstOrNull { it.name == name.trim() } }
}
