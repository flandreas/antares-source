package io.antarescircuit.antares.model.oscilloscope

import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.graph.model.oscilloscope.SignalHistoryEntry
import kotlin.test.*

class VcdExportTest {

	private fun sample(value: Boolean, time: Long) = SignalHistoryEntry(DigitalSignalFactory.of(value), time)

	@Test
	fun shouldMakeNumericAndUnsafeProbeNamesValidIdentifiers() {
		val text = buildString { VcdExport.write(listOf(DigitalTrace("123 / $" + "end", listOf(sample(true, 0)))), this) }
		val declaration = text.lineSequence().first { it.startsWith("\$var") }
		assertTrue(Regex("[A-Za-z_][A-Za-z0-9_]*").matches(declaration.split(' ')[4]))
	}

	@Test
	fun shouldMergeProbeStreamsAndPreserveFourStateBusBits() {
		val bus = DigitalSignalFactory.ofBits(listOf(Bit.True, Bit.False, Bit.Undefined, Bit.Error))
		val traces = listOf(
			DigitalTrace("clock / 1", listOf(sample(false, 0), sample(true, 20), sample(false, 40))),
			DigitalTrace("data", listOf(SignalHistoryEntry(bus, 10))),
		)
		val text = buildString { VcdExport.write(traces, this, 50) }
		assertTrue(text.contains("\$var wire 4 s1 data_2 \$end"))
		assertTrue(text.contains("\$var wire 1 s0 clock___1_1 \$end"))
		assertTrue(text.contains("#10\nbxz01 s1\n#20\nb1 s0\n#40\nb0 s0\n#50\n"))
		assertEquals(listOf(0L, 10L, 20L, 40L, 50L), text.lineSequence().filter { it.startsWith('#') }.map { it.drop(1).toLong() }.toList())
	}

	@Test
	fun shouldRetainSameTimeTransitionsAndMarkTruncatedHistoryUnknown() {
		val trace = DigitalTrace("same", listOf(sample(false, 100), sample(true, 100)))
		val text = buildString { VcdExport.write(listOf(trace, trace), this, clocked = true) }
		assertTrue(text.contains("#0\n\$dumpvars\nbx s0\nbx s1\n\$end\n#100\n"))
		assertTrue(text.contains("same_1") && text.contains("same_2"))
		assertTrue(text.contains("timestamps are synthetic"))
		assertEquals(2, text.lineSequence().count { it == "b1 s0" || it == "b1 s1" })
	}

	@Test
	fun shouldRejectUnorderedOrInconsistentTracesBeforeWriting() {
		val output = StringBuilder()
		assertFailsWith<IllegalArgumentException> { VcdExport.write(emptyList(), output) }
		assertFailsWith<IllegalArgumentException> {
			VcdExport.write(listOf(DigitalTrace("bad", listOf(sample(true, 20), sample(false, 10)))), output)
		}
		assertFailsWith<IllegalArgumentException> {
			VcdExport.write(listOf(DigitalTrace("bad", listOf(sample(true, 0), SignalHistoryEntry(DigitalSignalFactory.of(BitWidth.BW_8, 1UL), 10)))), output)
		}
		assertEquals("", output.toString())
	}
}
