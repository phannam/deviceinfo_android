package vn.npstudio.deviceinfo.feature.system

import java.time.LocalDate

/** Codename and first stable release date per API level, for the System tab hero card. */
object AndroidReleases {

    data class Release(val codename: String, val releaseDate: LocalDate)

    private val releases = mapOf(
        26 to Release("Oreo", LocalDate.of(2017, 8, 21)),
        27 to Release("Oreo", LocalDate.of(2017, 12, 5)),
        28 to Release("Pie", LocalDate.of(2018, 8, 6)),
        29 to Release("Quince Tart", LocalDate.of(2019, 9, 3)),
        30 to Release("Red Velvet Cake", LocalDate.of(2020, 9, 8)),
        31 to Release("Snow Cone", LocalDate.of(2021, 10, 4)),
        32 to Release("Snow Cone v2", LocalDate.of(2022, 3, 7)),
        33 to Release("Tiramisu", LocalDate.of(2022, 8, 15)),
        34 to Release("Upside Down Cake", LocalDate.of(2023, 10, 4)),
        35 to Release("Vanilla Ice Cream", LocalDate.of(2024, 9, 3)),
        36 to Release("Baklava", LocalDate.of(2025, 6, 10)),
    )

    /** Null for API levels this table does not know yet. */
    fun of(apiLevel: Int): Release? = releases[apiLevel]
}
