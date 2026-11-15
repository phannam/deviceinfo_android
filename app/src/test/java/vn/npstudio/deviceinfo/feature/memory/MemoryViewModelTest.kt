package vn.npstudio.deviceinfo.feature.memory

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import vn.npstudio.deviceinfo.common.stateflow.LoadingMode
import vn.npstudio.deviceinfo.domain.model.MemoryInfo
import vn.npstudio.deviceinfo.testing.FakeMemoryRepository
import vn.npstudio.deviceinfo.testing.MainDispatcherRule
import vn.npstudio.deviceinfo.testing.sampleMemoryInfo
import vn.npstudio.deviceinfo.testing.sampleStorageInfo

class MemoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun uiState_combinesLiveRamWithStorage() = runTest(mainDispatcherRule.dispatcher) {
        val ram = MutableSharedFlow<MemoryInfo>(replay = 1)
        val viewModel = MemoryViewModel(FakeMemoryRepository(ram))
        viewModel.onResumed()

        viewModel.uiState.test {
            assertEquals(LoadingMode.INITIAL, awaitItem().loadingMode)

            ram.emit(sampleMemoryInfo)
            val loaded = awaitItem()
            assertEquals(LoadingMode.IDLE, loaded.loadingMode)
            assertEquals(MemoryRowMapper.map(sampleMemoryInfo, sampleStorageInfo), loaded.rows)

            val lessFree = sampleMemoryInfo.copy(availableBytes = 1_000_000_000L)
            ram.emit(lessFree)
            assertEquals(MemoryRowMapper.map(lessFree, sampleStorageInfo), awaitItem().rows)
        }
    }

    @Test
    fun onResumed_reloadsStorage() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeMemoryRepository()
        val viewModel = MemoryViewModel(repository)
        viewModel.onResumed()

        viewModel.uiState.test {
            assertEquals(LoadingMode.INITIAL, awaitItem().loadingMode)
            awaitItem()
            assertEquals(1, repository.storageLoads)

            val cleaned = sampleStorageInfo.copy(freeBytes = 20_000_000_000L)
            repository.storage = cleaned
            viewModel.onResumed()
            assertEquals(MemoryRowMapper.map(sampleMemoryInfo, cleaned), awaitItem().rows)
            assertEquals(2, repository.storageLoads)
        }
    }
}
