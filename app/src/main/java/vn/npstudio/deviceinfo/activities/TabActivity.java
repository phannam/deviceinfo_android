package vn.npstudio.deviceinfo.activities;

import androidx.annotation.IdRes;
import androidx.fragment.app.FragmentTransaction;

import java.util.List;

import vn.npstudio.deviceinfo.fragments.BaseFragment;
import vn.npstudio.deviceinfo.views.SlidingFragmentTab;
import vn.npstudio.deviceinfo.views.SlidingTab;
import vn.npstudio.deviceinfo.views.SlidingTabRenderer;

/**
 * Created by namphan on 9/7/15.
 */

public abstract class TabActivity extends BaseActivity implements SlidingTabRenderer.SlidingTabListener {
    protected List<SlidingTab> slidingTabs;
    protected SlidingTab currentTab;
    protected SlidingTabRenderer slidingTabRenderer;
    protected SlidingFragmentTab mSlidingFragmentTab;
    protected BaseFragment currentView;

    protected abstract void initTabs();

    protected abstract BaseFragment getFragment(SlidingTab slidingTab);

    protected abstract
    @IdRes
    int getFragmentContainer();

    @Override
    public void setCurrentTab(SlidingTab slidingTab) {
        selectTab(currentTab, slidingTab);
    }

    protected void selectTab(SlidingTab fromTab, SlidingTab toTab) {
        mSlidingFragmentTab.selectTab(slidingTabs.indexOf(fromTab), slidingTabs.indexOf(toTab));
        if (currentView != null) {
            currentView.onPause();
            currentView.onStop();
        }
        currentView = getFragment(toTab);
        fromTab.setSelected(false);
        toTab.setSelected(true);
        currentTab = toTab;
        showView(currentView);
    }

    protected void showView(BaseFragment view) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        transaction.replace(getFragmentContainer(), view);
        transaction.addToBackStack(view.getClass().getSimpleName());
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.commit();
        mSlidingFragmentTab.redrawTabs();
    }


    protected SlidingTab getTabWithTypeOrFirstTab(int type) {
        for (SlidingTab tab : slidingTabs) {
            if (tab.getType() == type) return tab;
        }
        return slidingTabs.get(0);
    }


}
