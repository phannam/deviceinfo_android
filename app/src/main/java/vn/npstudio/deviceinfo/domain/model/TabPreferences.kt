package vn.npstudio.deviceinfo.domain.model

/** Main screen tabs in their default order. */
enum class TabId { DEVICE, CPU, BATTERY, DISPLAY, MEMORY, SCREEN_TEST }

/** [order] contains every [TabId] exactly once; at least one tab is not [hidden]. */
data class TabPreferences(
    val order: List<TabId> = TabId.entries,
    val hidden: Set<TabId> = emptySet(),
) {
    val visible: List<TabId> get() = order.filterNot { it in hidden }
}
