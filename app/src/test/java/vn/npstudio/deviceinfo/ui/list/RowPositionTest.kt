package vn.npstudio.deviceinfo.ui.list

import org.junit.Assert.assertEquals
import org.junit.Test

class RowPositionTest {

    @Test
    fun singleRow_isSingle() {
        assertEquals(RowPosition.SINGLE, RowPosition.of(0, 1))
    }

    @Test
    fun twoRows_areTopAndBottom() {
        assertEquals(listOf(RowPosition.TOP, RowPosition.BOTTOM), (0 until 2).map { RowPosition.of(it, 2) })
    }

    @Test
    fun manyRows_haveMiddles() {
        assertEquals(
            listOf(RowPosition.TOP, RowPosition.MIDDLE, RowPosition.MIDDLE, RowPosition.BOTTOM),
            (0 until 4).map { RowPosition.of(it, 4) },
        )
    }
}
