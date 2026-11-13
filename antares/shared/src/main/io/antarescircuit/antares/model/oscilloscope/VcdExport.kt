package io.antarescircuit.antares.model.oscilloscope

import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.jabbah.base.collection.PriorityQueue
import io.antarescircuit.jabbah.graph.model.oscilloscope.SignalHistoryEntry

/** A snapshot of one digital probe, independent of subsequent simulation updates. */
data class DigitalTrace(val name: String, val entries: List<SignalHistoryEntry<DigitalSignal>>)

/** Writes retained digital transitions as a streaming Value Change Dump for external waveform viewers. */
object VcdExport {

	fun write(traces: List<DigitalTrace>, output: Appendable, endTime: Long = 0, clocked: Boolean = false) {
		require(traces.isNotEmpty() && traces.any { it.entries.isNotEmpty() }) { "No recorded digital signals" }
		val signals = traces.filter { it.entries.isNotEmpty() }
		for (trace in signals) {
			val width = trace.entries.first().signal.bitWidth
			require(trace.entries.all { it.time >= 0 && it.signal.bitWidth == width }) { "Invalid trace width or timestamp" }
			var previousTime = -1L
			for (entry in trace.entries) {
				require(entry.time >= previousTime) { "Trace timestamps must be ordered" }
				previousTime = entry.time
			}
		}
		output.append("\$version Antares \$end\n\$timescale 1 ns \$end\n")
		if (clocked) output.append("\$comment Clocked mode: timestamps are synthetic oscilloscope units, not physical nanoseconds. \$end\n")
		output.append("\$comment Only retained history is exported; values before the first retained sample are unknown. \$end\n")
		output.append("\$scope module antares \$end\n")
		for ((index, trace) in signals.withIndex()) {
			val sanitized = trace.name.map { if (it in 'a'..'z' || it in 'A'..'Z' || it in '0'..'9' || it == '_') it else '_' }
				.joinToString("").ifEmpty { "probe" }
			val name = if (sanitized.first().isDigit()) "_$sanitized" else sanitized
			output.append("\$var wire ${trace.entries.first().signal.bitWidth.width} s$index ${name}_${index + 1} \$end\n")
		}
		output.append("\$upscope \$end\n\$enddefinitions \$end\n#0\n\$dumpvars\n")
		for ((index, trace) in signals.withIndex()) {
			output.append("b${"x".repeat(trace.entries.first().signal.bitWidth.width)} s$index\n")
		}
		output.append("\$end\n")
		// Merge ordered probe streams without sorting or allocating a combined transition list.
		val pending = PriorityQueue<Cursor>()
		signals.forEachIndexed { index, trace -> pending.add(Cursor(index, trace.entries)) }
		var time = 0L
		while (!pending.isEmpty) {
			val cursor = pending.remove()
			val entry = cursor.entry
			if (entry.time != time) {
				time = entry.time
				output.append("#$time\n")
			}
			output.append('b')
			for (index in entry.signal.bitWidth.width - 1 downTo 0) {
				output.append(when (entry.signal.bitAt(index)) {
					Bit.False -> '0'
					Bit.True -> '1'
					Bit.Undefined -> 'z'
					Bit.Error -> 'x'
				})
			}
			output.append(" s${cursor.signalIndex}\n")
			cursor.index++
			if (cursor.index < cursor.entries.size) pending.add(cursor)
		}
		if (endTime > time) output.append("#$endTime\n")
	}

	private class Cursor(val signalIndex: Int, val entries: List<SignalHistoryEntry<DigitalSignal>>) : Comparable<Cursor> {
		override fun compareTo(other: Cursor): Int = entry.time.compareTo(other.entry.time)
		var index = 0
		val entry: SignalHistoryEntry<DigitalSignal> get() = entries[index]
	}
}
