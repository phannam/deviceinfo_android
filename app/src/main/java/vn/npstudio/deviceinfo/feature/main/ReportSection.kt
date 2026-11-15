package vn.npstudio.deviceinfo.feature.main

import androidx.annotation.StringRes
import vn.npstudio.deviceinfo.ui.list.InfoRowUiModel

data class ReportSection(
    @param:StringRes val title: Int,
    val rows: List<InfoRowUiModel>,
)
