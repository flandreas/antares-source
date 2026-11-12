package io.antarescircuit.antares.view.oscilloscope

import io.antarescircuit.antares.model.oscilloscope.DigitalTrace
import io.antarescircuit.antares.model.oscilloscope.VcdExport
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.view.DigitalGraphView
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.io.WriteFileWrapper
import io.antarescircuit.jabbah.draw.view.AbstractViewAction
import io.antarescircuit.jabbah.graph.model.oscilloscope.SignalHistoriesType
import io.antarescircuit.jabbah.graph.model.oscilloscope.SignalHistoryEntry
import io.antarescircuit.jabbah.graph.view.oscilloscope.OscilloscopeView
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileNameExtensionFilter

/** Exports a stable snapshot before opening the modal chooser, even if simulation is still running. */
class ExportVcdAction : AbstractViewAction("antares.action.exportVcd") {

	override val opensDialog: Boolean get() = true

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && view?.mainContent?.drawable is DigitalGraphView

	override fun execute(event: ActionEvent) {
		val drawing = view?.mainContent?.drawable as? DigitalGraphView ?: return
		val scope = (drawing.getDrawable { it is OscilloscopeView } as? OscilloscopeView)?.model
		val traces = scope?.getPorts()?.mapNotNull { port ->
			val probeName = port.name ?: return@mapNotNull null
			val entries = scope.getSignalHistory(probeName)?.getEntriesSince(0)?.asSequence()?.mapNotNull { entry ->
				(entry.signal as? DigitalSignal)?.let { SignalHistoryEntry(it, entry.time) }
			}?.toList().orEmpty()
			if (entries.isEmpty()) null else DigitalTrace(probeName, entries)
		}.orEmpty()
		val parent = Frame.getFrames().firstOrNull { it.isActive }
		if (traces.isEmpty()) {
			JOptionPane.showMessageDialog(parent, Translations.getString("antares.action.exportVcd.empty"), name, JOptionPane.INFORMATION_MESSAGE)
			return
		}
		val endTime = scope!!.maxTime
		val clocked = scope.mode == SignalHistoriesType.Clocked
		val chooser = JFileChooser().apply {
			fileFilter = FileNameExtensionFilter("Value Change Dump (*.vcd)", "vcd")
		}
		if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return
		val selected = chooser.selectedFile
		val file = if (selected.extension.equals("vcd", ignoreCase = true)) selected else File(selected.path + ".vcd")
		if (file.exists() && JOptionPane.showConfirmDialog(parent,
			Translations.getString("antares.action.exportVcd.overwrite", file.name), name,
			JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return
		WriteFileWrapper.wrap(name) {
			file.bufferedWriter(Charsets.UTF_8).use { VcdExport.write(traces, it, endTime, clocked) }
		}
	}
}
