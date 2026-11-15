package vn.npstudio.deviceinfo.feature.screentest

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import vn.npstudio.deviceinfo.common.stateflow.LoadingMode
import vn.npstudio.deviceinfo.data.repository.ScreenTestResultRepositoryImpl
import vn.npstudio.deviceinfo.domain.model.ScreenDefect
import vn.npstudio.deviceinfo.domain.model.ScreenTestResult
import vn.npstudio.deviceinfo.testing.MainDispatcherRule

class ScreenTestResultViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun uiState_followsSavedResult() = runTest(mainDispatcherRule.dispatcher) {
        val repository = ScreenTestResultRepositoryImpl()
        val viewModel = ScreenTestResultViewModel(repository)

        viewModel.uiState.test {
            assertEquals(LoadingMode.INITIAL, awaitItem().loadingMode)
            assertEquals(ScreenTestRowMapper.map(null), awaitItem().rows)

            val result = ScreenTestResult(setOf(ScreenDefect.DEAD_OR_STUCK_PIXELS))
            repository.save(result)
            assertEquals(ScreenTestRowMapper.map(result), awaitItem().rows)
        }
    }
}
