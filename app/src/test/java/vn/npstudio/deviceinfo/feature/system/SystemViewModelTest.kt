package vn.npstudio.deviceinfo.feature.system

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.stateflow.LoadingMode
import vn.npstudio.deviceinfo.common.text.UiText
import vn.npstudio.deviceinfo.testing.FakeSystemInfoRepository
import vn.npstudio.deviceinfo.testing.MainDispatcherRule
import vn.npstudio.deviceinfo.testing.byLabel

class SystemViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun uiState_showsHeroAndTicksUptime() = runTest(mainDispatcherRule.dispatcher) {
        val uptime = MutableSharedFlow<Long>()
        val viewModel = SystemViewModel(FakeSystemInfoRepository(uptime = uptime))
        val label = UiText.res(R.string.label_system_uptime)

        viewModel.uiState.test {
            assertEquals(LoadingMode.INITIAL, awaitItem().loadingMode)
            uptime.emit(1_000L)
            val first = awaitItem()
            assertEquals(LoadingMode.IDLE, first.loadingMode)
            assertEquals("28", first.hero?.badge)
            assertEquals(UiText.res(R.string.value_duration_dhms, 0L, 0L, 0L, 1L), first.rows.byLabel()[label])

            uptime.emit(2_000L)
            assertEquals(UiText.res(R.string.value_duration_dhms, 0L, 0L, 0L, 2L), awaitItem().rows.byLabel()[label])
        }
    }
}
