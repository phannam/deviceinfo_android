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
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.LinearLayout

internal class SlidingTabStrip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {

    private val bottomBorderThickness: Int
    private val bottomBorderPaint = Paint()

    private val selectedIndicatorThickness: Int
    private val selectedIndicatorPaint = Paint()

    private val defaultBottomBorderColor: Int

    private val dividerPaint = Paint()
    private val dividerHeight = DEFAULT_DIVIDER_HEIGHT

    private var selectedPosition = 0
    private var selectionOffset = 0f

    private var customTabColorizer: SlidingTabLayout.TabColorizer? = null
    private val defaultTabColorizer = SimpleTabColorizer()

    init {
        setWillNotDraw(false)

        val density = resources.displayMetrics.density

        val outValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.colorForeground, outValue, true)
        val themeForegroundColor = outValue.data

        defaultBottomBorderColor = setColorAlpha(themeForegroundColor, DEFAULT_BOTTOM_BORDER_COLOR_ALPHA)

        defaultTabColorizer.setIndicatorColors(DEFAULT_SELECTED_INDICATOR_COLOR)
        defaultTabColorizer.setDividerColors(setColorAlpha(themeForegroundColor, DEFAULT_DIVIDER_COLOR_ALPHA))

        bottomBorderThickness = (DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS * density).toInt()
        bottomBorderPaint.color = defaultBottomBorderColor

        selectedIndicatorThickness = (SELECTED_INDICATOR_THICKNESS_DIPS * density).toInt()

        dividerPaint.strokeWidth = (DEFAULT_DIVIDER_THICKNESS_DIPS * density).toInt().toFloat()
    }

    fun setCustomTabColorizer(customTabColorizer: SlidingTabLayout.TabColorizer?) {
        this.customTabColorizer = customTabColorizer
        invalidate()
    }

    fun setSelectedIndicatorColors(vararg colors: Int) {
        // Make sure that the custom colorizer is removed
        customTabColorizer = null
        defaultTabColorizer.setIndicatorColors(*colors)
        invalidate()
    }

    fun setDividerColors(vararg colors: Int) {
        // Make sure that the custom colorizer is removed
        customTabColorizer = null
        defaultTabColorizer.setDividerColors(*colors)
        invalidate()
    }

    fun onViewPagerPageChanged(position: Int, positionOffset: Float) {
        selectedPosition = position
        selectionOffset = positionOffset
        invalidate()
    }

    fun setTab(position: Int, positionOffset: Float) {
        selectedPosition = position
        selectionOffset = positionOffset
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val height = height
        val childCount = childCount
        val dividerHeightPx = (dividerHeight.coerceIn(0f, 1f) * height).toInt()
        val tabColorizer = customTabColorizer ?: defaultTabColorizer

        // Thick colored underline below the current selection
        if (childCount > 0) {
            val selectedTitle = getChildAt(selectedPosition)
            var left = selectedTitle.left
            var right = selectedTitle.right
            var color = tabColorizer.getIndicatorColor(selectedPosition)

            if (selectionOffset > 0f && selectedPosition < childCount - 1) {
                val nextColor = tabColorizer.getIndicatorColor(selectedPosition + 1)
                if (color != nextColor) {
                    color = blendColors(nextColor, color, selectionOffset)
                }

                // Draw the selection partway between the tabs
                val nextTitle = getChildAt(selectedPosition + 1)
                left = (selectionOffset * nextTitle.left + (1.0f - selectionOffset) * left).toInt()
                right = (selectionOffset * nextTitle.right + (1.0f - selectionOffset) * right).toInt()
            }

            selectedIndicatorPaint.color = color

            canvas.drawRect(
                left.toFloat(), (height - selectedIndicatorThickness).toFloat(),
                right.toFloat(), height.toFloat(), selectedIndicatorPaint,
            )
        }

        // Thin underline along the entire bottom edge
        canvas.drawRect(
            0f, (height - bottomBorderThickness).toFloat(),
            width.toFloat(), height.toFloat(), bottomBorderPaint,
        )

        // Vertical separators between the titles
        val separatorTop = (height - dividerHeightPx) / 2
        for (i in 0 until childCount - 1) {
            val child = getChildAt(i)
            dividerPaint.color = tabColorizer.getDividerColor(i)
            canvas.drawLine(
                child.right.toFloat(), separatorTop.toFloat(),
                child.right.toFloat(), (separatorTop + dividerHeightPx).toFloat(), dividerPaint,
            )
        }
    }

    private class SimpleTabColorizer : SlidingTabLayout.TabColorizer {
        private var indicatorColors = IntArray(0)
        private var dividerColors = IntArray(0)

        override fun getIndicatorColor(position: Int): Int = indicatorColors[position % indicatorColors.size]

        override fun getDividerColor(position: Int): Int = dividerColors[position % dividerColors.size]

        fun setIndicatorColors(vararg colors: Int) {
            indicatorColors = colors
        }

        fun setDividerColors(vararg colors: Int) {
            dividerColors = colors
        }
    }

    private companion object {
        const val DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS = 2
        const val DEFAULT_BOTTOM_BORDER_COLOR_ALPHA = 0x26
        const val SELECTED_INDICATOR_THICKNESS_DIPS = 5
        const val DEFAULT_SELECTED_INDICATOR_COLOR = 0xFF33B5E5.toInt()

        const val DEFAULT_DIVIDER_THICKNESS_DIPS = 1
        const val DEFAULT_DIVIDER_COLOR_ALPHA = 0x20
        const val DEFAULT_DIVIDER_HEIGHT = 0.5f

        /** Set the alpha value of the [color] to be the given [alpha] value. */
        fun setColorAlpha(color: Int, alpha: Int): Int =
            Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))

        /**
         * Blend [color1] and [color2] using the given ratio.
         *
         * @param ratio of which to blend. 1.0 will return [color1], 0.5 will give an even blend,
         *              0.0 will return [color2].
         */
        fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
            val inverseRatio = 1f - ratio
            val r = Color.red(color1) * ratio + Color.red(color2) * inverseRatio
            val g = Color.green(color1) * ratio + Color.green(color2) * inverseRatio
            val b = Color.blue(color1) * ratio + Color.blue(color2) * inverseRatio
            return Color.rgb(r.toInt(), g.toInt(), b.toInt())
        }
    }
}
