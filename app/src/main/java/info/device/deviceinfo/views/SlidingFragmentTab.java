/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.device.deviceinfo.views;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SlidingFragmentTab extends LinearLayout {

    private static final int TITLE_OFFSET_DIPS = 24;
    private static final int TAB_VIEW_PADDING_DIPS = 16;
    private static final int TAB_VIEW_TEXT_SIZE_SP = 12;
    private final SlidingTabStrip mTabStrip;
    private int mTabViewLayoutId;
    private int mTabViewTextViewId;
    private int mTabViewIconId;
    private TabItemRenderer tabItemRenderer;

    public SlidingFragmentTab(Context context) {
        this(context, null);
    }

    public SlidingFragmentTab(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingFragmentTab(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setHorizontalScrollBarEnabled(false);

        mTabStrip = new SlidingTabStrip(context);
        addView(mTabStrip, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    public TabItemRenderer getTabItemRenderer() {
        return tabItemRenderer;
    }

    public void setTabItemRenderer(TabItemRenderer tabItemRenderer) {
        this.tabItemRenderer = tabItemRenderer;
        mTabStrip.removeAllViews();

        if (this.tabItemRenderer != null)
            populateTabStrip();
    }

    /**
     * Set the custom {@link com.saleshood.saleshoodlib.views.SlidingTabLayout.TabColorizer} to be used.
     * <p/>
     * If you only require simple custmisation then you can use
     * {@link #setSelectedIndicatorColors(int...)} and {@link #setDividerColors(int...)} to achieve
     * similar effects.
     */
    public void setCustomTabColorizer(SlidingTabLayout.TabColorizer tabColorizer) {
        mTabStrip.setCustomTabColorizer(tabColorizer);
    }

    /**
     * Sets the colors to be used for indicating the selected tab. These colors are treated as a
     * circular array. Providing one color will mean that all tabs are indicated with the same color.
     */
    public void setSelectedIndicatorColors(int... colors) {
        mTabStrip.setSelectedIndicatorColors(colors);
    }

    /**
     * Sets the colors to be used for tab dividers. These colors are treated as a circular array.
     * Providing one color will mean that all tabs are indicated with the same color.
     */
    public void setDividerColors(int... colors) {
        mTabStrip.setDividerColors(colors);
    }

    /**
     * Set the custom layout to be inflated for the tab views.
     *
     * @param layoutResId Layout id to be inflated
     * @param textViewId  id of the {@link android.widget.TextView} in the inflated view
     * @param iconId      id of the {@link android.widget.ImageView} in the inflated view
     */
    public void setCustomTabView(int layoutResId, int textViewId, int iconId) {
        mTabViewLayoutId = layoutResId;
        mTabViewTextViewId = textViewId;
        mTabViewIconId = iconId;
    }

    /**
     * Create a default view to be used for tabs. This is called if a custom tab view is not set via
     * {@link #setCustomTabView(int, int, int)}.
     */
    protected TextView createDefaultTabView(Context context) {
        TextView textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, TAB_VIEW_TEXT_SIZE_SP);
        textView.setTypeface(Typeface.DEFAULT_BOLD);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // If we're running on Honeycomb or newer, then we can use the Theme's
            // selectableItemBackground to ensure that the View has a pressed state
            TypedValue outValue = new TypedValue();
            getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground,
                    outValue, true);
            textView.setBackgroundResource(outValue.resourceId);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // If we're running on ICS or newer, enable all-caps to match the Action Bar tab style
            textView.setAllCaps(true);
        }

        int padding = (int) (TAB_VIEW_PADDING_DIPS * getResources().getDisplayMetrics().density);
        textView.setPadding(padding, padding, padding, padding);

        return textView;
    }

    private void populateTabStrip() {
        if (this.tabItemRenderer == null)
            return;
        final OnClickListener tabClickListener = new TabClickListener();
        int tabCount = this.tabItemRenderer.getCount();
        for (int i = 0; i < tabCount; i++) {
            View tabView = null;
            TextView tabTitleView = null;
            ImageView tabIconView = null;
            TextView badgeView = null;

            if (mTabViewLayoutId != 0) {
                // If there is a custom tab view layout id set, try and inflate it
                tabView = LayoutInflater.from(getContext()).inflate(mTabViewLayoutId, mTabStrip, false);
                tabTitleView = (TextView) tabView.findViewById(mTabViewTextViewId);
                tabIconView = (ImageView) tabView.findViewById(mTabViewIconId);
            }

            if (tabView == null) {
                tabView = createDefaultTabView(getContext());
            }

            if (tabTitleView == null && TextView.class.isInstance(tabView)) {
                tabTitleView = (TextView) tabView;
            }

            if (tabIconView != null && this.tabItemRenderer != null)
                tabIconView.setImageResource(this.tabItemRenderer.getPageIcon(i));
            if (this.tabItemRenderer != null) {
                tabTitleView.setText(this.tabItemRenderer.getPageTitle(i));
                tabTitleView.setTextColor(this.tabItemRenderer.getPageTitleColor(i));
            }
            tabView.setOnClickListener(tabClickListener);

            mTabStrip.addView(tabView);
        }
    }

    public View getTab(int position) {
        return mTabStrip.getChildAt(position);
    }

    public void redrawTabs() {
        mTabStrip.removeAllViews();
        populateTabStrip();
    }

    public void selectTab(int previousPosition, int currentPosition) {
        mTabStrip.setTab(previousPosition, 0);
        mTabStrip.setTab(currentPosition, 0);
    }

    public interface TabItemRenderer {
        int getPageIcon(int position);
        int getPageTitleColor(int position);
        String getPageTitle(int position);

        int getCount();

        void setCurrentItem(int position);
    }

    private class TabClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                if (v == mTabStrip.getChildAt(i)) {
                    if (tabItemRenderer != null)
                        tabItemRenderer.setCurrentItem(i);
                    return;
                }
            }
        }
    }

}
