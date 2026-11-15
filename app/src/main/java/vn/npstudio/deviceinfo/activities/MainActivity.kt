package vn.npstudio.deviceinfo.activities

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import vn.npstudio.deviceinfo.R
import vn.npstudio.deviceinfo.databinding.ActivityMainBinding
import vn.npstudio.deviceinfo.fragments.BaseFragment
import vn.npstudio.deviceinfo.fragments.DeviceFragment
import vn.npstudio.deviceinfo.fragments.DisplayFragment
import vn.npstudio.deviceinfo.utils.AppManager
import vn.npstudio.deviceinfo.utils.Constant
import vn.npstudio.deviceinfo.views.SlidingTab
import vn.npstudio.deviceinfo.views.SlidingTabRenderer

class MainActivity : TabActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.apply {
            title = "Info"
            setBackgroundDrawable(
                ContextCompat.getColor(this@MainActivity, R.color.actionbar_bg_color).toDrawable()
            )
        }
        initTabs()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_share) {
            checkExistingEmailAccountAndSendEmail()
        }
        return true
    }

    private fun checkExistingEmailAccountAndSendEmail() {
        val emailData = "mailto:namphanbk@gmail.com" +
            "?subject=" + Uri.encode(Build.DISPLAY + "information") +
            "&body=" + Uri.encode("")
        val filterEmailIntent = Intent(Intent.ACTION_SENDTO).setData(emailData.toUri())

        val infoList = packageManager.queryIntentActivities(filterEmailIntent, Intent.FLAG_ACTIVITY_NEW_TASK)
        when {
            infoList.isEmpty() -> Toast.makeText(this, "No email client!", Toast.LENGTH_LONG).show()
            infoList.size == 1 -> sendEmail(infoList[0].activityInfo)
            else -> sendEmail(null)
        }
    }

    private fun sendEmail(targetActivityInfo: ActivityInfo?) {
        val emailIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, "")
            putExtra(Intent.EXTRA_SUBJECT, Build.MODEL + " information")
            putExtra(Intent.EXTRA_TEXT, AppManager.getInstance().getAllInfos())
            targetActivityInfo?.let {
                component = ComponentName(it.packageName, it.name)
            }
        }
        try {
            startActivity(emailIntent)
        } catch (ex: Exception) {
            Toast.makeText(this, "Application not found!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun initTabs() {
        val normalColor = ContextCompat.getColor(this, R.color.tab_normal_color)
        val selectedColor = ContextCompat.getColor(this, R.color.tab_selected_color)
        slidingTabs = listOf(
            SlidingTab(Constant.Tab.TAB_DEVICEINFO, R.mipmap.ic_info_normal, "Device", normalColor).apply {
                setSelectedResource(R.mipmap.ic_info_selected, selectedColor)
            },
            SlidingTab(Constant.Tab.TAB_DISPLAY, R.mipmap.ic_display_normal, "Display", normalColor).apply {
                setSelectedResource(R.mipmap.ic_display_selected, selectedColor)
            },
        )

        slidingTabRenderer = SlidingTabRenderer(slidingTabs, this)

        selectedTab = getTabWithTypeOrFirstTab(Constant.Tab.TAB_DEVICEINFO)
        slidingFragmentTab = binding.slidingTabs
        slidingFragmentTab.setDividerColors(R.color.tab_device_color)
        slidingFragmentTab.selectTab(slidingTabs.indexOf(selectedTab), slidingTabs.indexOf(selectedTab))
        slidingFragmentTab.tabItemRenderer = slidingTabRenderer
        selectTab(selectedTab, selectedTab)
    }

    override fun getFragment(slidingTab: SlidingTab): BaseFragment = when (slidingTab.type) {
        Constant.Tab.TAB_DEVICEINFO -> DeviceFragment()
        else -> DisplayFragment()
    }

    override fun getFragmentContainer(): Int = R.id.fragment_container
}
