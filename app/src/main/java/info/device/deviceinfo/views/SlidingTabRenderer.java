package info.device.deviceinfo.views;

import java.util.List;

/**
 * Created by namphan on 5/12/15.
 */
public class SlidingTabRenderer implements SlidingFragmentTab.TabItemRenderer {
    private List<SlidingTab> tabs;
    private SlidingTabListener listener = null;

    public SlidingTabRenderer(List<SlidingTab> tabs, SlidingTabListener listener) {
        this.tabs = tabs;
        this.listener = listener;
    }

    public void setTabList(List<SlidingTab> tabs) {
        this.tabs = tabs;
    }

    public int getTabIndexByType(int tabType) {
        for (int i = 0; i < tabs.size(); i++) {
            SlidingTab slidingTab = tabs.get(i);
            if (slidingTab.getType() == tabType) {
                return i;
            }
        }
        return -1;
    }

    public interface SlidingTabListener {
        void setCurrentTab(SlidingTab slidingTab);
    }

    @Override
    public int getPageIcon(int position) {
        return this.tabs.get(position).getIconDrawable();
    }

    @Override
    public int getPageTitleColor(int position) {
        return this.tabs.get(position).getTextColor();
    }

    @Override
    public String getPageTitle(int position) {
        return this.tabs.get(position).getName();
    }

    @Override
    public int getCount() {
        return this.tabs.size();
    }

    @Override
    public void setCurrentItem(int position) {
        if (listener != null) {
            listener.setCurrentTab(tabs.get(position));
        }
    }
}
