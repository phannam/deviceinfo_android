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

package vn.npstudio.deviceinfo.views

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager

/**
 * To be used with ViewPager to provide a tab indicator component which give constant feedback as to
 * the user's scroll progress.
 *
 * To use the component, simply add it to your view hierarchy. Then in your Activity or Fragment
 * call [setViewPager] providing it the ViewPager this layout is being used for.
 *
 * The colors can be customized in two ways. The first and simplest is to provide an array of colors
 * via [setSelectedIndicatorColors] and [setDividerColors]. The alternative is via the
 * [TabColorizer] interface which provides you complete control over which color is used for any
 * individual position.
 *
 * The views used as tabs can be customized by calling [setCustomTabView], providing the layout ID
 * of your custom layout.
 */
class SlidingTabLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayout(context, attrs, defStyle) {

    /**
     * Allows complete control over the colors drawn in the tab layout. Set with
     * [setCustomTabColorizer].
     */
    interface TabColorizer {
        /** @return the color of the indicator used when [position] is selected. */
        fun getIndicatorColor(position: Int): Int

        /** @return the color of the divider drawn to the right of [position]. */
        fun getDividerColor(position: Int): Int
    }

    private val titleOffset: Int = (TITLE_OFFSET_DIPS * resources.displayMetrics.density).toInt()

    private var tabViewLayoutId = 0
    private var tabViewTextViewId = 0
    private var tabViewIconId = 0

    private var viewPager: ViewPager? = null
    private var viewPagerPageChangeListener: ViewPager.OnPageChangeListener? = null

    private val tabStrip = SlidingTabStrip(context)

