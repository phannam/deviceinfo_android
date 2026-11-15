package vn.npstudio.deviceinfo.feature.main

sealed interface MainUiEffect {
    data class Share(val sections: List<ReportSection>) : MainUiEffect
}
