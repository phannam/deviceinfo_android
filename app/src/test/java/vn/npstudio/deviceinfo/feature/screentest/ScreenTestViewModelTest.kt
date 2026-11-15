package vn.npstudio.deviceinfo.feature.screentest

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import vn.npstudio.deviceinfo.data.repository.ScreenTestResultRepositoryImpl
import vn.npstudio.deviceinfo.domain.model.ScreenDefect
import vn.npstudio.deviceinfo.domain.model.ScreenTestResult
import vn.npstudio.deviceinfo.testing.MainDispatcherRule

class ScreenTestViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = ScreenTestResultRepositoryImpl()

    private fun ScreenTestViewModel.tapThroughAllPatterns() = repeat(ScreenTestPattern.entries.size) { onTap() }

    @Test
    fun onTap_walksPatternsInOrderThenAsksForResult() {
        val viewModel = ScreenTestViewModel(repository)
        val seen = mutableListOf(viewModel.uiState.value.pattern)

        repeat(ScreenTestPattern.entries.size - 1) {
            viewModel.onTap()
            seen += viewModel.uiState.value.pattern
        }
        assertEquals(ScreenTestPattern.entries, seen)
        assertEquals(9, viewModel.uiState.value.position)
        assertFalse(viewModel.uiState.value.askingResult)

        viewModel.onTap()
        assertTrue(viewModel.uiState.value.askingResult)
        assertEquals(ScreenTestPattern.CHECKERBOARD, viewModel.uiState.value.pattern)

        viewModel.onTap()
        assertTrue(viewModel.uiState.value.askingResult)
    }

    @Test
    fun hint_hidesAfterTwoSecondsAndReappearsOnNextPattern() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = ScreenTestViewModel(repository)
        assertTrue(viewModel.uiState.value.hintVisible)

        testScheduler.advanceTimeBy(ScreenTestViewModel.HINT_DURATION_MILLIS - 1)
        testScheduler.runCurrent()
        assertTrue(viewModel.uiState.value.hintVisible)

        viewModel.onTap()
        testScheduler.advanceTimeBy(ScreenTestViewModel.HINT_DURATION_MILLIS - 1)
        testScheduler.runCurrent()
        assertTrue(viewModel.uiState.value.hintVisible)

        testScheduler.advanceTimeBy(2)
        testScheduler.runCurrent()
        assertFalse(viewModel.uiState.value.hintVisible)
    }

    @Test
    fun onSaveClicked_storesCheckedDefectsAndFinishes() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = ScreenTestViewModel(repository)
        viewModel.tapThroughAllPatterns()

        viewModel.effects.test {
            viewModel.onDefectChecked(ScreenDefect.DEAD_OR_STUCK_PIXELS, true)
            viewModel.onDefectChecked(ScreenDefect.LINES_OR_BANDING, true)
            viewModel.onDefectChecked(ScreenDefect.DEAD_OR_STUCK_PIXELS, false)
            viewModel.onSaveClicked()

            assertEquals(ScreenTestUiEffect.Finish, awaitItem())
        }
        assertEquals(ScreenTestResult(setOf(ScreenDefect.LINES_OR_BANDING)), repository.result.value)
    }

    @Test
    fun onDiscardClicked_finishesWithoutResult() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = ScreenTestViewModel(repository)
        viewModel.tapThroughAllPatterns()

        viewModel.effects.test {
            viewModel.onDefectChecked(ScreenDefect.BURN_IN_OR_DISCOLORATION, true)
            viewModel.onDiscardClicked()

            assertEquals(ScreenTestUiEffect.Finish, awaitItem())
        }
        assertNull(repository.result.value)
    }
}
