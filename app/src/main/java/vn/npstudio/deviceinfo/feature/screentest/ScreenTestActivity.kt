package vn.npstudio.deviceinfo.feature.screentest

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.extension.collectWithLifecycle
import vn.npstudio.deviceinfo.databinding.ActivityScreenTestBinding
import vn.npstudio.deviceinfo.domain.model.ScreenDefect

/** Full-screen pattern test. Tap advances; Back exits without saving results. */
@AndroidEntryPoint
class ScreenTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScreenTestBinding
    private val viewModel: ScreenTestViewModel by viewModels()
    private var resultDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScreenTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enterFullScreen()

        binding.patternView.setOnClickListener { viewModel.onTap() }
        viewModel.uiState.collectWithLifecycle(this) { render(it) }
        viewModel.effects.collectWithLifecycle(this) { effect ->
            when (effect) {
                ScreenTestUiEffect.Finish -> finish()
            }
        }
    }

    override fun onDestroy() {
        // dismiss() does not trigger the cancel listener, so recreation keeps the answers.
        resultDialog?.dismiss()
        resultDialog = null
        super.onDestroy()
    }

    private fun enterFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // Window-level brightness only applies while this Activity is visible; no WRITE_SETTINGS.
        window.attributes = window.attributes.apply {
            screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
    }

    private fun render(state: ScreenTestUiState) {
        binding.patternView.pattern = state.pattern
        binding.hintText.text = getString(R.string.screen_test_hint, state.position, state.total)
        binding.hintText.isVisible = state.hintVisible
        if (state.askingResult && resultDialog == null) showResultDialog(state.defects)
    }

    private fun showResultDialog(checkedDefects: Set<ScreenDefect>) {
        val defects = ScreenDefect.entries
        val labels = defects.map { getString(ScreenTestRowMapper.labelRes(it)) }.toTypedArray<CharSequence>()
        val checked = defects.map { it in checkedDefects }.toBooleanArray()
        resultDialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.screen_test_result_title)
            .setMultiChoiceItems(labels, checked) { _, which, isChecked ->
                viewModel.onDefectChecked(defects[which], isChecked)
            }
            .setPositiveButton(R.string.screen_test_save) { _, _ -> viewModel.onSaveClicked() }
            .setNegativeButton(R.string.screen_test_discard) { _, _ -> viewModel.onDiscardClicked() }
            .setOnCancelListener { viewModel.onDiscardClicked() }
            .create()
            .apply {
                setCanceledOnTouchOutside(false)
                show()
            }
    }
}
