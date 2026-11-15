package vn.npstudio.deviceinfo.common.text

import android.content.Context
import androidx.annotation.StringRes

sealed interface UiText {
    data class Dynamic(val value: String) : UiText

    /** [args] may contain nested [UiText] values; they are resolved before formatting. */
    data class Resource(@param:StringRes val resId: Int, val args: List<Any> = emptyList()) : UiText

    /** Resolves every part and joins them with [separator]. */
    data class Joined(val parts: List<UiText>, val separator: String) : UiText

    fun asString(context: Context): String = when (this) {
        is Dynamic -> value
        is Resource -> if (args.isEmpty()) {
            context.getString(resId)
        } else {
            val resolved = args.map { if (it is UiText) it.asString(context) else it }
            context.getString(resId, *resolved.toTypedArray())
        }
        is Joined -> parts.joinToString(separator) { it.asString(context) }
    }

    companion object {
        fun res(@StringRes resId: Int, vararg args: Any): UiText = Resource(resId, args.toList())
    }
}
