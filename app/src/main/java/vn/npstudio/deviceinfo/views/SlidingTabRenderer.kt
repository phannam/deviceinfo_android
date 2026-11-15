package vn.npstudio.deviceinfo.views

class SlidingTabRenderer(
    private var tabs: List<SlidingTab>,
    private val listener: SlidingTabListener?,
) : SlidingFragmentTab.TabItemRenderer {

    fun interface SlidingTabListener {
        fun setCurrentTab(slidingTab: SlidingTab)
    }

    fun setTabList(tabs: List<SlidingTab>) {
        this.tabs = tabs
    }

    fun getTabIndexByType(tabType: Int): Int = tabs.indexOfFirst { it.type == tabType }

    override fun getPageIcon(position: Int): Int = tabs[position].iconDrawable

    override fun getPageTitleColor(position: Int): Int = tabs[position].textColor

    override fun getPageTitle(position: Int): String = tabs[position].name

    override fun getCount(): Int = tabs.size

    override fun setCurrentItem(position: Int) {
        listener?.setCurrentTab(tabs[position])
    }
}
