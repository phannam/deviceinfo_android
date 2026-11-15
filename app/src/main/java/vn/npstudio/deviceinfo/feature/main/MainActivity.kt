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
import vn.npstudio.deviceinfo.feature.tabmanager.TabManagerBottomSheet
import vn.npstudio.deviceinfo.ui.window.WindowSnapshotReader
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var tabAdapter: MainTabAdapter

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
                R.id.action_tab_manager -> {
                    TabManagerBottomSheet.show(supportFragmentManager)
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
        tabAdapter = MainTabAdapter(this)
        binding.viewPager.adapter = tabAdapter
        // autoRefresh rebuilds the tab titles whenever the adapter list changes.
        TabLayoutMediator(binding.tabLayout, binding.viewPager, true) { tab, position ->
            tab.setText(tabAdapter.tabs[position].titleRes)
        }.attach()
        // Fill the adapter synchronously so ViewPager2 can restore fragments and the current page.
        showTabs(viewModel.visibleTabs.value)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.onTabSelected(tabAdapter.tabs[position])
            }
        })
        viewModel.visibleTabs.collectWithLifecycle(this) { showTabs(it) }
    }

    /** Applies the Tab Manager settings without recreating the Activity. */
    private fun showTabs(tabs: List<MainTab>) {
        if (tabs == tabAdapter.tabs) return
        // Resolve before submitting: the adapter change can report a page that is not the user's choice.
        val selected = viewModel.resolveSelectedTab(tabs)
        tabAdapter.submit(tabs)
        binding.viewPager.setCurrentItem(tabs.indexOf(selected), false)
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
