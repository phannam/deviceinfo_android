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
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import vn.npstudio.deviceinfo.domain.repository.AppConfigRepository
import java.util.WeakHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Applies the configured night mode app-wide and the primary/accent theme overlays to every Activity
 * before it inflates views. After a color change, started activities recreate at once and stopped ones
 * recreate when they start again.
 */
@Singleton
class ThemeApplier @Inject constructor(
    private val appConfigRepository: AppConfigRepository,
) : Application.ActivityLifecycleCallbacks {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // Main thread only. Weak keys so a leaked callback never holds a destroyed Activity.
    private val appliedColors = WeakHashMap<Activity, ThemeColors>()
    private val startedActivities = WeakHashMap<Activity, Unit>()

    /** Call from `Application.onCreate`; the current night mode is applied synchronously. */
    fun install(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
        appConfigRepository.config
            .map { NightModeMapper.toAppCompat(it.darkMode) }
            .distinctUntilChanged()
            .onEach(AppCompatDelegate::setDefaultNightMode)
            .launchIn(scope)
        appConfigRepository.config
            .map(ThemeColors::of)
            .distinctUntilChanged()
            .drop(1)
            .onEach { startedActivities.keys.toList().forEach(::recreateIfStale) }
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
        val colors = ThemeColors.of(appConfigRepository.config.value)
        activity.theme.applyStyle(ThemeOverlays.primary(colors.primary), true)
        activity.theme.applyStyle(ThemeOverlays.accent(colors.accent), true)
        appliedColors[activity] = colors
    }

    private fun recreateIfStale(activity: Activity) {
        val applied = appliedColors[activity] ?: return
        if (applied != ThemeColors.of(appConfigRepository.config.value) && !activity.isFinishing) {
            appliedColors.remove(activity)
            activity.recreate()
        }
    }

    override fun onActivityStarted(activity: Activity) {
        startedActivities[activity] = Unit
        recreateIfStale(activity)
    }

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) {
        startedActivities.remove(activity)
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) {
        startedActivities.remove(activity)
        appliedColors.remove(activity)
    }
}
