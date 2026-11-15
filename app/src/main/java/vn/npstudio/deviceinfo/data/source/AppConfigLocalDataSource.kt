package vn.npstudio.deviceinfo.data.source

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Key-value storage for the app configuration. Keys are in [AppConfigKeys]. */
interface AppConfigLocalDataSource {
    fun getString(key: String): String?

    fun putString(key: String, value: String)
}

object AppConfigKeys {
    const val DARK_MODE = "dark_mode"
    const val PRIMARY_COLOR = "primary_color"
    const val ACCENT_COLOR = "accent_color"
    const val TEMPERATURE_UNIT = "temperature_unit"
    const val TAB_ORDER = "tab_order"
    const val TAB_HIDDEN = "tab_hidden"
}

class AppConfigLocalDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : AppConfigLocalDataSource {

    private val preferences by lazy { context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE) }

    override fun getString(key: String): String? = preferences.getString(key, null)

    override fun putString(key: String, value: String) {
        preferences.edit { putString(key, value) }
    }

    private companion object {
        const val FILE_NAME = "app_config"
    }
}
