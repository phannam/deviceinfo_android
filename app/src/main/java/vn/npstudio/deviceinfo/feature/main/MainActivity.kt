package vn.npstudio.deviceinfo.feature.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import vn.npstudio.deviceinfo.BuildConfig
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.common.extension.collectWithLifecycle
import vn.npstudio.deviceinfo.databinding.ActivityMainBinding
import vn.npstudio.deviceinfo.feature.config.ConfigActivity
import vn.npstudio.deviceinfo.ui.window.WindowSnapshotReader
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.appBar) { appBar, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
            )
            appBar.updatePadding(left = bars.left, top = bars.top, right = bars.right)
            insets
        }
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_settings -> {
                    startActivity(Intent(this, ConfigActivity::class.java))
                    true
                }
                R.id.action_share_report -> {
                    viewModel.onShareClicked(WindowSnapshotReader.read(this, binding.root))
                    true
                }
                else -> false
            }
        }
        setUpTabs()

        viewModel.effects.collectWithLifecycle(this) { effect ->
            when (effect) {
                is MainUiEffect.Share -> share(effect)
            }
        }
    }

    private fun setUpTabs() {
        binding.viewPager.adapter = MainTabAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.setText(MainTab.entries[position].titleRes)
        }.attach()
        binding.viewPager.setCurrentItem(viewModel.selectedTab.value.ordinal, false)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.onTabSelected(MainTab.entries[position])
            }
        })
    }

    private fun share(effect: MainUiEffect.Share) {
        val appName = getString(R.string.app_name)
        val model = "${Build.MANUFACTURER} ${Build.MODEL}"
        val time = ZonedDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))
        val header = getString(R.string.report_header, appName, BuildConfig.VERSION_NAME, time, model)
        val text = ShareReportRenderer.render(effect.sections, header) { it.asString(this) }
        val send = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.report_subject, model))
            .putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(send, getString(R.string.share_chooser_title)))
    }
}
