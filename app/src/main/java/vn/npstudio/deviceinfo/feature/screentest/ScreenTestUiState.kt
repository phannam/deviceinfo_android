package vn.npstudio.deviceinfo.feature.screentest

import vn.npstudio.deviceinfo.domain.model.ScreenDefect

data class ScreenTestUiState(
    val pattern: ScreenTestPattern = ScreenTestPattern.entries.first(),
    val hintVisible: Boolean = true,
    /** True after the last pattern; the result dialog is shown. */
    val askingResult: Boolean = false,
    val defects: Set<ScreenDefect> = emptySet(),
) {
    val position: Int get() = pattern.ordinal + 1
    val total: Int get() = ScreenTestPattern.entries.size
}

sealed interface ScreenTestUiEffect {
    data object Finish : ScreenTestUiEffect
}
