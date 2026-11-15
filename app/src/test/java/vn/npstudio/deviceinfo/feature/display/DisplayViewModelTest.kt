package vn.npstudio.deviceinfo.feature.display

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import vn.npstudio.deviceinfo.common.stateflow.LoadingMode
import vn.npstudio.deviceinfo.testing.FakeDisplayRepository
import vn.npstudio.deviceinfo.testing.MainDispatcherRule
import vn.npstudio.deviceinfo.testing.sampleDisplayInfo
import vn.npstudio.deviceinfo.testing.sampleWindowSnapshot

class DisplayViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun uiState_waitsForWindowThenShowsRows() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = DisplayViewModel(FakeDisplayRepository())

        viewModel.uiState.test {
            assertEquals(LoadingMode.INITIAL, awaitItem().loadingMode)

            viewModel.onWindowMeasured(sampleWindowSnapshot)
            val loaded = awaitItem()
            assertEquals(LoadingMode.IDLE, loaded.loadingMode)
            assertEquals(DisplayRowMapper.map(sampleDisplayInfo, sampleWindowSnapshot), loaded.rows)
        }
    }

    @Test
    fun onWindowMeasured_reloadsDisplayValuesEvenForEqualSnapshot() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeDisplayRepository()
        val viewModel = DisplayViewModel(repository)

        viewModel.uiState.test {
            awaitItem()
            viewModel.onWindowMeasured(sampleWindowSnapshot)
            awaitItem()

            repository.info = sampleDisplayInfo.copy(fontScale = 1.3f)
            viewModel.onWindowMeasured(sampleWindowSnapshot)

            assertEquals(
                DisplayRowMapper.map(repository.info, sampleWindowSnapshot),
                awaitItem().rows,
            )
            assertEquals(2, repository.loadCount)
        }
    }
}
