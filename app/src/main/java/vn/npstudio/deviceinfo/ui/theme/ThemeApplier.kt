package vn.npstudio.deviceinfo.ui.theme

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import vn.npstudio.deviceinfo.domain.repository.AppConfigRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Applies the configured night mode app-wide and the primary/accent theme overlays to every Activity
 * before it inflates views. Activities already open must recreate themselves after a color change.
 */
@Singleton
class ThemeApplier @Inject constructor(
    private val appConfigRepository: AppConfigRepository,
) : Application.ActivityLifecycleCallbacks {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /** Call from `Application.onCreate`; the current night mode is applied synchronously. */
    fun install(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
        appConfigRepository.config
            .map { NightModeMapper.toAppCompat(it.darkMode) }
            .distinctUntilChanged()
            .onEach(AppCompatDelegate::setDefaultNightMode)
            .launchIn(scope)
    }

    // API 29+ runs before Activity.onCreate; older versions fall back to onActivityCreated,
    // which still runs inside super.onCreate() and before setContentView().
    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        applyOverlays(activity)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) applyOverlays(activity)
    }

    private fun applyOverlays(activity: Activity) {
        val config = appConfigRepository.config.value
        activity.theme.applyStyle(ThemeOverlays.primary(config.primaryColor), true)
        activity.theme.applyStyle(ThemeOverlays.accent(config.accentColor), true)
    }

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) = Unit
}