    init {
        // Disable the Scroll Bar
        isHorizontalScrollBarEnabled = false
        addView(tabStrip, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    /**
     * Set the custom [TabColorizer] to be used.
     *
     * If you only require simple customisation then you can use
     * [setSelectedIndicatorColors] and [setDividerColors] to achieve similar effects.
     */
    fun setCustomTabColorizer(tabColorizer: TabColorizer?) {
        tabStrip.setCustomTabColorizer(tabColorizer)
    }

    /**
     * Sets the colors to be used for indicating the selected tab. These colors are treated as a
     * circular array. Providing one color will mean that all tabs are indicated with the same color.
     */
    fun setSelectedIndicatorColors(vararg colors: Int) {
        tabStrip.setSelectedIndicatorColors(*colors)
    }

    /**
     * Sets the colors to be used for tab dividers. These colors are treated as a circular array.
     * Providing one color will mean that all tabs are indicated with the same color.
     */
    fun setDividerColors(vararg colors: Int) {
        tabStrip.setDividerColors(*colors)
    }

    /**
     * Set the [ViewPager.OnPageChangeListener]. When using [SlidingTabLayout] you are required to
     * set any [ViewPager.OnPageChangeListener] through this method. This is so that the layout can
     * update its scroll position correctly.
     */
    fun setOnPageChangeListener(listener: ViewPager.OnPageChangeListener?) {
        viewPagerPageChangeListener = listener
    }

    /**
     * Set the custom layout to be inflated for the tab views.
     *
     * @param layoutResId Layout id to be inflated
     * @param textViewId  id of the [TextView] in the inflated view
     * @param iconId      id of the [ImageView] in the inflated view
     */
    fun setCustomTabView(layoutResId: Int, textViewId: Int, iconId: Int) {
        tabViewLayoutId = layoutResId
        tabViewTextViewId = textViewId
        tabViewIconId = iconId
    }

    /**
     * Sets the associated view pager. Note that the assumption here is that the pager content
     * (number of tabs and tab titles) does not change after this call has been made.
     */
    fun setViewPager(viewPager: ViewPager?) {
        tabStrip.removeAllViews()

        this.viewPager = viewPager
        if (viewPager != null) {
            viewPager.clearOnPageChangeListeners()
            viewPager.addOnPageChangeListener(InternalViewPagerListener())
            populateTabStrip()
        }
    }

    /**
     * Create a default view to be used for tabs. This is called if a custom tab view is not set via
     * [setCustomTabView].
     */
    protected fun createDefaultTabView(context: Context): TextView = TextView(context).apply {
        gravity = Gravity.CENTER
        setTextSize(TypedValue.COMPLEX_UNIT_SP, TAB_VIEW_TEXT_SIZE_SP.toFloat())
        typeface = Typeface.DEFAULT_BOLD

        // Use the Theme's selectableItemBackground to ensure that the View has a pressed state
        val outValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        setBackgroundResource(outValue.resourceId)

        // Enable all-caps to match the Action Bar tab style
        isAllCaps = true

        val padding = (TAB_VIEW_PADDING_DIPS * resources.displayMetrics.density).toInt()
        setPadding(padding, padding, padding, padding)
    }

    private fun populateTabStrip() {
        val adapter = viewPager?.adapter ?: return
        val tabClickListener = OnClickListener { v ->
            for (i in 0 until tabStrip.childCount) {
                if (v === tabStrip.getChildAt(i)) {
                    viewPager?.currentItem = i
                    return@OnClickListener
                }
            }
        }

        for (i in 0 until adapter.count) {
            var tabView: View? = null
            var tabTitleView: TextView? = null

            if (tabViewLayoutId != 0) {
                // If there is a custom tab view layout id set, try and inflate it
                tabView = LayoutInflater.from(context).inflate(tabViewLayoutId, tabStrip, false)
                tabTitleView = tabView.findViewById(tabViewTextViewId)
            }

            if (tabView == null) {
                tabView = createDefaultTabView(context)
            }

            if (tabTitleView == null && tabView is TextView) {
                tabTitleView = tabView
            }
            tabTitleView?.text = adapter.getPageTitle(i)
            tabView.setOnClickListener(tabClickListener)

            tabStrip.addView(tabView)
        }
    }

    private fun scrollToTab(tabIndex: Int, positionOffset: Int) {
        val tabStripChildCount = tabStrip.childCount
        if (tabStripChildCount == 0 || tabIndex < 0 || tabIndex >= tabStripChildCount) {
            return
        }

        val selectedChild = tabStrip.getChildAt(tabIndex) ?: return
        var targetScrollX = selectedChild.left + positionOffset

        if (tabIndex > 0 || positionOffset > 0) {
            // If we're not at the first child and are mid-scroll, make sure we obey the offset
            targetScrollX -= titleOffset
        }

        scrollTo(targetScrollX, 0)
    }

    fun getTab(position: Int): View? = tabStrip.getChildAt(position)

    fun selectTab(position: Int) {
        viewPager?.currentItem = position
    }

    private inner class InternalViewPagerListener : ViewPager.OnPageChangeListener {
        private var scrollState = 0
        private var lastSelection = 0

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            val tabStripChildCount = tabStrip.childCount
            if (tabStripChildCount == 0 || position < 0 || position >= tabStripChildCount) {
                return
            }

            tabStrip.onViewPagerPageChanged(position, positionOffset)

            viewPagerPageChangeListener?.onPageScrolled(position, positionOffset, positionOffsetPixels)

            // Add color filter for selected tab
            clearTint(lastSelection)
            applyTint(position)
            lastSelection = position
        }

        override fun onPageScrollStateChanged(state: Int) {
            scrollState = state
            viewPagerPageChangeListener?.onPageScrollStateChanged(state)
        }

        override fun onPageSelected(position: Int) {
            if (scrollState == ViewPager.SCROLL_STATE_IDLE) {
                tabStrip.onViewPagerPageChanged(position, 0f)
            }
            viewPagerPageChangeListener?.onPageSelected(position)
        }

        private fun applyTint(position: Int) {
            val tabView = tabStrip.getChildAt(position)
            val blueColor = ContextCompat.getColor(context, android.R.color.holo_blue_bright)

            // Filter color for title
            tabView.findViewById<TextView>(tabViewTextViewId)?.let {
                val darkText = ContextCompat.getColor(context, android.R.color.darker_gray)
                it.setTextColor(blueColor and darkText)
            }
            // Filter color for icon
            tabView.findViewById<ImageView>(tabViewIconId)?.colorFilter =
                PorterDuffColorFilter(blueColor, PorterDuff.Mode.MULTIPLY)
        }

        private fun clearTint(position: Int) {
            val tabView = tabStrip.getChildAt(position)
            tabView.findViewById<TextView>(tabViewTextViewId)?.setTextColor(-0x76000000)
            tabView.findViewById<ImageView>(tabViewIconId)?.clearColorFilter()
        }
    }

    private companion object {
        const val TITLE_OFFSET_DIPS = 24
        const val TAB_VIEW_PADDING_DIPS = 16
        const val TAB_VIEW_TEXT_SIZE_SP = 12
    }
}
