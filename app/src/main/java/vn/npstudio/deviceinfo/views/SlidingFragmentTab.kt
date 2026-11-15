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
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import vn.npstudio.deviceinfo.databinding.ViewTabBinding

class SlidingFragmentTab @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayout(context, attrs, defStyle) {

    private val tabStrip = SlidingTabStrip(context)

    var tabItemRenderer: TabItemRenderer? = null
        set(value) {
            field = value
            tabStrip.removeAllViews()
            populateTabStrip()
        }

    init {
        isHorizontalScrollBarEnabled = false
        addView(tabStrip, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    /**
     * Set the custom [SlidingTabLayout.TabColorizer] to be used.
     *
     * If you only require simple customisation then you can use
     * [setSelectedIndicatorColors] and [setDividerColors] to achieve similar effects.
     */
    fun setCustomTabColorizer(tabColorizer: SlidingTabLayout.TabColorizer?) {
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

    private fun populateTabStrip() {
        val renderer = tabItemRenderer ?: return
        val inflater = LayoutInflater.from(context)
        for (i in 0 until renderer.getCount()) {
            val tabBinding = ViewTabBinding.inflate(inflater, tabStrip, false)
            tabBinding.tabIconImage.setImageResource(renderer.getPageIcon(i))
            tabBinding.tabTitleText.text = renderer.getPageTitle(i)
            tabBinding.tabTitleText.setTextColor(renderer.getPageTitleColor(i))
            tabBinding.root.setOnClickListener(tabClickListener)
            tabStrip.addView(tabBinding.root)
        }
    }

    fun getTab(position: Int): View? = tabStrip.getChildAt(position)

    fun redrawTabs() {
        tabStrip.removeAllViews()
        populateTabStrip()
    }

    fun selectTab(previousPosition: Int, currentPosition: Int) {
        tabStrip.setTab(previousPosition, 0f)
        tabStrip.setTab(currentPosition, 0f)
    }

    interface TabItemRenderer {
        fun getPageIcon(position: Int): Int
        fun getPageTitleColor(position: Int): Int
        fun getPageTitle(position: Int): String
        fun getCount(): Int
        fun setCurrentItem(position: Int)
    }

    private val tabClickListener = OnClickListener { v ->
        for (i in 0 until tabStrip.childCount) {
            if (v === tabStrip.getChildAt(i)) {
                tabItemRenderer?.setCurrentItem(i)
                return@OnClickListener
            }
        }
    }
}
