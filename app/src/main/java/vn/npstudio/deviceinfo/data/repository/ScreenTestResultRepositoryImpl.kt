package vn.npstudio.deviceinfo.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import vn.npstudio.deviceinfo.domain.model.ScreenTestResult
import vn.npstudio.deviceinfo.domain.repository.ScreenTestResultRepository
import javax.inject.Inject
import javax.inject.Singleton

/** In-memory only: results are kept for the app session and are not persisted. */
@Singleton
class ScreenTestResultRepositoryImpl @Inject constructor() : ScreenTestResultRepository {

    private val _result = MutableStateFlow<ScreenTestResult?>(null)
    override val result: StateFlow<ScreenTestResult?> = _result.asStateFlow()

    override fun save(result: ScreenTestResult) {
        _result.value = result
    }
}
