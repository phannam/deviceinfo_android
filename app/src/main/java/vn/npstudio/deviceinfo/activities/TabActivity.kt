package vn.npstudio.deviceinfo.activities

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentTransaction
import vn.npstudio.deviceinfo.fragments.BaseFragment
import vn.npstudio.deviceinfo.views.SlidingFragmentTab
import vn.npstudio.deviceinfo.views.SlidingTab
import vn.npstudio.deviceinfo.views.SlidingTabRenderer

abstract class TabActivity : BaseActivity(), SlidingTabRenderer.SlidingTabListener {
    protected lateinit var slidingTabs: List<SlidingTab>
    protected lateinit var selectedTab: SlidingTab
    protected lateinit var slidingTabRenderer: SlidingTabRenderer
    protected lateinit var slidingFragmentTab: SlidingFragmentTab
    protected var currentView: BaseFragment? = null

    protected abstract fun initTabs()

    protected abstract fun getFragment(slidingTab: SlidingTab): BaseFragment

    @IdRes
    protected abstract fun getFragmentContainer(): Int

    override fun setCurrentTab(slidingTab: SlidingTab) {
        selectTab(selectedTab, slidingTab)
    }

    protected fun selectTab(fromTab: SlidingTab, toTab: SlidingTab) {
        slidingFragmentTab.selectTab(slidingTabs.indexOf(fromTab), slidingTabs.indexOf(toTab))
        currentView?.let {
            it.onPause()
            it.onStop()
        }
        val view = getFragment(toTab)
        currentView = view
        fromTab.isSelected = false
        toTab.isSelected = true
        selectedTab = toTab
        showView(view)
    }

    protected fun showView(view: BaseFragment) {
        supportFragmentManager.beginTransaction()
            .replace(getFragmentContainer(), view)
            .addToBackStack(view.javaClass.simpleName)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()
        slidingFragmentTab.redrawTabs()
    }

    protected fun getTabWithTypeOrFirstTab(type: Int): SlidingTab =
        slidingTabs.firstOrNull { it.type == type } ?: slidingTabs[0]
}
