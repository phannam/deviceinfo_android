package vn.npstudio.deviceinfo.feature.config

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import vn.npstudio.deviceinfo.BuildConfig
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.extension.applySystemBarPadding
import vn.npstudio.deviceinfo.common.extension.collectWithLifecycle
import vn.npstudio.deviceinfo.databinding.ActivityConfigBinding
import vn.npstudio.deviceinfo.domain.model.DarkMode
import vn.npstudio.deviceinfo.domain.model.TemperatureUnit
import vn.npstudio.deviceinfo.feature.tabmanager.TabManagerBottomSheet
import vn.npstudio.deviceinfo.ui.theme.ColorRole

/** The user-facing "Settings" page (spec 002 section 4.3). */
@AndroidEntryPoint
class ConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfigBinding
    private val viewModel: ConfigViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.appBar) { appBar, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
            )
            appBar.updatePadding(left = bars.left, top = bars.top, right = bars.right)
            insets
        }
        binding.scrollView.applySystemBarPadding()
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.aboutVersion.text =
            getString(R.string.config_about_version, getString(R.string.app_name), BuildConfig.VERSION_NAME)
        binding.darkModeTile.setOnClickListener { showDarkModeDialog() }
        binding.primaryColorTile.setOnClickListener { showColorDialog(ColorRole.PRIMARY) }
        binding.accentColorTile.setOnClickListener { showColorDialog(ColorRole.ACCENT) }
        binding.tabManagerRow.setOnClickListener { TabManagerBottomSheet.show(supportFragmentManager) }
        binding.temperatureUnitRow.setOnClickListener { showTemperatureUnitDialog() }
        binding.privacyPolicyLink.setOnClickListener { openUrl(R.string.url_privacy_policy) }
        binding.termsOfServiceLink.setOnClickListener { openUrl(R.string.url_terms_of_service) }

        viewModel.uiState.collectWithLifecycle(this) { state ->
            binding.darkModeValue.text = state.darkModeLabel.asString(this)
            binding.temperatureUnitValue.text = state.temperatureUnitLabel.asString(this)
        }
    }

    private fun showDarkModeDialog() {
        val modes = DarkMode.entries
        showSingleChoice(
            R.string.config_dark_mode,
            modes.map { it.labelRes },
            modes.indexOf(viewModel.uiState.value.darkMode),
        ) { viewModel.onDarkModeSelected(modes[it]) }
    }

    private fun showColorDialog(role: ColorRole) {
        if (supportFragmentManager.findFragmentByTag(ColorPickerDialog.TAG) != null) return
        val state = viewModel.uiState.value
        val current = if (role == ColorRole.PRIMARY) state.primaryColor else state.accentColor
        ColorPickerDialog.newInstance(role, current).show(supportFragmentManager, ColorPickerDialog.TAG)
    }

    private fun showTemperatureUnitDialog() {
        val units = TemperatureUnit.entries
        showSingleChoice(
            R.string.config_temperature_unit,
            units.map { it.labelRes },
            units.indexOf(viewModel.uiState.value.temperatureUnit),
        ) { viewModel.onTemperatureUnitSelected(units[it]) }
    }

    private fun showSingleChoice(
        @StringRes title: Int,
        labels: List<Int>,
        checked: Int,
        onSelected: (Int) -> Unit,
    ) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setSingleChoiceItems(labels.map { getString(it) }.toTypedArray(), checked) { dialog, which ->
                dialog.dismiss()
                if (which != checked) onSelected(which)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun openUrl(@StringRes urlRes: Int) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, getString(urlRes).toUri()))
        } catch (e: ActivityNotFoundException) {
            Snackbar.make(binding.root, R.string.config_no_browser, Snackbar.LENGTH_SHORT).show()
        }
    }
}
