package vn.npstudio.deviceinfo.domain.model

/** Problems the user can report after the screen test. */
enum class ScreenDefect {
    DEAD_OR_STUCK_PIXELS,
    LINES_OR_BANDING,
    BURN_IN_OR_DISCOLORATION,
}

/** Answers from the last screen test; [defects] holds every problem the user reported. */
data class ScreenTestResult(val defects: Set<ScreenDefect>)
