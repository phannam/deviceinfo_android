package vn.npstudio.deviceinfo.feature.device

import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.domain.model.DeviceInfo
import vn.npstudio.deviceinfo.ui.format.ValueFormatter
import vn.npstudio.deviceinfo.ui.list.InfoRowUiModel
import java.util.Locale

object DeviceRowMapper {

    fun map(info: DeviceInfo, locale: Locale = Locale.getDefault()): List<InfoRowUiModel> = listOf(
        InfoRowUiModel(R.string.label_device_manufacturer, ValueFormatter.text(info.manufacturer)),
        InfoRowUiModel(R.string.label_device_brand, ValueFormatter.text(info.brand)),
        InfoRowUiModel(R.string.label_device_model, ValueFormatter.text(info.model)),
        InfoRowUiModel(R.string.label_device_codename, ValueFormatter.text(info.device)),
        InfoRowUiModel(R.string.label_device_product, ValueFormatter.text(info.product)),
        InfoRowUiModel(R.string.label_device_board, ValueFormatter.text(info.board)),
        InfoRowUiModel(R.string.label_device_hardware, ValueFormatter.text(info.hardware)),
        InfoRowUiModel(R.string.label_device_android_version, ValueFormatter.text(info.androidVersion)),
        InfoRowUiModel(R.string.label_device_api_level, ValueFormatter.number(info.apiLevel)),
        InfoRowUiModel(R.string.label_device_security_patch, ValueFormatter.isoDate(info.securityPatch, locale)),
        InfoRowUiModel(R.string.label_device_build_number, ValueFormatter.text(info.buildNumber)),
        InfoRowUiModel(R.string.label_device_fingerprint, ValueFormatter.text(info.fingerprint)),
        InfoRowUiModel(R.string.label_device_bootloader, ValueFormatter.text(info.bootloader)),
        InfoRowUiModel(R.string.label_device_kernel, ValueFormatter.text(info.kernelVersion)),
        InfoRowUiModel(R.string.label_device_java_vm, ValueFormatter.text(info.javaVmVersion)),
        InfoRowUiModel(R.string.label_device_uptime, ValueFormatter.duration(info.uptimeMillis)),
    )
}
