package vn.npstudio.deviceinfo.domain.repository

import kotlinx.coroutines.flow.StateFlow
import vn.npstudio.deviceinfo.domain.model.ScreenTestResult

/** Last screen test result in this process; null until a test is saved. */
interface ScreenTestResultRepository {
    val result: StateFlow<ScreenTestResult?>

    fun save(result: ScreenTestResult)
}
