package vn.npstudio.deviceinfo.feature.tabmanager

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import vn.npstudio.deviceinfo.data.repository.AppConfigRepositoryImpl
import vn.npstudio.deviceinfo.domain.model.TabId
import vn.npstudio.deviceinfo.domain.model.TabPreferences
import vn.npstudio.deviceinfo.testing.FakeAppConfigLocalDataSource
import vn.npstudio.deviceinfo.testing.MainDispatcherRule

class TabManagerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = AppConfigRepositoryImpl(FakeAppConfigLocalDataSource())

    private fun TabManagerViewModel.items() = uiState.value.items

    @Test
    fun uiState_startsFromSavedTabs() {
        repository.saveTabs(TabPreferences(order = TabId.entries.reversed(), hidden = setOf(TabId.CPU)))

        val items = TabManagerViewModel(repository).items()

        assertEquals(TabId.entries.reversed(), items.map { it.id })
        assertFalse(items.single { it.id == TabId.CPU }.visible)
    }

    @Test
    fun onMoved_reordersDraft() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = TabManagerViewModel(repository)

        viewModel.uiState.test {
            awaitItem()
            viewModel.onMoved(0, 2)
            assertEquals(
                listOf(TabId.CPU, TabId.BATTERY, TabId.DEVICE, TabId.DISPLAY, TabId.MEMORY, TabId.SCREEN_TEST),
                awaitItem().items.map { it.id },
            )
        }
    }

    @Test
    fun onMoved_ignoresOutOfRangePositions() {
        val viewModel = TabManagerViewModel(repository)

        viewModel.onMoved(0, TabId.entries.size)
        viewModel.onSave()

        assertEquals(TabPreferences(), repository.tabs.value)
    }

    @Test
    fun onToggled_hidesAndShowsTab() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = TabManagerViewModel(repository)

        viewModel.uiState.test {
            awaitItem()
            viewModel.onToggled(TabId.BATTERY, false)
            assertFalse(awaitItem().items.single { it.id == TabId.BATTERY }.visible)
            viewModel.onToggled(TabId.BATTERY, true)
            assertTrue(awaitItem().items.single { it.id == TabId.BATTERY }.visible)
        }
    }

    @Test
    fun lastVisibleTab_cannotBeHidden() {
        repository.saveTabs(TabPreferences(hidden = TabId.entries.toSet() - TabId.MEMORY))
        val viewModel = TabManagerViewModel(repository)

        val memory = viewModel.items().single { it.id == TabId.MEMORY }
        assertFalse(memory.toggleEnabled)
        assertTrue(viewModel.items().filter { it.id != TabId.MEMORY }.all { it.toggleEnabled })

        viewModel.onToggled(TabId.MEMORY, false)
        viewModel.onSave()

        assertEquals(TabId.entries.toSet() - TabId.MEMORY, repository.tabs.value.hidden)
    }

    @Test
    fun onReset_restoresDefaultsInDraftOnly() = runTest(mainDispatcherRule.dispatcher) {
        val saved = TabPreferences(order = TabId.entries.reversed(), hidden = setOf(TabId.CPU))
        repository.saveTabs(saved)
        val viewModel = TabManagerViewModel(repository)

        viewModel.uiState.test {
            awaitItem()
            viewModel.onReset()
            assertEquals(TabManagerUiState.from(TabPreferences()), awaitItem())
        }
        assertEquals(saved, repository.tabs.value)
    }

    @Test
    fun onSave_persistsDraftAndDismisses() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = TabManagerViewModel(repository)
        viewModel.onMoved(5, 0)
        viewModel.onToggled(TabId.DISPLAY, false)
        assertEquals(TabPreferences(), repository.tabs.value)

        viewModel.effects.test {
            viewModel.onSave()
            assertEquals(TabManagerUiEffect.Dismiss, awaitItem())
        }
        assertEquals(
            TabPreferences(
                order = listOf(TabId.SCREEN_TEST) + TabId.entries.dropLast(1),
                hidden = setOf(TabId.DISPLAY),
            ),
            repository.tabs.value,
        )
    }
}
