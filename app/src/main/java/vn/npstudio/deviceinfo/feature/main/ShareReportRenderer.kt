package vn.npstudio.deviceinfo.feature.main

import vn.npstudio.deviceinfo.common.text.UiText

/** Renders the plain-text share report. [resolve] turns [UiText] into localized strings. */
object ShareReportRenderer {

    fun render(sections: List<ReportSection>, header: String, resolve: (UiText) -> String): String =
        buildString {
            append(header)
            sections.forEach { section ->
                append("\n\n")
                append(resolve(UiText.res(section.title)))
                section.rows.forEach { row ->
                    append('\n')
                    append(resolve(UiText.res(row.label)))
                    append(": ")
                    append(resolve(row.value))
                }
            }
            append('\n')
        }
}
