package vn.npstudio.deviceinfo.data.source

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CpuSysFsParserTest {

    @Test
    fun parseCpuList_handlesRangesAndSingles() {
        assertEquals((0..7).toList(), CpuSysFsParser.parseCpuList("0-7\n"))
        assertEquals((0..7).toList(), CpuSysFsParser.parseCpuList("0-3,4-7"))
        assertEquals(listOf(0, 2, 5), CpuSysFsParser.parseCpuList("0,2,5"))
        assertEquals(listOf(0), CpuSysFsParser.parseCpuList("0"))
    }

    @Test
    fun parseCpuList_returnsEmptyForBadInput() {
        assertEquals(emptyList<Int>(), CpuSysFsParser.parseCpuList(null))
        assertEquals(emptyList<Int>(), CpuSysFsParser.parseCpuList(""))
        assertEquals(emptyList<Int>(), CpuSysFsParser.parseCpuList("abc"))
        assertEquals(emptyList<Int>(), CpuSysFsParser.parseCpuList("7-0"))
    }

    @Test
    fun parseKhz_acceptsPositiveNumbersOnly() {
        assertEquals(1_900_800L, CpuSysFsParser.parseKhz("1900800\n"))
        assertNull(CpuSysFsParser.parseKhz(null))
        assertNull(CpuSysFsParser.parseKhz(" "))
        assertNull(CpuSysFsParser.parseKhz("0"))
        assertNull(CpuSysFsParser.parseKhz("<unknown>"))
    }

    @Test
    fun parseHardware_readsHardwareLine() {
        val cpuinfo = """
            processor	: 0
            BogoMIPS	: 38.40
            Hardware	: Qualcomm Technologies, Inc MSM8998
        """.trimIndent()

        assertEquals("Qualcomm Technologies, Inc MSM8998", CpuSysFsParser.parseHardware(cpuinfo))
    }

    @Test
    fun parseHardware_returnsNullWhenLineIsMissing() {
        assertNull(CpuSysFsParser.parseHardware("processor\t: 0\nBogoMIPS\t: 38.40"))
        assertNull(CpuSysFsParser.parseHardware("Hardware\t:"))
        assertNull(CpuSysFsParser.parseHardware(null))
    }
}
