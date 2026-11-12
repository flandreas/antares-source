package io.antarescircuit.antares.model.truthtable

import io.antarescircuit.antares.model.signal.Bit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TruthTableClipboardTest {

	@Test
	fun shouldReadSpreadsheetAndCsvSelectionsWithWindowsLineEndings() {
		val expected = listOf(listOf(Bit.True, Bit.Error), listOf(Bit.False, Bit.True))
		assertEquals(expected, TruthTableClipboard.parse("1\tx\r\n0\t1\r\n"))
		assertEquals(expected, TruthTableClipboard.parse("1,X\n0,1\n"))
		assertEquals(expected, TruthTableClipboard.parse("1 X\n0 1"))
	}

	@Test
	fun shouldRejectInvalidOrIncompleteDataWithoutSilentlyShiftingCells() {
		for (text in listOf("", "1,", "\t1", "1\t", "1,X\n0", "1\n\n0", "1,2", "1;0")) {
			assertFailsWith<IllegalArgumentException>(text) { TruthTableClipboard.parse(text) }
		}
	}
}
